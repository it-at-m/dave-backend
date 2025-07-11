package de.muenchen.dave.services.messstelle.auswertung;

import de.muenchen.dave.configuration.LogExecutionTime;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatenSteplineDTO;
import de.muenchen.dave.domain.dtos.messstelle.FahrzeugOptionsDTO;
import de.muenchen.dave.domain.dtos.messstelle.ReadMessfaehigkeitDTO;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.Auswertung;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.AuswertungMessstelle;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.AuswertungMessstelleUndZeitraum;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.AuswertungMessstelleWithFileDTO;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.MessstelleAuswertungDTO;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.MessstelleAuswertungOptionsDTO;
import de.muenchen.dave.domain.enums.AuswertungsZeitraum;
import de.muenchen.dave.domain.enums.Fahrzeugklasse;
import de.muenchen.dave.domain.enums.TagesTyp;
import de.muenchen.dave.domain.mapper.detektor.AuswertungMapper;
import de.muenchen.dave.domain.model.messstelle.ValidateZeitraumAndTagesTypForMessstelleModel;
import de.muenchen.dave.geodateneai.gen.model.TagesaggregatDto;
import de.muenchen.dave.geodateneai.gen.model.TagesaggregatResponseDto;
import de.muenchen.dave.services.messstelle.MessstelleService;
import de.muenchen.dave.services.messstelle.MesswerteService;
import de.muenchen.dave.services.messstelle.ValidierungService;
import de.muenchen.dave.services.messstelle.Zeitraum;
import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class AuswertungService {

    private final MessstelleService messstelleService;

    private final MesswerteService messwerteService;

    private final AuswertungMapper auswertungMapper;

    private final SpreadsheetService spreadsheetService;

    private final GanglinieGesamtauswertungService ganglinieGesamtauswertungService;

    private final ValidierungService validierungService;

    public List<MessstelleAuswertungDTO> getAllVisibleMessstellen() {
        return messstelleService.getAllVisibleMessstellenForAuswertungOrderByMstIdAsc();
    }

    /**
     * Ermittelt je Messstelle die in Zeiträume unterteilten Zähldaten.
     *
     * Die Zähldaten werden für die Darstellungen in einem Gangliniendiagramm aufbereitet.
     * Zusätzlich werden die Informationen als Tabellenkalkulationsdatei bereitgestellt.
     *
     * @param options zum Laden und Aufbereiten der Messstelleninformationen mit Zähldaten.
     * @return die Zähldaten der Messstelleninformationen mitsamt der Tabellenkalkulationsdatei.
     * @throws IOException
     */
    @LogExecutionTime
    public AuswertungMessstelleWithFileDTO ladeAuswertungMessstellen(final MessstelleAuswertungOptionsDTO options) throws IOException {
        log.debug("#ladeAuswertungMessstellen {}", options);
        final var auswertungMessstellen = new AuswertungMessstelleWithFileDTO();
        final var auswertungenMqByMstId = this.ladeAuswertungGroupedByMstId(options);
        final var zaehldatenMessstellen = this.createZaehldatenForGanglinie(options.getFahrzeuge(), auswertungenMqByMstId);
        auswertungMessstellen.setZaehldatenMessstellen(zaehldatenMessstellen);
        final var spreadsheet = this.createAuswertungMessstellenSpreadsheet(options, auswertungenMqByMstId);
        final var spreadsheetBase64Encoded = Base64.getEncoder().encodeToString(spreadsheet);
        auswertungMessstellen.setSpreadsheetBase64Encoded(spreadsheetBase64Encoded);
        return auswertungMessstellen;
    }

    /**
     *
     * @param options
     * @return
     * @throws IOException
     */
    @LogExecutionTime
    public AuswertungMessstelleWithFileDTO ladeAuswertungMessstellenNg(final MessstelleAuswertungOptionsDTO options) throws IOException {

        log.debug("#ladeAuswertungMessstellen {}", options);
        final var auswertungMessstellen = new AuswertungMessstelleWithFileDTO();
        final var auswertungenMqByMstId = this.ladeAuswertungGroupedByMstId(options);
        final var zaehldatenMessstellen = this.createZaehldatenForGanglinie(options.getFahrzeuge(), auswertungenMqByMstId);
        auswertungMessstellen.setZaehldatenMessstellen(zaehldatenMessstellen);
        final var spreadsheet = this.createAuswertungMessstellenSpreadsheet(options, auswertungenMqByMstId);
        final var spreadsheetBase64Encoded = Base64.getEncoder().encodeToString(spreadsheet);
        auswertungMessstellen.setSpreadsheetBase64Encoded(spreadsheetBase64Encoded);
        return auswertungMessstellen;
    }

    /**
     * Bereitet die im Parameter gegebenen Zähldaten für die Gangliniendarstellung auf.
     *
     * @param fahrzeugOptions die Optionen zur Aufbereitung der Zähldaten für die Gangliniendarstellung.
     * @param auswertungenMessstellen die Auswertungen der Messstellen zur Aufbereitung für die
     *            Gangliniendarstellung.
     * @return die aufbereiteten Daten für die Gangliniendarstellung.
     */
    protected LadeZaehldatenSteplineDTO createZaehldatenForGanglinie(
            final FahrzeugOptionsDTO fahrzeugOptions,
            final List<AuswertungMessstelle> auswertungenMessstellen) {
        final var auswertungenProMessstelle = ListUtils.emptyIfNull(auswertungenMessstellen);
        if (auswertungenProMessstelle.size() == 1) {
            return ganglinieGesamtauswertungService.createGanglinieForSingleMessstelle(auswertungenProMessstelle.getFirst(), fahrzeugOptions);
        } else {
            return ganglinieGesamtauswertungService.createGanglinieForMultipleMessstellen(auswertungenProMessstelle, fahrzeugOptions);
        }
    }

    /**
     * Erzeugt mittels der geladenen Daten eine Datei für die Auswertung
     *
     * @param options Optionen für die Auswertung
     * @param auswertungenProMessstelle ausgewerteten Daten. Die Sortierung des Attributs und der darin
     *            enthaltenen Unterattribute
     *            bildet sich ebenfalls in der erstellen Datei ab.
     * @return Auswertungsdatei als byte[]
     * @throws IOException kann beim Erstellen des byte[] geworfen werden. Fehlerbehandlung erfolgt im
     *             Controller
     */
    protected byte[] createAuswertungMessstellenSpreadsheet(
            final MessstelleAuswertungOptionsDTO options,
            final List<AuswertungMessstelle> auswertungenProMessstelle) throws IOException {
        if (CollectionUtils.isEmpty(options.getMessstelleAuswertungIds())) {
            throw new IllegalArgumentException("Es wurden keine Messstellen ausgewählt.");
        }

        return spreadsheetService.createSpreadsheetForMessstellen(auswertungenProMessstelle, options);
    }

    /**
     * Lädt die Daten pro Messstelle je Zeitraum.
     *
     * @param options Definierte Optionen zum Laden der Daten
     * @return Liste an Auswertungen Pro Messstelle
     */
    protected List<AuswertungMessstelle> ladeAuswertungGroupedByMstId(final MessstelleAuswertungOptionsDTO options) {

        // Pro Jahr + Zeitintervall, z.B. Januar ein eintrag in der Liste
        final List<Zeitraum> zeitraeume = this.createZeitraeume(options.getZeitraum(), options.getJahre());

        final ConcurrentMap<String, List<AuswertungMessstelleUndZeitraum>> auswertungenGroupedByMstId = CollectionUtils
                // Lädt die Daten pro Messstelle
                .emptyIfNull(options.getMessstelleAuswertungIds())
                .parallelStream()
                // Lädt die Daten einer Messstelle pro Zeitraum
                .flatMap(messstelleAuswertungIdDTO -> CollectionUtils.emptyIfNull(zeitraeume)
                        .parallelStream()
                        .map(zeitraum -> {
                            final var model = createValidateZeitraumAndTagesTyp(
                                    messstelleAuswertungIdDTO.getMstId(),
                                    messstelleAuswertungIdDTO.getMqIds(),
                                    zeitraum,
                                    options.getTagesTyp());
                            final TagesaggregatResponseDto tagesaggregatResponse;
                            if (validierungService.isZeitraumAndTagestypValid(model)) {
                                tagesaggregatResponse = messwerteService.ladeMeanOfTagesaggregatePerMq(
                                        options.getTagesTyp(),
                                        messstelleAuswertungIdDTO.getMqIds(),
                                        List.of(List.of(zeitraum.getAuswertungsZeitraum().getZeitraumStart(),
                                                zeitraum.getAuswertungsZeitraum().getZeitraumEnd())));
                            } else {
                                tagesaggregatResponse = new TagesaggregatResponseDto();
                                final var emptyTagesaggregate = new ArrayList<TagesaggregatDto>();
                                messstelleAuswertungIdDTO.getMqIds().forEach(mqId -> {
                                    final TagesaggregatDto tagesaggregatDto = new TagesaggregatDto();
                                    tagesaggregatDto.setMqId(Integer.valueOf(mqId));
                                    emptyTagesaggregate.add(tagesaggregatDto);
                                });
                                tagesaggregatResponse.setMeanOfAggregatesForEachMqId(emptyTagesaggregate);
                                tagesaggregatResponse.setSumOverAllAggregatesOfAllMqId(new TagesaggregatDto());
                            }
                            // Mappt die geladenen Daten auf ein eigenes Objekt und reichert dieses mit den Informationen
                            // über den geladenen Zeitraum und die MstId an.
                            return auswertungMapper.tagesaggregatDto2AuswertungProMessstelleUndZeitraum(
                                    tagesaggregatResponse,
                                    zeitraum,
                                    messstelleAuswertungIdDTO.getMstId());
                        }))
                .collect(Collectors.groupingByConcurrent(AuswertungMessstelleUndZeitraum::getMstId));
        return mapAuswertungMapToListOfAuswertungProMessstelle(auswertungenGroupedByMstId);
    }

    /**
     * Lädt die Daten pro Messstelle je Zeitraum.
     *
     * @param options Definierte Optionen zum Laden der Daten
     * @return Liste an Auswertungen Pro Messstelle
     */
    protected List<AuswertungMessstelle> ladeAuswertungGroupedByMstIdNg(final MessstelleAuswertungOptionsDTO options) {

        // Pro Jahr + Zeitintervall, z.B. Januar ein eintrag in der Liste
        final List<Zeitraum> zeitraeume = this.createZeitraeume(options.getZeitraum(), options.getJahre());

        final ConcurrentMap<String, List<AuswertungMessstelleUndZeitraum>> auswertungenGroupedByMstId = CollectionUtils
                // Lädt die Daten pro Messstelle
                .emptyIfNull(options.getMessstelleAuswertungIds())
                .parallelStream()
                .flatMap(messstelleAuswertungIdDTO -> CollectionUtils.emptyIfNull(zeitraeume)
                        .parallelStream()
                        .map(zeitraum -> createValidateZeitraumAndTagesTyp(
                                messstelleAuswertungIdDTO.getMstId(),
                                messstelleAuswertungIdDTO.getMqIds(),
                                zeitraum,
                                options.getTagesTyp())))
                .map(validateZeitraumAndTagesTypForMessstelle -> {
                    final var zeitraum = validateZeitraumAndTagesTypForMessstelle.getZeitraum();
                    final var requestedZeitraum = new ArrayList<LocalDate>();
                    requestedZeitraum.add(LocalDate.of(zeitraum.getStart().getYear(), zeitraum.getStart().getMonthValue(), 1));
                    requestedZeitraum.add(LocalDate.of(zeitraum.getEnd().getYear(), zeitraum.getEnd().getMonthValue(),
                            zeitraum.getEnd().atEndOfMonth().getDayOfMonth()));
                    var fahrzeugklasseAccordingChoosenFahrzeugoptions = validierungService
                            .getFahrzeugklasseAccordingChoosenFahrzeugoptions(options.getFahrzeuge());
                    var relevantMessfaehigkeiten = validierungService.getRelevantMessfaehigkeitenAccordingFahrzeugklasse(
                            validateZeitraumAndTagesTypForMessstelle, fahrzeugklasseAccordingChoosenFahrzeugoptions);

                    TagesaggregatResponseDto tagesaggregatResponse = null;
                    if (Fahrzeugklasse.ACHT_PLUS_EINS.equals(fahrzeugklasseAccordingChoosenFahrzeugoptions)) {
                        relevantMessfaehigkeiten = validierungService.getRelevantMessfaehigkeitenAccordingFahrzeugklasse(
                                validateZeitraumAndTagesTypForMessstelle, fahrzeugklasseAccordingChoosenFahrzeugoptions);

                        final var zeitraumeOfRelevantMessfaehigkeiten = getZeitraeumeOfGivenMessfaehigkeiten(relevantMessfaehigkeiten);
                        final var isValid = validierungService.areZeitraeumeAndTagesTypForMessstelleValid(
                                validateZeitraumAndTagesTypForMessstelle.getMstId(),
                                zeitraumeOfRelevantMessfaehigkeiten,
                                validateZeitraumAndTagesTypForMessstelle.getTagesTyp());
                        if (isValid) {
                            tagesaggregatResponse = messwerteService.ladeMeanOfTagesaggregatePerMq(
                                    options.getTagesTyp(),
                                    validateZeitraumAndTagesTypForMessstelle.getMqIds(),
                                    zeitraumeOfRelevantMessfaehigkeiten);
                        } else {
                            fahrzeugklasseAccordingChoosenFahrzeugoptions = Fahrzeugklasse.ZWEI_PLUS_EINS;
                        }
                    }

                    if (Fahrzeugklasse.ZWEI_PLUS_EINS.equals(fahrzeugklasseAccordingChoosenFahrzeugoptions)) {
                        relevantMessfaehigkeiten = validierungService.getRelevantMessfaehigkeitenAccordingFahrzeugklasse(
                                validateZeitraumAndTagesTypForMessstelle, fahrzeugklasseAccordingChoosenFahrzeugoptions);

                        final var zeitraumeOfRelevantMessfaehigkeiten = getZeitraeumeOfGivenMessfaehigkeiten(relevantMessfaehigkeiten);
                        final var isValid = validierungService.areZeitraeumeAndTagesTypForMessstelleValid(
                                validateZeitraumAndTagesTypForMessstelle.getMstId(),
                                zeitraumeOfRelevantMessfaehigkeiten,
                                validateZeitraumAndTagesTypForMessstelle.getTagesTyp());
                        if (isValid) {
                            tagesaggregatResponse = messwerteService.ladeMeanOfTagesaggregatePerMq(
                                    options.getTagesTyp(),
                                    validateZeitraumAndTagesTypForMessstelle.getMqIds(),
                                    zeitraumeOfRelevantMessfaehigkeiten);
                        } else {
                            fahrzeugklasseAccordingChoosenFahrzeugoptions = Fahrzeugklasse.SUMME_KFZ;
                        }
                    }

                    if (Fahrzeugklasse.SUMME_KFZ.equals(fahrzeugklasseAccordingChoosenFahrzeugoptions)) {
                        relevantMessfaehigkeiten = validierungService.getRelevantMessfaehigkeitenAccordingFahrzeugklasse(
                                validateZeitraumAndTagesTypForMessstelle, fahrzeugklasseAccordingChoosenFahrzeugoptions);

                        final var zeitraumeOfRelevantMessfaehigkeiten = getZeitraeumeOfGivenMessfaehigkeiten(relevantMessfaehigkeiten);
                        final var isValid = validierungService.areZeitraeumeAndTagesTypForMessstelleValid(
                                validateZeitraumAndTagesTypForMessstelle.getMstId(),
                                zeitraumeOfRelevantMessfaehigkeiten,
                                validateZeitraumAndTagesTypForMessstelle.getTagesTyp());
                        if (isValid) {
                            tagesaggregatResponse = messwerteService.ladeMeanOfTagesaggregatePerMq(
                                    options.getTagesTyp(),
                                    validateZeitraumAndTagesTypForMessstelle.getMqIds(),
                                    zeitraumeOfRelevantMessfaehigkeiten);
                        }
                    }

                    if (Objects.isNull(tagesaggregatResponse)) {
                        tagesaggregatResponse = createEmptyTagesaggregatResponse(validateZeitraumAndTagesTypForMessstelle.getMqIds());
                    }

                    // Mappt die geladenen Daten auf ein eigenes Objekt und reichert dieses mit den Informationen
                    // über den geladenen Zeitraum und die MstId an.
                    return auswertungMapper.tagesaggregatDto2AuswertungProMessstelleUndZeitraum(
                            tagesaggregatResponse,
                            zeitraum,
                            validateZeitraumAndTagesTypForMessstelle.getMstId());

                })
                .collect(Collectors.groupingByConcurrent(AuswertungMessstelleUndZeitraum::getMstId));
        return mapAuswertungMapToListOfAuswertungProMessstelle(auswertungenGroupedByMstId);
    }

    /**
     * Erzeugt aus den übergebenen Parametern ein Objekt welches als Basis für die Validierung bezüglich
     * Zeitraum, Tagestyp und Messfähigkeiten dient.
     *
     * @param mstId ID der angefragten Messstelle
     * @param zeitraum angefragter Zeitraum
     * @param tagesTyp angefragter Tagestyp
     * @return ValidateZeitraumAndTagesTypForMessstelleModel
     */
    protected ValidateZeitraumAndTagesTypForMessstelleModel createValidateZeitraumAndTagesTyp(
            final String mstId,
            final Set<String> mqIds,
            final Zeitraum zeitraum,
            final TagesTyp tagesTyp) {
        final var model = new ValidateZeitraumAndTagesTypForMessstelleModel();

        model.setMstId(mstId);
        model.setMqIds(mqIds);
        model.setTagesTyp(tagesTyp);
        model.setZeitraum(zeitraum);

        final var messfaehigkeiten = messstelleService.getMessfaehigkeitenForZeitraumForMessstelle(
                mstId,
                zeitraum.getAuswertungsZeitraum().getZeitraumStart(),
                zeitraum.getAuswertungsZeitraum().getZeitraumEnd());
        model.setMessfaehigkeiten(messfaehigkeiten);

        return model;
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
    protected List<AuswertungMessstelle> mapAuswertungMapToListOfAuswertungProMessstelle(
            final ConcurrentMap<String, List<AuswertungMessstelleUndZeitraum>> auswertungenGroupedByMstId) {
        final List<AuswertungMessstelle> auswertungen = new ArrayList<>();

        auswertungenGroupedByMstId.forEach((mstId, auswertungenProMessstelleUndZeitraum) -> {
            // Pro Messstelle wird ein Objekt erzeugt
            final var auswertungProMessstelle = new AuswertungMessstelle();
            auswertungProMessstelle.setMstId(mstId);
            // Pro ausgewertetem Zeitraum einer Messstelle werden die Daten auf ein neues Objekt
            // gemappt
            auswertungenProMessstelleUndZeitraum.forEach(auswertungMessstelleUndZeitraum -> {
                final var auswertung = new Auswertung();
                auswertung.setObjectId(mstId);
                auswertung.setZeitraum(auswertungMessstelleUndZeitraum.getZeitraum());
                auswertung.setDaten(auswertungMessstelleUndZeitraum.getSumOverAllAggregatesOfAllMqId());
                auswertungProMessstelle.getAuswertungenProZeitraum().add(auswertung);
                final List<TagesaggregatDto> meanOfAggregatesForEachMqId = ListUtils
                        .emptyIfNull(auswertungMessstelleUndZeitraum.getMeanOfAggregatesForEachMqId());
                meanOfAggregatesForEachMqId.sort(Comparator.comparing(TagesaggregatDto::getMqId));
                // Pro Messquerschnitt einer Messstelle werden die Daten ebenfalls pro Zeitraum auf ein
                // neues Objekt gemapt und in einer Map abgelegt
                meanOfAggregatesForEachMqId.forEach(tagesaggregatDto -> {
                    final var auswertungMq = new Auswertung();
                    final var mqIdAsString = String.valueOf(tagesaggregatDto.getMqId());
                    auswertungMq.setObjectId(mqIdAsString);
                    auswertungMq.setZeitraum(auswertungMessstelleUndZeitraum.getZeitraum());
                    auswertungMq.setDaten(tagesaggregatDto);
                    // Erzeugt für jeden geladenen Messquerschnitt einen eigenen Eintrag in der Map,
                    // um die geladenen Daten pro Zeitraum abzulegen
                    if (!auswertungProMessstelle.getAuswertungenProMq().containsKey(mqIdAsString)) {
                        auswertungProMessstelle.getAuswertungenProMq().put(mqIdAsString, new ArrayList<>());
                    }
                    auswertungProMessstelle.getAuswertungenProMq().get(mqIdAsString).add(auswertungMq);
                });
            });

            // Sortierung nach Zeitraum.
            auswertungProMessstelle
                    .getAuswertungenProZeitraum()
                    .sort(Comparator.comparing(auswertung -> auswertung.getZeitraum().getStart()));
            auswertungProMessstelle
                    .getAuswertungenProMq()
                    .values()
                    .parallelStream()
                    .forEach(auswertungenMesstelleProZeitraum -> {
                        auswertungenMesstelleProZeitraum.sort(Comparator.comparing(auswertung -> auswertung.getZeitraum().getStart()));
                    });
            auswertungen.add(auswertungProMessstelle);
        });

        // Sortierung nach Messtelle
        auswertungen.sort(Comparator.comparing(AuswertungMessstelle::getMstId));
        return auswertungen;
    }

    protected List<List<LocalDate>> getZeitraeumeOfGivenMessfaehigkeiten(final List<ReadMessfaehigkeitDTO> messfaehigkeiten) {
        return CollectionUtils.emptyIfNull(messfaehigkeiten)
                .stream()
                .map(messfaehigkeit -> List.of(LocalDate.parse(messfaehigkeit.getGueltigAb()), LocalDate.parse(messfaehigkeit.getGueltigBis())))
                .toList();
    }

    protected FahrzeugOptionsDTO getAdaptedFahrzeugOptionsAccordingFahrzeugklasseAndGivenFahrzeugOptions(
            final Fahrzeugklasse fahrzeugklasse,
            final FahrzeugOptionsDTO fahrzeugOptions) {
        final FahrzeugOptionsDTO adaptedFahrzeugOptions = auswertungMapper.deepCopyOf(fahrzeugOptions);
        if (Fahrzeugklasse.ACHT_PLUS_EINS.equals(fahrzeugklasse)) {
            return adaptedFahrzeugOptions;
        } else if (Fahrzeugklasse.ZWEI_PLUS_EINS.equals(fahrzeugklasse)) {
            fahrzeugOptions.setGueterverkehr(false);
            fahrzeugOptions.setGueterverkehrsanteilProzent(false);
            fahrzeugOptions.setLastkraftwagen(false);
            fahrzeugOptions.setLastzuege(false);
            fahrzeugOptions.setBusse(false);
            fahrzeugOptions.setKraftraeder(false);
            fahrzeugOptions.setPersonenkraftwagen(false);
            fahrzeugOptions.setLieferwagen(false);
        } else if (Fahrzeugklasse.SUMME_KFZ.equals(fahrzeugklasse)) {
            fahrzeugOptions.setSchwerverkehr(false);
            fahrzeugOptions.setSchwerverkehrsanteilProzent(false);
            fahrzeugOptions.setGueterverkehr(false);
            fahrzeugOptions.setGueterverkehrsanteilProzent(false);
            fahrzeugOptions.setLastkraftwagen(false);
            fahrzeugOptions.setLastzuege(false);
            fahrzeugOptions.setBusse(false);
            fahrzeugOptions.setKraftraeder(false);
            fahrzeugOptions.setPersonenkraftwagen(false);
            fahrzeugOptions.setLieferwagen(false);
        } else {
            // RAD
            fahrzeugOptions.setKraftfahrzeugverkehr(false);
            fahrzeugOptions.setSchwerverkehr(false);
            fahrzeugOptions.setSchwerverkehrsanteilProzent(false);
            fahrzeugOptions.setGueterverkehr(false);
            fahrzeugOptions.setGueterverkehrsanteilProzent(false);
            fahrzeugOptions.setLastkraftwagen(false);
            fahrzeugOptions.setLastzuege(false);
            fahrzeugOptions.setBusse(false);
            fahrzeugOptions.setKraftraeder(false);
            fahrzeugOptions.setPersonenkraftwagen(false);
            fahrzeugOptions.setLieferwagen(false);
        }
        return adaptedFahrzeugOptions;
    }

    protected TagesaggregatResponseDto createEmptyTagesaggregatResponse(final Set<String> mqIds) {
        final var tagesaggregatResponse = new TagesaggregatResponseDto();
        final var emptyTagesaggregate = new ArrayList<TagesaggregatDto>();
        mqIds.forEach(mqId -> {
            final TagesaggregatDto tagesaggregatDto = new TagesaggregatDto();
            tagesaggregatDto.setMqId(Integer.valueOf(mqId));
            emptyTagesaggregate.add(tagesaggregatDto);
        });
        tagesaggregatResponse.setMeanOfAggregatesForEachMqId(emptyTagesaggregate);
        tagesaggregatResponse.setSumOverAllAggregatesOfAllMqId(new TagesaggregatDto());
        return tagesaggregatResponse;
    }
}
