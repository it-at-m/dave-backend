package de.muenchen.dave.services.ladezaehldaten;

import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatenTableDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatumDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatumTageswertDTO;
import de.muenchen.dave.domain.elasticsearch.PkwEinheit;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.enums.FahrbewegungKreisverkehr;
import de.muenchen.dave.domain.enums.TypeZeitintervall;
import de.muenchen.dave.domain.enums.Zaehlart;
import de.muenchen.dave.domain.enums.ZaehldatenIntervall;
import de.muenchen.dave.domain.enums.Zaehldauer;
import de.muenchen.dave.domain.enums.Zeitblock;
import de.muenchen.dave.exceptions.DataNotFoundException;
import de.muenchen.dave.repositories.relationaldb.ZeitintervallRepository;
import de.muenchen.dave.services.ZaehlstelleIndexService;
import de.muenchen.dave.util.CalculationUtil;
import de.muenchen.dave.util.dataimport.ZeitintervallSortingIndexUtil;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LadeZaehldatenService {

    public static final String ZEITAUSWAHL_SPITZENSTUNDE = "Spitzenstunde";
    public static final String ZEITAUSWAHL_SPITZENSTUNDE_KFZ = ZEITAUSWAHL_SPITZENSTUNDE + " KFZ";
    public static final String ZEITAUSWAHL_SPITZENSTUNDE_RAD = ZEITAUSWAHL_SPITZENSTUNDE + " Rad";
    public static final String ZEITAUSWAHL_SPITZENSTUNDE_FUSS = ZEITAUSWAHL_SPITZENSTUNDE + " Fuß";
    public static final String TAGESWERT = "Tageswert";
    public static final String GESAMT = "Gesamt";
    public static final String BLOCK = "Block";
    public static final String STUNDE = "Stunde";
    public static final String SPITZENSTUNDE_TAG = "SpStdTag";
    public static final String SPITZENSTUNDE_TAG_KFZ = SPITZENSTUNDE_TAG + " KFZ";
    public static final String SPITZENSTUNDE_TAG_RAD = SPITZENSTUNDE_TAG + " Rad";
    public static final String SPITZENSTUNDE_TAG_FUSS = SPITZENSTUNDE_TAG + " Fuß";
    public static final String SPITZENSTUNDE_BLOCK = "SpStdBlock";
    public static final String SPITZENSTUNDE_BLOCK_KFZ = SPITZENSTUNDE_BLOCK + " KFZ";
    public static final String SPITZENSTUNDE_BLOCK_RAD = SPITZENSTUNDE_BLOCK + " Rad";
    public static final String SPITZENSTUNDE_BLOCK_FUSS = SPITZENSTUNDE_BLOCK + " Fuß";
    private static final Set<Integer> SPITZENSTUNDEN_BLOCK_SORTING_INDEX = new HashSet<>();

    private final ZeitintervallRepository zeitintervallRepository;

    private final ZaehlstelleIndexService indexService;

    public LadeZaehldatenService(
            final ZeitintervallRepository zeitintervallRepository,
            final ZaehlstelleIndexService indexService) {
        this.zeitintervallRepository = zeitintervallRepository;
        this.indexService = indexService;
        // Kfz
        SPITZENSTUNDEN_BLOCK_SORTING_INDEX
                .add(ZeitintervallSortingIndexUtil.SORTING_INDEX_ZB_00_06 + ZeitintervallSortingIndexUtil.getSortingIndexSpitzenStundeWithinBlockKfz());
        SPITZENSTUNDEN_BLOCK_SORTING_INDEX
                .add(ZeitintervallSortingIndexUtil.SORTING_INDEX_ZB_06_10 + ZeitintervallSortingIndexUtil.getSortingIndexSpitzenStundeWithinBlockKfz());
        SPITZENSTUNDEN_BLOCK_SORTING_INDEX
                .add(ZeitintervallSortingIndexUtil.SORTING_INDEX_ZB_10_15 + ZeitintervallSortingIndexUtil.getSortingIndexSpitzenStundeWithinBlockKfz());
        SPITZENSTUNDEN_BLOCK_SORTING_INDEX
                .add(ZeitintervallSortingIndexUtil.SORTING_INDEX_ZB_15_19 + ZeitintervallSortingIndexUtil.getSortingIndexSpitzenStundeWithinBlockKfz());
        SPITZENSTUNDEN_BLOCK_SORTING_INDEX
                .add(ZeitintervallSortingIndexUtil.SORTING_INDEX_ZB_19_24 + ZeitintervallSortingIndexUtil.getSortingIndexSpitzenStundeWithinBlockKfz());
        // Rad
        SPITZENSTUNDEN_BLOCK_SORTING_INDEX
                .add(ZeitintervallSortingIndexUtil.SORTING_INDEX_ZB_00_06 + ZeitintervallSortingIndexUtil.getSortingIndexSpitzenStundeWithinBlockRad());
        SPITZENSTUNDEN_BLOCK_SORTING_INDEX
                .add(ZeitintervallSortingIndexUtil.SORTING_INDEX_ZB_06_10 + ZeitintervallSortingIndexUtil.getSortingIndexSpitzenStundeWithinBlockRad());
        SPITZENSTUNDEN_BLOCK_SORTING_INDEX
                .add(ZeitintervallSortingIndexUtil.SORTING_INDEX_ZB_10_15 + ZeitintervallSortingIndexUtil.getSortingIndexSpitzenStundeWithinBlockRad());
        SPITZENSTUNDEN_BLOCK_SORTING_INDEX
                .add(ZeitintervallSortingIndexUtil.SORTING_INDEX_ZB_15_19 + ZeitintervallSortingIndexUtil.getSortingIndexSpitzenStundeWithinBlockRad());
        SPITZENSTUNDEN_BLOCK_SORTING_INDEX
                .add(ZeitintervallSortingIndexUtil.SORTING_INDEX_ZB_19_24 + ZeitintervallSortingIndexUtil.getSortingIndexSpitzenStundeWithinBlockRad());
        // Fuss
        SPITZENSTUNDEN_BLOCK_SORTING_INDEX
                .add(ZeitintervallSortingIndexUtil.SORTING_INDEX_ZB_00_06 + ZeitintervallSortingIndexUtil.getSortingIndexSpitzenStundeWithinBlockFuss());
        SPITZENSTUNDEN_BLOCK_SORTING_INDEX
                .add(ZeitintervallSortingIndexUtil.SORTING_INDEX_ZB_06_10 + ZeitintervallSortingIndexUtil.getSortingIndexSpitzenStundeWithinBlockFuss());
        SPITZENSTUNDEN_BLOCK_SORTING_INDEX
                .add(ZeitintervallSortingIndexUtil.SORTING_INDEX_ZB_10_15 + ZeitintervallSortingIndexUtil.getSortingIndexSpitzenStundeWithinBlockFuss());
        SPITZENSTUNDEN_BLOCK_SORTING_INDEX
                .add(ZeitintervallSortingIndexUtil.SORTING_INDEX_ZB_15_19 + ZeitintervallSortingIndexUtil.getSortingIndexSpitzenStundeWithinBlockFuss());
        SPITZENSTUNDEN_BLOCK_SORTING_INDEX
                .add(ZeitintervallSortingIndexUtil.SORTING_INDEX_ZB_19_24 + ZeitintervallSortingIndexUtil.getSortingIndexSpitzenStundeWithinBlockFuss());
    }

    /**
     * Diese Methode gibt an ob ein Zeitintervall nach der Datenextraktion weiterverarbeitet werden
     * soll.
     *
     * @param zeitintervall welcher geprüft werden soll.
     * @param zeitblock der für die Prüfung benötigt wird.
     * @return false falls der Zeitintervall nicht weiterverarbeitet werden soll andernfalls true.
     */
    private static boolean shouldZeitintervallBeReturned(final Zeitintervall zeitintervall,
            final Zeitblock zeitblock) {
        boolean returnZeitintervall = (Zeitblock.ZB_00_24.equals(zeitblock) || zeitblock.getTypeZeitintervall().equals(TypeZeitintervall.BLOCK_SPEZIAL))
                || (zeitintervall.getSortingIndex() != ZeitintervallSortingIndexUtil.SORTING_INDEX_SPITZEN_STUNDE_DAY_KFZ
                        && zeitintervall.getSortingIndex() != ZeitintervallSortingIndexUtil.SORTING_INDEX_SPITZEN_STUNDE_DAY_RAD
                        && zeitintervall.getSortingIndex() != ZeitintervallSortingIndexUtil.SORTING_INDEX_SPITZEN_STUNDE_DAY_FUSS
                        && zeitintervall.getSortingIndex() != ZeitintervallSortingIndexUtil.SORTING_INDEX_GESAMT_DAY);
        if (zeitblock.getTypeZeitintervall().equals(TypeZeitintervall.BLOCK_SPEZIAL)
                && (SPITZENSTUNDEN_BLOCK_SORTING_INDEX.contains(zeitintervall.getSortingIndex()))) {
            returnZeitintervall = false;
        }
        return returnZeitintervall;
    }

    /**
     * In dieser Methode findet das Mapping eines {@link Zeitintervall}s nach {@link LadeZaehldatumDTO}
     * statt. Beim Mapping werden die Informationen in
     * {@link OptionsDTO} berücksichtigt.
     *
     * @param zeitintervall Der {@link Zeitintervall} zum Mapping nach {@link LadeZaehldatumDTO}.
     * @param pkwEinheit als {@link PkwEinheit}
     * @param options die {@link OptionsDTO} zur Berücksichtigung der Art und Weise des Mappings.
     * @return das gemappte {@link LadeZaehldatumDTO}.
     */
    public static LadeZaehldatumDTO mapToZaehldatum(final Zeitintervall zeitintervall,
            final PkwEinheit pkwEinheit,
            final OptionsDTO options) {
        final LadeZaehldatumDTO ladeZaehldatum;
        if (isZeitintervallForTageswert(zeitintervall, options)) {
            final LadeZaehldatumTageswertDTO ladeZaehldatumTageswert = new LadeZaehldatumTageswertDTO();
            ladeZaehldatumTageswert.setKfz(
                    ObjectUtils.defaultIfNull(
                            zeitintervall.getHochrechnung().getHochrechnungKfz(),
                            BigDecimal.ZERO).setScale(0, RoundingMode.HALF_UP));
            ladeZaehldatumTageswert.setGueterverkehr(
                    ObjectUtils.defaultIfNull(
                            zeitintervall.getHochrechnung().getHochrechnungGv(),
                            BigDecimal.ZERO).setScale(0, RoundingMode.HALF_UP));
            ladeZaehldatumTageswert.setSchwerverkehr(
                    ObjectUtils.defaultIfNull(
                            zeitintervall.getHochrechnung().getHochrechnungSv(),
                            BigDecimal.ZERO).setScale(0, RoundingMode.HALF_UP));

            ladeZaehldatumTageswert.setFahrradfahrer(
                    ObjectUtils.defaultIfNull(
                            zeitintervall.getHochrechnung().getHochrechnungRad(),
                            0));
            ladeZaehldatumTageswert.setType(TAGESWERT);
            ladeZaehldatum = ladeZaehldatumTageswert;
        } else {
            ladeZaehldatum = new LadeZaehldatumDTO();
            ladeZaehldatum.setPkw(zeitintervall.getPkw());
            ladeZaehldatum.setLkw(zeitintervall.getLkw());
            ladeZaehldatum.setLastzuege(zeitintervall.getLastzuege());
            ladeZaehldatum.setBusse(zeitintervall.getBusse());
            ladeZaehldatum.setKraftraeder(zeitintervall.getKraftraeder());
            ladeZaehldatum.setFahrradfahrer(zeitintervall.getFahrradfahrer());
            ladeZaehldatum.setFussgaenger(zeitintervall.getFussgaenger());
            ladeZaehldatum.setPkwEinheiten(
                    CalculationUtil.calculatePkwEinheiten(ladeZaehldatum, pkwEinheit));
            if (TypeZeitintervall.GESAMT.equals(zeitintervall.getType())) {
                ladeZaehldatum.setType(GESAMT);
            }
        }
        ladeZaehldatum.setStartUhrzeit(zeitintervall.getStartUhrzeit().toLocalTime());
        ladeZaehldatum.setEndeUhrzeit(zeitintervall.getEndeUhrzeit().toLocalTime());
        if (TypeZeitintervall.STUNDE_KOMPLETT.equals(zeitintervall.getType())
                && !ZaehldatenIntervall.STUNDE_KOMPLETT.equals(options.getIntervall())) {
            ladeZaehldatum.setType(STUNDE);
        }
        if (TypeZeitintervall.SPITZENSTUNDE_KFZ.equals(zeitintervall.getType())) {
            if (zeitintervall.getSortingIndex().equals(ZeitintervallSortingIndexUtil.SORTING_INDEX_SPITZEN_STUNDE_DAY_KFZ)) {
                ladeZaehldatum.setType(SPITZENSTUNDE_TAG_KFZ);
            } else {
                ladeZaehldatum.setType(SPITZENSTUNDE_BLOCK_KFZ);
            }
        }
        if (TypeZeitintervall.SPITZENSTUNDE_RAD.equals(zeitintervall.getType())) {
            if (zeitintervall.getSortingIndex().equals(ZeitintervallSortingIndexUtil.SORTING_INDEX_SPITZEN_STUNDE_DAY_RAD)) {
                ladeZaehldatum.setType(SPITZENSTUNDE_TAG_RAD);
            } else {
                ladeZaehldatum.setType(SPITZENSTUNDE_BLOCK_RAD);
            }
        }
        if (TypeZeitintervall.SPITZENSTUNDE_FUSS.equals(zeitintervall.getType())) {
            if (zeitintervall.getSortingIndex().equals(ZeitintervallSortingIndexUtil.SORTING_INDEX_SPITZEN_STUNDE_DAY_FUSS)) {
                ladeZaehldatum.setType(SPITZENSTUNDE_TAG_FUSS);
            } else {
                ladeZaehldatum.setType(SPITZENSTUNDE_BLOCK_FUSS);
            }
        }
        if (TypeZeitintervall.BLOCK.equals(zeitintervall.getType())
                || TypeZeitintervall.BLOCK_SPEZIAL.equals(zeitintervall.getType())) {
            ladeZaehldatum.setType(BLOCK);
        }
        return ladeZaehldatum;
    }

    /**
     * Anhand der im Parameter übergebenen {@link OptionsDTO} werden die {@link TypeZeitintervall}e
     * ermittelt, um die korrekten {@link Zeitintervall}e aus der
     * Datenbank extrahieren zu können.
     *
     * @param options zur Bestimmung der {@link TypeZeitintervall}e
     * @return {@link TypeZeitintervall}e für eine korrekte Datenextraktion.
     */
    public static Set<TypeZeitintervall> getTypesAccordingChosenOptions(final OptionsDTO options) {
        final Set<TypeZeitintervall> types = new HashSet<>();
        if (StringUtils.equals(options.getZeitauswahl(), ZEITAUSWAHL_SPITZENSTUNDE_KFZ)) {
            types.add(TypeZeitintervall.SPITZENSTUNDE_KFZ);
        } else if (StringUtils.equals(options.getZeitauswahl(), ZEITAUSWAHL_SPITZENSTUNDE_RAD)) {
            types.add(TypeZeitintervall.SPITZENSTUNDE_RAD);
        } else if (StringUtils.equals(options.getZeitauswahl(), ZEITAUSWAHL_SPITZENSTUNDE_FUSS)) {
            types.add(TypeZeitintervall.SPITZENSTUNDE_FUSS);
        } else {
            if (ZaehldatenIntervall.STUNDE_VIERTEL.equals(options.getIntervall())) {
                types.add(TypeZeitintervall.STUNDE_VIERTEL);
            }
            if (ZaehldatenIntervall.STUNDE_HALB.equals(options.getIntervall())) {
                types.add(TypeZeitintervall.STUNDE_HALB);
            }
            if (ZaehldatenIntervall.STUNDE_KOMPLETT.equals(options.getIntervall())
                    || (!ZaehldatenIntervall.STUNDE_KOMPLETT.equals(options.getIntervall())
                            && BooleanUtils.isTrue(options.getStundensumme()))) {
                types.add(TypeZeitintervall.STUNDE_KOMPLETT);
            }
            if (BooleanUtils.isTrue(options.getSpitzenstunde())) {
                if (BooleanUtils.isTrue(options.getSpitzenstundeKfz())) {
                    types.add(TypeZeitintervall.SPITZENSTUNDE_KFZ);
                }
                if (BooleanUtils.isTrue(options.getSpitzenstundeRad())) {
                    types.add(TypeZeitintervall.SPITZENSTUNDE_RAD);
                }
                if (BooleanUtils.isTrue(options.getSpitzenstundeFuss())) {
                    types.add(TypeZeitintervall.SPITZENSTUNDE_FUSS);
                }
            }
            if (BooleanUtils.isTrue(options.getBlocksumme())
                    && options.getZeitblock().getTypeZeitintervall().equals(TypeZeitintervall.BLOCK_SPEZIAL)) {
                /*
                 * Erforderlich falls als Zeitblock {@link Zeitblock#ZB_06_19}
                 * oder {@link Zeitblock#ZB_06_22} gewählt wurde.
                 */
                types.add(TypeZeitintervall.BLOCK_SPEZIAL);
            } else if (BooleanUtils.isTrue(options.getBlocksumme())) {
                types.add(TypeZeitintervall.BLOCK);
            }
            if (BooleanUtils.isTrue(options.getTagessumme())
                    && !options.getZeitblock().getTypeZeitintervall().equals(TypeZeitintervall.BLOCK_SPEZIAL)) {
                types.add(TypeZeitintervall.GESAMT);
            }
        }
        return types;
    }

    public static boolean isZeitintervallForTageswert(final Zeitintervall zeitintervall,
            final OptionsDTO options) {
        return TypeZeitintervall.GESAMT.equals(zeitintervall.getType())
                && !Zaehldauer.DAUER_24_STUNDEN.equals(options.getZaehldauer());
    }

    /**
     * Diese Methode erzeugt auf Basis der gewählten Verkehrsbeziehung sowie Bezeichners für Kreuzung
     * und
     * Kreisverkehr die für die Datenextraktion relevante
     * {@link FahrbewegungKreisverkehr}.
     *
     * @param von als Startknotenarm.
     * @param nach als Zielknotenarm
     * @param isKreisverkehr bezeichner ob erzeugung für Kreuzung oder Kreisverkehr.
     * @return null falls es sich um eine Kreuzung oder um einen Kreisverkehr mit
     *         Verkehrsbeziehungsauswahl
     *         "alle nach alle" handelt.
     *         {@link FahrbewegungKreisverkehr#HINEIN} falls es sich um eine Verkehrsbeziehungsauswahl
     *         mit
     *         "X nach alle" handelt.
     *         {@link FahrbewegungKreisverkehr#HERAUS} falls es sich um eine Verkehrsbeziehungsauswahl
     *         mit
     *         "alle nach X" handelt.
     */
    public static FahrbewegungKreisverkehr createFahrbewegungKreisverkehr(final Integer von,
            final Integer nach,
            final Boolean isKreisverkehr) {
        final FahrbewegungKreisverkehr fahrbewegungKreisverkehr;
        if (isKreisverkehr) {
            if (ObjectUtils.isNotEmpty(von) && ObjectUtils.isEmpty(nach)) {
                fahrbewegungKreisverkehr = FahrbewegungKreisverkehr.HINEIN;
            } else if (ObjectUtils.isEmpty(von) && ObjectUtils.isNotEmpty(nach)) {
                fahrbewegungKreisverkehr = FahrbewegungKreisverkehr.HERAUS;
            } else {
                fahrbewegungKreisverkehr = null;
            }
        } else {
            fahrbewegungKreisverkehr = null;
        }
        return fahrbewegungKreisverkehr;
    }

    /**
     * Diese Methode extrahiert die {@link Zeitintervall}e aus der Datenbank entsprechend der in den
     * {@link OptionsDTO} vorhandenen Informationen und der
     * Zaehlungs-ID. Die aus der Datenbank extrahierten Daten werden anschließend nach
     * {@link LadeZaehldatumDTO} gemappt. Schlussendlich werden alle
     * {@link LadeZaehldatumDTO} im Objekt {@link LadeZaehldatenTableDTO} zurückgegeben.
     *
     * @param zaehlungId zur Extraktion der {@link Zeitintervall}e aus der Datenbank.
     * @param options zur Extraktion der {@link Zeitintervall}e aus der Datenbank und zum Mapping nach
     *            {@link LadeZaehldatumDTO}.
     * @return ein {@link LadeZaehldatenTableDTO} mit den {@link LadeZaehldatumDTO}.
     * @throws DataNotFoundException wenn keine Zaehlung gefunden wurde
     */
    public LadeZaehldatenTableDTO ladeZaehldaten(final UUID zaehlungId,
            final OptionsDTO options) throws DataNotFoundException {

        final LadeZaehldatenTableDTO ladeZaehldatenTable = new LadeZaehldatenTableDTO();
        final List<Zeitintervall> zeitintervalle;
        final Zaehlung zaehlung = indexService.getZaehlung(zaehlungId.toString());
        if (StringUtils.contains(options.getZeitauswahl(), ZEITAUSWAHL_SPITZENSTUNDE)) {
            zeitintervalle = extractZeitintervalleForSpitzenstunde(zaehlungId, zaehlung.getKreisverkehr(), options);
        } else {
            zeitintervalle = extractZeitintervalle(zaehlungId, zaehlung.getKreisverkehr(), options);
        }
        final PkwEinheit pkwEinheit = zaehlung.getPkwEinheit();
        List<LadeZaehldatumDTO> ladeZaehldaten = zeitintervalle.stream()
                .map(zeitintervall -> mapToZaehldatum(zeitintervall, pkwEinheit, options))
                .collect(Collectors.toList());
        ladeZaehldatenTable.setZaehldaten(ladeZaehldaten);
        log.debug("Anzahl der Zaehldaten: {}", ladeZaehldatenTable.getZaehldaten().size());
        return ladeZaehldatenTable;
    }

    private List<Zeitintervall> extractZeitintervalle(final UUID zaehlungId,
            final Boolean isKreisverkehr,
            final OptionsDTO options) {
        final Set<TypeZeitintervall> types = getTypesAccordingChosenOptions(options);
        log.debug("Types according chosen options: {}", types);
        final List<Zeitintervall> extractedZeitintervalle = extractZeitintervalle(
                zaehlungId,
                options.getZeitblock().getStart(),
                options.getZeitblock().getEnd(),
                options.getVonKnotenarm(),
                options.getNachKnotenarm(),
                isKreisverkehr,
                types);
        log.debug("Size of extracted Zeitintervalle: {}", extractedZeitintervalle.size());
        return extractedZeitintervalle.stream()
                .filter(zeitintervall -> shouldZeitintervallBeReturned(zeitintervall, options.getZeitblock()))
                .collect(Collectors.toList());
    }

    public List<Zeitintervall> extractZeitintervalle(
            final UUID zaehlungId,
            final LocalDateTime startUhrzeit,
            final LocalDateTime endeUhrzeit,
            final Integer von,
            final Integer nach,
            final Boolean isKreisverkehr,
            final Set<TypeZeitintervall> types) {
        final FahrbewegungKreisverkehr fahrbewegungKreisverkehr = createFahrbewegungKreisverkehr(von, nach, isKreisverkehr);
        final Integer vonKnotenarm;
        final Integer nachKnotenarm;
        if (isKreisverkehr) {
            /*
             * In {@link de.muenchen.dave.domain.Verkehrsbeziehung} definiert das Attribut "von"
             * den im Kreisverkehr jeweils betroffenen Knotenarm.
             * Das Attribut "nach" ist immer "null".
             */
            if (ObjectUtils.isNotEmpty(von) && ObjectUtils.isEmpty(nach)) {
                // Hinein
                vonKnotenarm = von;
            } else if (ObjectUtils.isEmpty(von) && ObjectUtils.isNotEmpty(nach)) {
                // Heraus
                vonKnotenarm = nach;
            } else {
                // Alles Hinein + Heraus + Vorbei
                vonKnotenarm = null;
            }
            nachKnotenarm = null;
        } else {
            vonKnotenarm = von;
            nachKnotenarm = nach;
        }
        return extractZeitintervalle(
                zaehlungId,
                startUhrzeit,
                endeUhrzeit,
                vonKnotenarm,
                nachKnotenarm,
                fahrbewegungKreisverkehr,
                types);
    }

    public List<Zeitintervall> extractZeitintervalleNg(
            final UUID zaehlungId,
            final Zaehlart zaehlart,
            final LocalDateTime startUhrzeit,
            final LocalDateTime endeUhrzeit,
            final OptionsDTO options,
            final Boolean isKreisverkehr,
            final Set<TypeZeitintervall> types) {

        return null;
    }

    public List<Zeitintervall> extractZeitintervalle(final UUID zaehlungId,
            final LocalDateTime startUhrzeit,
            final LocalDateTime endeUhrzeit,
            final Integer von,
            final Integer nach,
            final FahrbewegungKreisverkehr fahrbewegungKreisverkehr,
            final Set<TypeZeitintervall> types) {
        return zeitintervallRepository
                .findByZaehlungIdAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndVerkehrsbeziehungVonAndVerkehrsbeziehungNachAndVerkehrsbeziehungFahrbewegungKreisverkehrAndTypeInOrderBySortingIndexAsc(
                        zaehlungId,
                        startUhrzeit,
                        endeUhrzeit,
                        von,
                        nach,
                        fahrbewegungKreisverkehr,
                        types);
    }

    /**
     * Diese Methode extrahiert die Zeitintervalle für die Zeitauswahl bezüglich Spitzenstunde.
     *
     * @param zaehlungId zur Extraktion der {@link Zeitintervall}e aus der Datenbank.
     * @param options zur Extraktion der {@link Zeitintervall}e aus der Datenbank und zum Mapping nach
     *            {@link LadeZaehldatumDTO}.
     * @return die 15-minütigen {@link Zeitintervall}e welche die gewählte Spitzenstunde definieren
     *         gefolgt vom {@link Zeitintervall} der Spitzenstunde.
     */
    private List<Zeitintervall> extractZeitintervalleForSpitzenstunde(final UUID zaehlungId,
            final Boolean isKreisverkehr,
            final OptionsDTO options) {
        final List<Zeitintervall> spitzenstunden = extractZeitintervalle(zaehlungId, isKreisverkehr, options);
        final List<Zeitintervall> extractedZeitintervalle;
        if (!spitzenstunden.isEmpty()) {
            /*
             * Bei Auswahl des Zeitblocks für den gesamten Tag werden alle Spitzenstunden zurückgegeben.
             * d.h. die Spitzenstunden je Zeitblock und die Spitzenstunde über den ganzen Tag.
             * Hier ist dann die am Ende der Liste befindliche Spitzenstunde über den ganzen Tag zu extrahieren.
             * Bei Auswahl eines bestimmten Zeitblocks (nicht gesamter Tag) wird nur diese eine Spitzenstunde
             * in der Liste zurückgegeben. Diese wird ebenfalls vom Ende der Liste extrahiert.
             */
            final Zeitintervall spitzenStunde = spitzenstunden.get(spitzenstunden.size() - 1);
            extractedZeitintervalle = extractZeitintervalle(
                    zaehlungId,
                    spitzenStunde.getStartUhrzeit(),
                    spitzenStunde.getEndeUhrzeit(),
                    options.getVonKnotenarm(),
                    options.getNachKnotenarm(),
                    isKreisverkehr,
                    SetUtils.hashSet(TypeZeitintervall.STUNDE_VIERTEL));
            if (BooleanUtils.isTrue(options.getSpitzenstunde())) {
                extractedZeitintervalle.add(spitzenStunde);
            }
        } else {
            extractedZeitintervalle = List.of();
        }
        return extractedZeitintervalle;
    }

}
