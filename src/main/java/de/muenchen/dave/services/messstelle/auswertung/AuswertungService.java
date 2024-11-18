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

    @LogExecutionTime
    public byte[] createAuswertungsfile(final MessstelleAuswertungOptionsDTO options) throws IOException {
        log.info("#createAuswertungsfile {}", options);
        if (CollectionUtils.isEmpty(options.getMessstelleAuswertungIds())) {
            throw new IllegalArgumentException("Es wurden keine Messstellen ausgewählt.");
        }
        final var auswertungenMqByMstId = this.ladeAuswertungGroupedByMstId(options);
        return spreadsheetService.createFile(auswertungenMqByMstId, options);
    }

    protected List<AuswertungProMessstelle> ladeAuswertungGroupedByMstId(final MessstelleAuswertungOptionsDTO options) {

        final List<Zeitraum> zeitraeume = this.createZeitraeume(options.getZeitraum(), options.getJahre());

        final ConcurrentMap<String, List<AuswertungProMessstelleUndZeitraum>> auswertungenGroupedByMstId = CollectionUtils
                .emptyIfNull(options.getMessstelleAuswertungIds())
                .parallelStream()
                .flatMap(messstelleAuswertungIdDTO -> CollectionUtils.emptyIfNull(zeitraeume)
                        .parallelStream()
                        .map(zeitraum -> {
                            final var tagesaggregate = messwerteService.ladeTagesaggregate(options.getTagesTyp(), messstelleAuswertungIdDTO.getMqIds(),
                                    zeitraum);
                            return auswertungMapper.tagesaggregatDto2AuswertungProMessstelleUndZeitraum(tagesaggregate,
                                    zeitraum, messstelleAuswertungIdDTO.getMstId());
                        }))
                .collect(Collectors.groupingByConcurrent(AuswertungProMessstelleUndZeitraum::getMstId));
        return convertAuswertungen(auswertungenGroupedByMstId);
    }

    protected List<Zeitraum> createZeitraeume(final List<AuswertungsZeitraum> auswertungszeitraeume, final List<Integer> jahre) {
        return ListUtils.emptyIfNull(auswertungszeitraeume)
                .stream()
                .flatMap(auswertungsZeitraum -> ListUtils.emptyIfNull(jahre)
                        .stream()
                        .map(jahr -> new Zeitraum(
                                YearMonth.of(jahr, auswertungsZeitraum.getZeitraumStart().getMonth()),
                                YearMonth.of(jahr, auswertungsZeitraum.getZeitraumEnd().getMonth()),
                                auswertungsZeitraum)))
                .toList();
    }

    protected List<AuswertungProMessstelle> convertAuswertungen(
            final ConcurrentMap<String, List<AuswertungProMessstelleUndZeitraum>> auswertungenGroupedByMstId) {
        final List<AuswertungProMessstelle> auswertungen = new ArrayList<>();
        auswertungenGroupedByMstId.forEach((mstId, auswertungenProMessstelleUndZeitraum) -> {
            final AuswertungProMessstelle auswertungProMessstelle = new AuswertungProMessstelle();
            auswertungProMessstelle.setMstId(mstId);
            auswertungenProMessstelleUndZeitraum.forEach(auswertungProMessstelleUndZeitraum -> {
                final Auswertung auswertung = new Auswertung();
                auswertung.setObjectId(mstId);
                auswertung.setZeitraum(auswertungProMessstelleUndZeitraum.getZeitraum());
                auswertung.setDaten(auswertungProMessstelleUndZeitraum.getMeanOverAllAggregatesOfAllMqId());
                auswertungProMessstelle.getAuswertungenProZeitraum().add(auswertung);
                final List<TagesaggregatDto> meanOfAggregatesForEachMqId = ListUtils
                        .emptyIfNull(auswertungProMessstelleUndZeitraum.getMeanOfAggregatesForEachMqId());
                meanOfAggregatesForEachMqId.sort(Comparator.comparing(TagesaggregatDto::getMqId));
                meanOfAggregatesForEachMqId.forEach(tagesaggregatDto -> {
                    final Auswertung auswertungMq = new Auswertung();
                    String mqIdAsString = String.valueOf(tagesaggregatDto.getMqId());
                    auswertungMq.setObjectId(mqIdAsString);
                    auswertungMq.setZeitraum(auswertungProMessstelleUndZeitraum.getZeitraum());
                    auswertungMq.setDaten(tagesaggregatDto);
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
