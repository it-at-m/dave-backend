package de.muenchen.dave.services.messstelle.auswertung;

import de.muenchen.dave.configuration.LogExecutionTime;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.Auswertung;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.AuswertungProMessstelleUndZeitraum;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.AuswertungProMessstelle;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.MessstelleAuswertungDTO;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.MessstelleAuswertungOptionsDTO;
import de.muenchen.dave.domain.enums.AuswertungsZeitraum;
import de.muenchen.dave.domain.mapper.detektor.AuswertungMapper;
import de.muenchen.dave.geodateneai.gen.model.TagesaggregatDto;
import de.muenchen.dave.services.messstelle.MessstelleService;
import de.muenchen.dave.services.messstelle.MesswerteService;
import de.muenchen.dave.services.messstelle.Zeitraum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class AuswertungService {

    private final MessstelleService messstelleService;

    private final MesswerteService messwerteService;

    private final AuswertungMapper auswertungMapper;

    private final SpreadsheetService spreadsheetService;

    public List<MessstelleAuswertungDTO> getAllVisibleMessstellen() {
        return messstelleService.getAllVisibleMessstellenForAuswertungOrderByMstIdAsc();
    }

    /**
     * Erzeugt mittels der geladenen Daten eine Datei für die Auswertung
     *
     * @param options Optionen für die Auswertung
     * @return Auswertungsdatei als byte[]
     * @throws IOException kann beim Erstellen des byte[] geworfen werden. Fehlerbehandlung erfolgt im
     *             Controller
     */
    @LogExecutionTime
    public byte[] createAuswertungsfile(final MessstelleAuswertungOptionsDTO options) throws IOException {
        log.debug("#createAuswertungsfile {}", options);
        if (CollectionUtils.isEmpty(options.getMessstelleAuswertungIds())) {
            throw new IllegalArgumentException("Es wurden keine Messstellen ausgewählt.");
        }
        final var auswertungenMqByMstId = this.ladeAuswertungGroupedByMstId(options);
        return spreadsheetService.createFile(auswertungenMqByMstId, options);
    }

    /**
     * Lädt die Daten pro Messstelle pro Zeitraum.
     *
     * @param options Definierte Optionen zum Laden der Daten
     * @return Liste an Auswertungen Pro Messstelle
     */
    protected List<AuswertungProMessstelle> ladeAuswertungGroupedByMstId(final MessstelleAuswertungOptionsDTO options) {

        final List<Zeitraum> zeitraeume = this.createZeitraeume(options.getZeitraum(), options.getJahre());

        final ConcurrentMap<String, List<AuswertungProMessstelleUndZeitraum>> auswertungenGroupedByMstId = CollectionUtils
                // Lädt die Daten pro Messstelle
                .emptyIfNull(options.getMessstelleAuswertungIds())
                .parallelStream()
                // Lädt die Daten einer Messstelle pro Zeitraum
                .flatMap(messstelleAuswertungIdDTO -> CollectionUtils.emptyIfNull(zeitraeume)
                        .parallelStream()
                        .map(zeitraum -> {
                            // Mappt die geladenen Daten auf ein eigenes Objekt und reichert dieses mit den Informationen
                            // über den geladenen Zeitraum und die MstId an.
                            final var tagesaggregate = messwerteService.ladeTagesaggregate(options.getTagesTyp(), messstelleAuswertungIdDTO.getMqIds(),
                                    zeitraum);
                            return auswertungMapper.tagesaggregatDto2AuswertungProMessstelleUndZeitraum(tagesaggregate,
                                    zeitraum, messstelleAuswertungIdDTO.getMstId());
                        }))
                .collect(Collectors.groupingByConcurrent(AuswertungProMessstelleUndZeitraum::getMstId));
        return mapAuswertungMapToListOfAuswertungProMessstelle(auswertungenGroupedByMstId);
    }

    /**
     * Erzeugt aus den übergebenen Parametern eine Liste mit Zeiträumen für die die Daten geladen werden
     * sollen.
     *
     * @param auswertungszeitraeume Liste an Auswertungszeiträumen, z.B. Januar oder Quartal_1 für die
     *            die Daten geladen werden sollen
     * @param jahre Liste an Jahren für die die Daten geladen werden sollen
     * @return Liste der Zeiträume
     */
    protected List<Zeitraum> createZeitraeume(final List<AuswertungsZeitraum> auswertungszeitraeume, final List<Integer> jahre) {
        return ListUtils.emptyIfNull(auswertungszeitraeume)
                .stream()
                .flatMap(auswertungsZeitraum -> ListUtils.emptyIfNull(jahre)
                        .stream()
                        // erzeugt für jedes Jahr im Auswertungszeitraum ein Objekt vom Typ Zeitraum
                        .map(jahr -> new Zeitraum(
                                YearMonth.of(jahr, auswertungsZeitraum.getZeitraumStart().getMonth()),
                                YearMonth.of(jahr, auswertungsZeitraum.getZeitraumEnd().getMonth()),
                                auswertungsZeitraum)))
                .toList();
    }

    /**
     * Wandelt die als Map vorliegenden Daten in eine Liste mit den Auswertungen pro Messstelle um.
     * Die Daten liegen pro Messquerschnitt und pro Zeitraum in einer flachen Liste vor und müssen
     * anhand der MQ-Id gruppiert werden, damit pro Messstelle ein Objekt geliefert werden kann,
     * welches die geladenen Daten in einer Liste pro Zeitraum über alle Messquerschnitte beinhaltet,
     * sowie pro Messquerschnitt eine Liste an Daten pro Zeitraum.
     *
     * @param auswertungenGroupedByMstId Map mit den nach der MessstellenId gruppierten Daten
     * @return Liste mit der Auswertung pro Messstelle
     */
    protected List<AuswertungProMessstelle> mapAuswertungMapToListOfAuswertungProMessstelle(
            final ConcurrentMap<String, List<AuswertungProMessstelleUndZeitraum>> auswertungenGroupedByMstId) {
        final List<AuswertungProMessstelle> auswertungen = new ArrayList<>();
        auswertungenGroupedByMstId.forEach((mstId, auswertungenProMessstelleUndZeitraum) -> {
            // Pro Messstelle wird ein Objekt erzeugt
            final var auswertungProMessstelle = new AuswertungProMessstelle();
            auswertungProMessstelle.setMstId(mstId);
            // Pro ausgewertetem Zeitraum einer Messstelle werden die Daten auf ein neues Objekt
            // gemappt
            auswertungenProMessstelleUndZeitraum.forEach(auswertungProMessstelleUndZeitraum -> {
                final var auswertung = new Auswertung();
                auswertung.setObjectId(mstId);
                auswertung.setZeitraum(auswertungProMessstelleUndZeitraum.getZeitraum());
                auswertung.setDaten(auswertungProMessstelleUndZeitraum.getMeanOverAllAggregatesOfAllMqId());
                auswertungProMessstelle.getAuswertungenProZeitraum().add(auswertung);
                final List<TagesaggregatDto> meanOfAggregatesForEachMqId = ListUtils
                        .emptyIfNull(auswertungProMessstelleUndZeitraum.getMeanOfAggregatesForEachMqId());
                meanOfAggregatesForEachMqId.sort(Comparator.comparing(TagesaggregatDto::getMqId));
                // Pro Messquerschnitt einer Messstelle werden die Daten ebenfalls pro Zeitraum auf ein
                // neues Objekt gemapt und in einer Map abgelegt
                meanOfAggregatesForEachMqId.forEach(tagesaggregatDto -> {
                    final var auswertungMq = new Auswertung();
                    final var mqIdAsString = String.valueOf(tagesaggregatDto.getMqId());
                    auswertungMq.setObjectId(mqIdAsString);
                    auswertungMq.setZeitraum(auswertungProMessstelleUndZeitraum.getZeitraum());
                    auswertungMq.setDaten(tagesaggregatDto);
                    // Erzeugt für jeden geladenen Messquerschnitt einen eigenen Eintrag in der Map,
                    // um die geladenen Daten pro Zeitraum abzulegen
                    if (!auswertungProMessstelle.getAuswertungenProMq().containsKey(mqIdAsString)) {
                        auswertungProMessstelle.getAuswertungenProMq().put(mqIdAsString, new ArrayList<>());
                    }
                    auswertungProMessstelle.getAuswertungenProMq().get(mqIdAsString).add(auswertungMq);
                });
            });
            auswertungen.add(auswertungProMessstelle);
        });
        return auswertungen;
    }
}
