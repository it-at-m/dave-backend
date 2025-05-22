/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.services.processzaehldaten;

import de.muenchen.dave.configuration.CachingConfiguration;
import de.muenchen.dave.domain.Fahrbeziehung;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.dtos.laden.BelastungsplanDataDTO;
import de.muenchen.dave.domain.dtos.laden.LadeBelastungsplanDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatumDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatumTageswertDTO;
import de.muenchen.dave.domain.elasticsearch.Zaehlstelle;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.enums.FahrbewegungKreisverkehr;
import de.muenchen.dave.domain.enums.Fahrzeug;
import de.muenchen.dave.domain.enums.TypeZeitintervall;
import de.muenchen.dave.domain.enums.Zaehldauer;
import de.muenchen.dave.domain.enums.Zeitauswahl;
import de.muenchen.dave.domain.enums.Zeitblock;
import de.muenchen.dave.exceptions.DataNotFoundException;
import de.muenchen.dave.repositories.elasticsearch.ZaehlstelleIndex;
import de.muenchen.dave.repositories.relationaldb.ZeitintervallRepository;
import de.muenchen.dave.services.ladezaehldaten.LadeZaehldatenService;
import de.muenchen.dave.util.CalculationUtil;
import de.muenchen.dave.util.dataimport.ZeitintervallGleitendeSpitzenstundeUtil;
import de.muenchen.dave.util.dataimport.ZeitintervallSortingIndexUtil;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ProcessZaehldatenBelastungsplanService {

    private static final Integer VALUE_TO_ROUND = 100;
    private static final String SUM = "sum";
    private static final String SUM_IN = "sumIn";
    private static final String SUM_OUT = "sumOut";

    private final ZeitintervallRepository zeitintervallRepository;

    private final ZaehlstelleIndex zaehlstelleIndex;

    private final LadeZaehldatenService ladeZaehldatenService;

    public ProcessZaehldatenBelastungsplanService(final ZeitintervallRepository zeitintervallRepository,
            final ZaehlstelleIndex zaehlstelleIndex,
            final LadeZaehldatenService ladeZaehldatenService) {
        this.zeitintervallRepository = zeitintervallRepository;
        this.zaehlstelleIndex = zaehlstelleIndex;
        this.ladeZaehldatenService = ladeZaehldatenService;
    }

    /**
     * Subtrahiert alle BigDecimal[][] des vergleichsBelastungsplans von den BigDecimal[][] des
     * basisBelastungsplan. Außerdem werden in den
     * Differenzwert-LadeBelastgunsplanDTO die Straßennamen von, wobei die Namen vom basisBelastungsplan
     * Prio1 haben, die vom vergleichsBelastungsplan Prio2 und
     * als default null.
     *
     * @param basisBelastungsplan Minuend-LadeBelastungsplanDTO
     * @param vergleichsBelastungsplan Subtrahend-LadeBelastungsplanDTO
     * @return Differenzwert-LadeBelastungsplanDTO
     */
    public static LadeBelastungsplanDTO calculateDifferenzdatenDTO(final LadeBelastungsplanDTO basisBelastungsplan,
            final LadeBelastungsplanDTO vergleichsBelastungsplan) {

        final LadeBelastungsplanDTO differenzBelastungsplanDTO = new LadeBelastungsplanDTO();
        if (basisBelastungsplan.getValue1().isFilled()) {
            differenzBelastungsplanDTO.setValue1(calculateDifferenzBelastungsplanData(basisBelastungsplan.getValue1(), vergleichsBelastungsplan.getValue1()));
        } else {
            differenzBelastungsplanDTO.setValue1(basisBelastungsplan.getValue1());
        }
        if (basisBelastungsplan.getValue2().isFilled()) {
            differenzBelastungsplanDTO.setValue2(calculateDifferenzBelastungsplanData(basisBelastungsplan.getValue2(), vergleichsBelastungsplan.getValue2()));
        } else {
            differenzBelastungsplanDTO.setValue2(basisBelastungsplan.getValue2());
        }
        if (basisBelastungsplan.getValue3().isFilled()) {
            differenzBelastungsplanDTO.setValue3(calculateDifferenzBelastungsplanData(basisBelastungsplan.getValue3(), vergleichsBelastungsplan.getValue3()));
        } else {
            differenzBelastungsplanDTO.setValue3(basisBelastungsplan.getValue3());
        }

        differenzBelastungsplanDTO.setKreisverkehr(basisBelastungsplan.isKreisverkehr());

        final String[] streets = new String[8];

        for (int i = 0; i < 8; i++) {
            if (basisBelastungsplan.getStreets()[i] != null) {
                streets[i] = basisBelastungsplan.getStreets()[i];
            } else if (vergleichsBelastungsplan.getStreets()[i] != null) {
                streets[i] = vergleichsBelastungsplan.getStreets()[i];
            }
        }

        differenzBelastungsplanDTO.setStreets(streets);

        return differenzBelastungsplanDTO;
    }

    /**
     * Subtrahiert eine BigDecimal[][]-Matrize von einer anderen.
     *
     * @param basis Minuend-Matrize
     * @param vergleich Subtrahend-Matrize
     * @return Differenzwert-Matrize
     */
    public static BigDecimal[][] subtractMatrice(final BigDecimal[][] basis, final BigDecimal[][] vergleich) {
        final int rows = basis.length;
        final int cols = basis[0].length;

        final BigDecimal[][] diff = new BigDecimal[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                diff[i][j] = basis[i][j].subtract(vergleich[i][j]);
            }
        }
        return diff;
    }

    /**
     * Erzeugt aus den beiden zu vergleichenden BelastungsplanDataDTO-Objekten ein
     * Belastungsplandata-Objekt
     *
     * @param basis Basis BelastungsplanDataDTO
     * @param vergleich Vergleich BelastungsplanDataDTO
     * @return Differenz BelastungsplanDataDTO
     */
    private static BelastungsplanDataDTO calculateDifferenzBelastungsplanData(final BelastungsplanDataDTO basis, final BelastungsplanDataDTO vergleich) {
        final BelastungsplanDataDTO belastungsplanData = new BelastungsplanDataDTO();
        belastungsplanData.setLabel(basis.getLabel());
        belastungsplanData.setFilled(basis.isFilled());
        belastungsplanData.setPercent(basis.isPercent());
        belastungsplanData.setValues(subtractMatrice(basis.getValues(), vergleich.getValues()));

        belastungsplanData.setSum(subtractSums(basis.getSum(), vergleich.getSum()));
        belastungsplanData.setSumIn(subtractSums(basis.getSumIn(), vergleich.getSumIn()));
        belastungsplanData.setSumOut(subtractSums(basis.getSumOut(), vergleich.getSumOut()));

        return belastungsplanData;
    }

    private static BigDecimal[] subtractSums(final BigDecimal[] basis, final BigDecimal[] vergleich) {
        final int minLength = Math.min(basis.length, vergleich.length);
        final BigDecimal[] fff = new BigDecimal[minLength];
        for (int i = 0; i < minLength; i++) {
            fff[i] = basis[i].subtract(vergleich[i]);
        }
        return fff;
    }

    private static boolean isKreisverkehr(final Fahrbeziehung fahrbeziehung) {
        return ObjectUtils.isNotEmpty(fahrbeziehung.getFahrbewegungKreisverkehr())
                && ObjectUtils.isEmpty(fahrbeziehung.getNach());
    }

    /**
     * Die Grafiken im Frontend erwarten pro Fahrbeziehung einen einzelnen Wert. Um an alle Werte
     * mittels Index zugreifen zu können ist ein 2-Stufiges Array
     * erforderlich. Ebene 1: Enthält alle Werte für die Von-Spuren Ebene 2: Enthält die Werte für die
     * Nach-Spur pro Von-Spur Bsp.: [ [Nach_1, Nach_2, ...,
     * Nach_8] // Von_1 ... [Nach_1, Nach_2, ..., Nach_8] // Von_8 ]
     *
     * @return mit BigDecimal.ZERO initialisierte Datenstruktur
     */
    private static BigDecimal[][] getEmptyDatastructure() {
        final BigDecimal[][] datastructure = new BigDecimal[8][8];
        Arrays.stream(datastructure).forEach(data -> Arrays.fill(data, BigDecimal.ZERO));
        return datastructure;
    }

    private static BelastungsplanDataDTO getEmptyBelastungsplanData() {
        final BelastungsplanDataDTO data = new BelastungsplanDataDTO();
        data.setLabel("");
        data.setFilled(false);
        data.setPercent(false);
        data.setValues(getEmptyDatastructure());
        return data;
    }

    private static boolean isFahrbeziehungNachOrKreisverkehrSet(final Zeitintervall zeitintervall) {
        return ObjectUtils.isNotEmpty(zeitintervall.getFahrbeziehung().getNach())
                || ObjectUtils.isNotEmpty(zeitintervall.getFahrbeziehung().getFahrbewegungKreisverkehr());
    }

    /**
     * Diese Methode rundet die Zahlinformationen im {@link LadeZaehldatumDTO} des Parameters "toRound"
     * auf den nächsten Wert welcher im Parameter
     * "nearestValueToRound" angegeben ist.
     * <p>
     * Eine Rundung wird durchgeführt sobald {@link OptionsDTO}#getWerteHundertRunden() den Wert true
     * besitzt.
     * <p>
     * Sobald der Wert im Zehnerbereich kleiner 50 wird auf den nächsten 100er-Wert abgerundet.
     * Andernfalls wird aufgerundet.
     *
     * @param toRound Auf welchem die Rundung durchgeführt werden soll.
     * @param nearestValueToRound Der Wert auf welchen aufgerundet werden soll.
     * @param optionsDto Um auf Durchführung der Rundung zu prüfen
     * @return den gerundeten {@link LadeZaehldatumDTO}, falls
     *         {@link OptionsDTO}#getWerteHundertRunden() den Wert true besitzt. Andernfall wird das
     *         {@link LadeZaehldatumDTO} im Parameter zurückgegeben.
     */
    public static LadeZaehldatumDTO roundToNearestIfRoundingIsChoosen(final LadeZaehldatumDTO toRound,
            final int nearestValueToRound,
            final OptionsDTO optionsDto) {
        if (BooleanUtils.isTrue(optionsDto.getWerteHundertRunden())) {
            final LadeZaehldatumTageswertDTO ladeZaehldatumDTO = new LadeZaehldatumTageswertDTO();
            ladeZaehldatumDTO.setType(toRound.getType());
            ladeZaehldatumDTO.setStartUhrzeit(toRound.getStartUhrzeit());
            ladeZaehldatumDTO.setEndeUhrzeit(toRound.getEndeUhrzeit());
            ladeZaehldatumDTO.setPkw(
                    roundIfNotNullOrZero(toRound.getPkw(), nearestValueToRound));
            ladeZaehldatumDTO.setLkw(
                    roundIfNotNullOrZero(toRound.getLkw(), nearestValueToRound));
            ladeZaehldatumDTO.setLastzuege(
                    roundIfNotNullOrZero(toRound.getLastzuege(), nearestValueToRound));
            ladeZaehldatumDTO.setBusse(
                    roundIfNotNullOrZero(toRound.getBusse(), nearestValueToRound));
            ladeZaehldatumDTO.setKraftraeder(
                    roundIfNotNullOrZero(toRound.getKraftraeder(), nearestValueToRound));
            ladeZaehldatumDTO.setFahrradfahrer(
                    roundIfNotNullOrZero(toRound.getFahrradfahrer(), nearestValueToRound));
            ladeZaehldatumDTO.setFussgaenger(
                    roundIfNotNullOrZero(toRound.getFussgaenger(), nearestValueToRound));
            ladeZaehldatumDTO.setPkwEinheiten(
                    roundIfNotNullOrZero(toRound.getPkwEinheiten(), nearestValueToRound));
            ladeZaehldatumDTO.setKfz(
                    roundIfNotNullOrZero(toRound.getKfz(), nearestValueToRound));
            ladeZaehldatumDTO.setSchwerverkehr(
                    roundIfNotNullOrZero(toRound.getSchwerverkehr(), nearestValueToRound));
            ladeZaehldatumDTO.setGueterverkehr(
                    roundIfNotNullOrZero(toRound.getGueterverkehr(), nearestValueToRound));
            return ladeZaehldatumDTO;
        } else {
            return toRound;
        }
    }

    /**
     * Führt eine Rundung durch sobald der Wert im Parameter "toRound" nicht NULL oder 0 ist.
     * Andernfalls wird der übergebene Wert zurückgegeben.
     * <p>
     * Sobald der Wert im Zehnerbereich kleiner 50 ist, wird auf den nächsten 100er-Wert abgerundet.
     * Andernfalls wird aufgerundet.
     *
     * @param toRound Der Wert welcher gerundet werden soll
     * @param nearestValueToRound Der nächste Wert auf den gerundet werden soll.
     * @return den gerundeten Wert oder der übergebene Wert falls keine Rundung durchgeführt wurde.
     */
    public static Integer roundIfNotNullOrZero(final Integer toRound, final int nearestValueToRound) {
        final Integer roundedValue;
        if (ObjectUtils.isNotEmpty(toRound)) {
            roundedValue = roundIfNotNullOrZero(BigDecimal.valueOf(toRound), nearestValueToRound).intValue();
        } else {
            roundedValue = toRound;
        }
        return roundedValue;
    }

    /**
     * Führt eine Rundung durch sobald der Wert im Parameter "toRound" nicht NULL oder 0 ist.
     * Andernfalls wird der übergebene Wert zurückgegeben.
     * <p>
     * Sobald der Wert im Zehnerbereich kleiner 50 ist, wird auf den nächsten 100er-Wert abgerundet.
     * Andernfalls wird aufgerundet.
     *
     * @param toRound Der Wert welcher gerundet werden soll
     * @param nearestValueToRound Der nächste Wert auf den gerundet werden soll.
     * @return den gerundeten Wert.
     */
    public static BigDecimal roundIfNotNullOrZero(final BigDecimal toRound, final int nearestValueToRound) {
        final BigDecimal roundedValue;
        if (ObjectUtils.isNotEmpty(toRound) && !toRound.equals(BigDecimal.ZERO)) {
            roundedValue = toRound
                    .divide(BigDecimal.valueOf(nearestValueToRound))
                    .setScale(0, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(nearestValueToRound));
        } else {
            roundedValue = toRound;
        }
        return roundedValue;
    }

    public static boolean containsSortingIndexForCompleteDay(final Zeitintervall zeitintervall) {
        return zeitintervall.getSortingIndex().equals(ZeitintervallSortingIndexUtil.SORTING_INDEX_SPITZEN_STUNDE_DAY_KFZ)
                || zeitintervall.getSortingIndex().equals(ZeitintervallSortingIndexUtil.SORTING_INDEX_SPITZEN_STUNDE_DAY_RAD)
                || zeitintervall.getSortingIndex().equals(ZeitintervallSortingIndexUtil.SORTING_INDEX_SPITZEN_STUNDE_DAY_FUSS);
    }

    /**
     * Ansprungsmethode, die unterscheidet, ob Differenzdaten berechnet werden müssen oder nicht
     *
     * @param zaehlungId Die Zaehlungs-ID für die {@link Zaehlung} und {@link Zeitintervall}e.
     * @param options Die durch den User im Frontend gewählten Optionen.
     * @return Die aufbreiteten Daten für das Belastungsplan.
     * @throws DataNotFoundException falls die {@link Zaehlstelle} oder die {@link Zaehlung}
     *             * nicht aus den DBs extrahiert werden kann.
     */
    @Cacheable(value = CachingConfiguration.LADE_BELASTUNGSPLAN_DTO, key = "{#p0, #p1}")
    public LadeBelastungsplanDTO getBelastungsplanDTO(final String zaehlungId,
            final OptionsDTO options) throws DataNotFoundException {
        log.debug(String.format("Zugriff auf #getBelastungsplanDTO mit %s und %s", zaehlungId, options.toString()));
        // überprüfung, ob Zaehlung exisitert. Wenn nicht -> DataNotFoundException
        findByZaehlungenId(zaehlungId);
        if (BooleanUtils.isTrue(options.getDifferenzdatenDarstellen())
                && options.getVergleichszaehlungsId() != null) {
            // überprüfung, ob Zaehlung exisitert. Wenn nicht -> DataNotFoundException
            findByZaehlungenId(options.getVergleichszaehlungsId());
            log.info("ladeDifferenzdatenBelastungsplan für Zaehlung {} aufgerufen", options.getVergleichszaehlungsId());
            return this.getDifferenzdatenBelastungsplanDTO(zaehlungId, options);
        } else {
            return this.ladeProcessedZaehldatenBelastungsplan(zaehlungId, options);
        }
    }

    /**
     * Diese Methode führt die Datenaufbereitung für den Belastungsplan durch.
     * <p>
     * Als Basis zur Datenaufbereitung wird bei {@link OptionsDTO}#getZeitauswahl() vom Typ
     * {@link LadeZaehldatenService#ZEITAUSWAHL_SPITZENSTUNDE_KFZ} die
     * Spitzenstunde des {@link OptionsDTO}#getZeitblock() verwendet. Ist der {@link Zeitblock#ZB_00_24}
     * gewählt, so wird die Spitzenstunde des Tages verwendet.
     * Ist bei {@link OptionsDTO}#getZeitauswahl() der Wert
     * {@link LadeZaehldatenService#ZEITAUSWAHL_SPITZENSTUNDE_KFZ} NICHT gesetzt, so dient als Basis zur
     * Datenaufbereitung die Tagessumme bzw. der Tageswert je einzelne {@link Fahrbeziehung}.
     * <p>
     * Das {@link LadeBelastungsplanDTO} enthält in jedem Attribut einen zweidimensionalen Array. Die
     * erste Dimension stellt die von Knotenarme dar. In der
     * zweiten Dimesion werden alle nach Knotenarme vorgehalten. Die im Array hinterlegten Werte
     * entsprechen somit den Verkehr fließend von einem Knotenarm zu
     * einem anderen oder den selben Knotenarm.
     *
     * @param zaehlungId Die Zaehlungs-ID für die {@link Zaehlung} und {@link Zeitintervall}e.
     * @param options Die durch den User im Frontend gewählten Optionen.
     * @return Die aufbreiteten Daten für das Belastungsplan.
     * @throws DataNotFoundException falls die {@link Zaehlstelle} oder die {@link Zaehlung} nicht aus
     *             den DBs extrahiert werden kann.
     */
    public LadeBelastungsplanDTO ladeProcessedZaehldatenBelastungsplan(final String zaehlungId,
            final OptionsDTO options) throws DataNotFoundException {
        final Zaehlung zaehlung = findByZaehlungenId(zaehlungId);
        final List<Zeitintervall> zeitintervalle;
        if (StringUtils.contains(options.getZeitauswahl(), LadeZaehldatenService.ZEITAUSWAHL_SPITZENSTUNDE)) {
            zeitintervalle = extractZeitintervalleSpitzenstunde(zaehlung, options);
        } else {
            zeitintervalle = extractZeitintervalle(zaehlungId, options);
        }
        final Map<Fahrbeziehung, TupelTageswertZaehldatum> ladeZaehldatumBelastungsplan = zeitintervalle.stream()
                .filter(ProcessZaehldatenBelastungsplanService::isFahrbeziehungNachOrKreisverkehrSet)
                .collect(Collectors.toMap(
                        Zeitintervall::getFahrbeziehung,
                        zeitintervall -> new TupelTageswertZaehldatum(
                                LadeZaehldatenService.isZeitintervallForTageswert(zeitintervall, options),
                                roundToNearestIfRoundingIsChoosen(
                                        LadeZaehldatenService.mapToZaehldatum(zeitintervall, zaehlung.getPkwEinheit(), options),
                                        VALUE_TO_ROUND,
                                        options))));

        final LadeBelastungsplanDTO ladeBelastungsplan = new LadeBelastungsplanDTO();
        ladeBelastungsplan.setStreets(new String[8]);
        ladeBelastungsplan.setValue1(getEmptyBelastungsplanData());
        ladeBelastungsplan.setValue2(getEmptyBelastungsplanData());
        ladeBelastungsplan.setValue3(getEmptyBelastungsplanData());

        final Map<Fahrzeug, BelastungsplanDataDTO> belastungsplanData = getBelastungsplanData(ladeZaehldatumBelastungsplan, zaehlung);
        zaehlung.getKnotenarme().forEach(knotenarm -> ladeBelastungsplan.getStreets()[knotenarm.getNummer() - 1] = knotenarm.getStrassenname());
        ladeBelastungsplan.setKreisverkehr(zaehlung.getKreisverkehr());

        // Wenn KFZ, GV, SV, GV_Prozent oder SV_Prozent gesetzt ist, dürfen RAD und FUSS nicht angezeigt werden
        if ((options.getGueterverkehrsanteilProzent() && belastungsplanData.containsKey(Fahrzeug.GV_P))
                || (options.getSchwerverkehrsanteilProzent() && belastungsplanData.containsKey(Fahrzeug.SV_P))
                || (options.getGueterverkehr() && belastungsplanData.containsKey(Fahrzeug.GV))
                || (options.getSchwerverkehr() && belastungsplanData.containsKey(Fahrzeug.SV))
                || (options.getKraftfahrzeugverkehr() && belastungsplanData.containsKey(Fahrzeug.KFZ))) {
            if (options.getGueterverkehrsanteilProzent() && belastungsplanData.containsKey(Fahrzeug.GV_P)) {
                putFirstValueInBelastungsplan(ladeBelastungsplan, belastungsplanData, Fahrzeug.GV_P);
            }
            if (options.getSchwerverkehrsanteilProzent() && belastungsplanData.containsKey(Fahrzeug.SV_P)) {
                putFirstValueInBelastungsplan(ladeBelastungsplan, belastungsplanData, Fahrzeug.SV_P);
            }
            if (options.getGueterverkehr() && belastungsplanData.containsKey(Fahrzeug.GV)) {
                putFirstValueInBelastungsplan(ladeBelastungsplan, belastungsplanData, Fahrzeug.GV);
            }
            if (options.getSchwerverkehr() && belastungsplanData.containsKey(Fahrzeug.SV)) {
                putFirstValueInBelastungsplan(ladeBelastungsplan, belastungsplanData, Fahrzeug.SV);
            }
            if (options.getKraftfahrzeugverkehr() && belastungsplanData.containsKey(Fahrzeug.KFZ)) {
                putFirstValueInBelastungsplan(ladeBelastungsplan, belastungsplanData, Fahrzeug.KFZ);
            }
        } else {
            if (options.getFussverkehr() && belastungsplanData.containsKey(Fahrzeug.FUSS)) {
                putFirstValueInBelastungsplan(ladeBelastungsplan, belastungsplanData, Fahrzeug.FUSS);
            }
            if (options.getRadverkehr() && belastungsplanData.containsKey(Fahrzeug.RAD)) {
                putFirstValueInBelastungsplan(ladeBelastungsplan, belastungsplanData, Fahrzeug.RAD);
            }
        }

        var ladeBelastungsplanSum = this.calculateSumsForLadeBelastungsplanDto(ladeBelastungsplan, belastungsplanData.get(Fahrzeug.KFZ),
                belastungsplanData.get(Fahrzeug.SV), belastungsplanData.get(Fahrzeug.GV));

        // KI-Hochgerechnete Werte sollen im Belastungsplan entsprechend gekennzeichnet werden
        if (Zeitauswahl.TAGESWERT.getCapitalizedName().equals(options.getZeitauswahl()) && List.of(Zaehldauer.DAUER_2_X_4_STUNDEN.toString(),
                Zaehldauer.DAUER_13_STUNDEN.toString(), Zaehldauer.DAUER_16_STUNDEN.toString()).contains(zaehlung.getZaehldauer())) {
            Stream.of(
                    ladeBelastungsplanSum.getValue1(),
                    ladeBelastungsplanSum.getValue2(),
                    ladeBelastungsplanSum.getValue3()).filter(v -> "RAD".equals(v.getLabel()))
                    .forEach(v -> v.setLabel("RAD (KI-Hochrechnung)"));
        }

        return ladeBelastungsplanSum;
    }

    private void putFirstValueInBelastungsplan(LadeBelastungsplanDTO ladeBelastungsplan, Map<Fahrzeug, BelastungsplanDataDTO> belastungsplanData,
            Fahrzeug value) {
        ladeBelastungsplan.setValue3(ladeBelastungsplan.getValue2());
        ladeBelastungsplan.setValue2(ladeBelastungsplan.getValue1());
        ladeBelastungsplan.setValue1(belastungsplanData.get(value));
    }

    /**
     * Reichert das übergebene LadeBelastungsplanDTO-Objekt um die Summen der einzelnen Knotenarme an.
     *
     * @param ladeBelastungsplan LadeBelastungsplanDTO-Objekt, welches um die Summen angereichert werden
     *            soll
     * @param dataKfz Datengrundlage von KFZ zur Berechnung der %-Anteile
     * @param dataSv Datengrundlage von SV zur Berechnung der SV%-Anteile
     * @param dataGv Datengrundlage von GV zur Berechnung der GV%-Anteile
     * @return gibt das um alle Summen erweiterte LadeBelastungsplanDTO-Objekt zurück
     */
    private LadeBelastungsplanDTO calculateSumsForLadeBelastungsplanDto(final LadeBelastungsplanDTO ladeBelastungsplan, final BelastungsplanDataDTO dataKfz,
            final BelastungsplanDataDTO dataSv, final BelastungsplanDataDTO dataGv) {

        Map<String, BigDecimal[]> sumsKfz = null;
        Map<String, BigDecimal[]> sumsSv = null;
        Map<String, BigDecimal[]> sumsGv = null;

        if (dataKfz != null && dataKfz.isFilled()) {
            if (ladeBelastungsplan.isKreisverkehr()) {
                sumsKfz = this.calcSumsKreisverkehr(dataKfz.getValues());
            } else {
                sumsKfz = this.calcSumsKreuzung(dataKfz.getValues());
            }
        }
        if (dataSv != null && dataSv.isFilled()) {
            if (ladeBelastungsplan.isKreisverkehr()) {
                sumsSv = this.calcSumsKreisverkehr(dataSv.getValues());
            } else {
                sumsSv = this.calcSumsKreuzung(dataSv.getValues());
            }
        }
        if (dataGv != null && dataGv.isFilled()) {
            if (ladeBelastungsplan.isKreisverkehr()) {
                sumsGv = this.calcSumsKreisverkehr(dataGv.getValues());
            } else {
                sumsGv = this.calcSumsKreuzung(dataGv.getValues());
            }
        }

        if (ladeBelastungsplan.getValue1().isFilled()) {
            ladeBelastungsplan.setValue1(
                    this.calculateSumsForBelastungsplanDataDto(ladeBelastungsplan.getValue1(), sumsKfz, sumsSv, sumsGv, ladeBelastungsplan.isKreisverkehr()));
        }

        if (ladeBelastungsplan.getValue2().isFilled()) {
            ladeBelastungsplan.setValue2(
                    this.calculateSumsForBelastungsplanDataDto(ladeBelastungsplan.getValue2(), sumsKfz, sumsSv, sumsGv, ladeBelastungsplan.isKreisverkehr()));
        }

        if (ladeBelastungsplan.getValue3().isFilled()) {
            ladeBelastungsplan.setValue3(
                    this.calculateSumsForBelastungsplanDataDto(ladeBelastungsplan.getValue3(), sumsKfz, sumsSv, sumsGv, ladeBelastungsplan.isKreisverkehr()));
        }

        return ladeBelastungsplan;
    }

    /**
     * Reichert das übergebene BelastungsplanDataDTO-Objekt um die Summen der einzelnen Knotenarme an.
     *
     * @param data BelastungsplanDataDTO-Objekt, welches um die Summen angereichert werden soll
     * @param sumsKfz Datengrundlage von KFZ zur Berechnung der %-Anteile
     * @param sumsSv Datengrundlage von SV zur Berechnung der SV%-Anteile
     * @param sumsGv Datengrundlage von GV zur Berechnung der GV%-Anteile
     * @return gibt das um die Summen erweiterte BelastungsplanDataDTO-Objekt zurück
     */
    private BelastungsplanDataDTO calculateSumsForBelastungsplanDataDto(final BelastungsplanDataDTO data, final Map<String, BigDecimal[]> sumsKfz,
            final Map<String, BigDecimal[]> sumsSv, final Map<String, BigDecimal[]> sumsGv, final boolean isKreisverkehr) {

        if (data.getLabel().equalsIgnoreCase(Fahrzeug.SV_P.getName()) && sumsKfz != null && sumsSv != null) {
            final Map<String, BigDecimal[]> sumSvp = this.calculateSumsSvpOrGvpKreuzung(sumsKfz, sumsSv);
            data.setSum(sumSvp.get(SUM));
            data.setSumIn(sumSvp.get(SUM_IN));
            data.setSumOut(sumSvp.get(SUM_OUT));
        } else if (data.getLabel().equalsIgnoreCase(Fahrzeug.GV_P.getName()) && sumsKfz != null && sumsGv != null) {
            final Map<String, BigDecimal[]> sumGvp = this.calculateSumsSvpOrGvpKreuzung(sumsKfz, sumsGv);
            data.setSum(sumGvp.get(SUM));
            data.setSumIn(sumGvp.get(SUM_IN));
            data.setSumOut(sumGvp.get(SUM_OUT));
        } else {
            final Map<String, BigDecimal[]> sums;
            if (isKreisverkehr) {
                sums = this.calcSumsKreisverkehr(data.getValues());
            } else {
                sums = this.calcSumsKreuzung(data.getValues());
            }
            data.setSum(sums.get(SUM));
            data.setSumIn(sums.get(SUM_IN));
            data.setSumOut(sums.get(SUM_OUT));
        }
        return data;
    }

    private Map<String, BigDecimal[]> calcSumsKreuzung(final BigDecimal[][] values) {
        final Map<Integer, List<BigDecimal>> listOut = new HashMap<>();
        final Map<Integer, List<BigDecimal>> listIn = new HashMap<>();
        final Map<Integer, List<BigDecimal>> listBoth = new HashMap<>();
        for (int outerIndex = 0; outerIndex < values.length; outerIndex++) { // von
            final ArrayList<BigDecimal> out = new ArrayList<>();
            final ArrayList<BigDecimal> in = new ArrayList<>();
            final ArrayList<BigDecimal> both = new ArrayList<>();
            for (int innerIndex = 0; innerIndex < values[outerIndex].length; innerIndex++) { // nach
                in.add(values[outerIndex][innerIndex]);
                out.add(values[innerIndex][outerIndex]);
                both.add(values[outerIndex][innerIndex]);
                both.add(values[innerIndex][outerIndex]);
            }
            listOut.put(outerIndex, out);
            listIn.put(outerIndex, in);
            listBoth.put(outerIndex, both);
        }

        final Map<String, BigDecimal[]> sums = new HashMap<>();
        sums.put(SUM_IN, this.sumValuesOfList(listIn));
        sums.put(SUM_OUT, this.sumValuesOfList(listOut));
        sums.put(SUM, this.sumValuesOfList(listBoth));
        return sums;
    }

    /**
     * Berechnet pro Summe den Prozentwert
     *
     * @param sumsKfz Summen von KFZ
     * @param sumsSvOrGv Summen von SV oder GV
     * @return Summen von SV% oder GV%
     */
    private Map<String, BigDecimal[]> calculateSumsSvpOrGvpKreuzung(final Map<String, BigDecimal[]> sumsKfz, final Map<String, BigDecimal[]> sumsSvOrGv) {
        final Map<String, BigDecimal[]> sumsSvpOrGvp = new HashMap<>();
        sumsSvpOrGvp.put(SUM_IN, this.calculateAnteilProzent(sumsKfz.get(SUM_IN), sumsSvOrGv.get(SUM_IN)));
        sumsSvpOrGvp.put(SUM_OUT, this.calculateAnteilProzent(sumsKfz.get(SUM_OUT), sumsSvOrGv.get(SUM_OUT)));
        sumsSvpOrGvp.put(SUM, this.calculateAnteilProzent(sumsKfz.get(SUM), sumsSvOrGv.get(SUM)));
        return sumsSvpOrGvp;
    }

    /**
     * Berechnet den Prozentwert pro ArrayElement
     *
     * @param kfz kfz
     * @param svOrGv sv oder gv
     * @return Array
     */
    private BigDecimal[] calculateAnteilProzent(final BigDecimal[] kfz, final BigDecimal[] svOrGv) {
        if (kfz != null && svOrGv != null) {
            final BigDecimal[] sumInSvpOrGvp = new BigDecimal[svOrGv.length];
            for (int index = 0; index < svOrGv.length; index++) {
                sumInSvpOrGvp[index] = CalculationUtil.calculateAnteilProzent(svOrGv[index], kfz[index]);
            }
            return sumInSvpOrGvp;
        } else {
            return new BigDecimal[0];
        }
    }

    /**
     * Berechnet aus eine zweidimensionalen Array die einzelnen Summen (Einfahrend, Ausfahren, Beide
     * zusammen) pro Knotenarm für Kreisverkehre.
     *
     * @param values Werte des Kreisverkehrs pro Knotenarm
     * @return Map mit den einzelnen Summen pro Knotenarm
     */
    private Map<String, BigDecimal[]> calcSumsKreisverkehr(final BigDecimal[][] values) {

        final Map<Integer, List<BigDecimal>> listIn = new HashMap<>();
        final Map<Integer, List<BigDecimal>> listBoth = new HashMap<>();
        for (int outerIndex = 0; outerIndex < values.length; outerIndex++) { // von
            final ArrayList<BigDecimal> in = new ArrayList<>();
            final ArrayList<BigDecimal> both = new ArrayList<>();
            both.add(values[outerIndex][0]); // in den Kreis
            both.add(values[outerIndex][2]); // aus dem Kreis

            in.add(values[outerIndex][0]); // in den Kreis
            in.add(values[outerIndex][1]); // vorbei am Arm
            listIn.put(outerIndex, in);
            listBoth.put(outerIndex, both);
        }

        final Map<String, BigDecimal[]> sums = new HashMap<>();
        sums.put(SUM_IN, this.sumValuesOfList(listIn));
        //        sums.put(SUM_OUT, this.sumValuesOfList(listOut));
        sums.put(SUM, this.sumValuesOfList(listBoth));
        return sums;
    }

    /**
     * Summiert die einzelnen Werte in der List auf und packt sie an die entsprechende Stelle im Array
     *
     * @param listToSum Map mit allen zu addierenden Werten pro Knotenarm
     * @return Array mit allen Summen pro Knotenarm
     */
    private BigDecimal[] sumValuesOfList(final Map<Integer, List<BigDecimal>> listToSum) {
        final BigDecimal[] sumPerNode = new BigDecimal[listToSum.size()];
        listToSum.forEach((key, value) -> {
            BigDecimal sum = new BigDecimal(0);
            for (BigDecimal bd : value) {
                sum = sum.add(bd);
            }
            sumPerNode[key] = sum;
        });
        return sumPerNode;
    }

    /**
     * Lädt zwei zu vergleichende Zählungen als LadeBelastungsplanDTO, subtrahiert diese voneinander und
     * gibt den daraus resultierenden LadeBelastungsplanDTO
     * zurück.
     *
     * @param zaehlungId ID f. Basis-Belastungsplan
     * @param options Optionen, in denen die ID für den Vergleichs-Belastungsplan vorhanden ist
     * @return Differenzbelastungsplan
     * @throws DataNotFoundException beim Laden der Zaehldaten aus der DB
     */
    public LadeBelastungsplanDTO getDifferenzdatenBelastungsplanDTO(final String zaehlungId,
            final OptionsDTO options) throws DataNotFoundException {
        final LadeBelastungsplanDTO basisBelastungsplan = ladeProcessedZaehldatenBelastungsplan(zaehlungId, options);

        // Fuer die zweite Zaehlung muss in den Optionen die korrekte Zaehldauer gesetzt werden, damit
        // der Vergleich auch klappt. Dies ist noetig, wenn zwei Zaehlungen mit unterschiedlicher Dauer verglichen
        // werden (Es macht aber nichts, wenn man den Wert immer neu setzt).
        // Stimmt die Zaehldauer aus den Options nicht mit der Zaehldauer aus der Zaehlung ueberein, so
        // liefert die Methode LadeZaehldatenService.isZeitintervallForTageswert(zeitintervall, options) ein falsches
        // Ergebnis zurueck.
        final Zaehlung zaehlung = this.findByZaehlungenId(options.getVergleichszaehlungsId());
        options.setZaehldauer(Zaehldauer.valueOf(zaehlung.getZaehldauer()));

        final LadeBelastungsplanDTO vergleichsBelastungsplan = ladeProcessedZaehldatenBelastungsplan(options.getVergleichszaehlungsId(), options);

        return calculateDifferenzdatenDTO(basisBelastungsplan, vergleichsBelastungsplan);
    }

    public List<Zeitintervall> extractZeitintervalle(final String zaehlungId,
            final OptionsDTO options) {
        return zeitintervallRepository
                .findByZaehlungIdAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndFahrbeziehungVonNotNullAndTypeOrderBySortingIndexAsc(
                        UUID.fromString(zaehlungId),
                        options.getZeitblock().getStart(),
                        options.getZeitblock().getEnd(),
                        options.getZeitblock().getTypeZeitintervall());
    }

    /**
     * Diese Methode extrahiert die Zeitintervalle für die Zeitauswahl bezüglich Spitzenstunde.
     * <p>
     * Anhand der Informationen in den {@link OptionsDTO} wird die relevante Spitzenstunde extrahiert.
     * Diese Spitzenstunde dient mit der
     * {@link Zeitintervall}#getStartUhrzeit() und der {@link Zeitintervall}#getEndeUhrzeit() als
     * Zeitbasis zur Ermittlung der Summen über die vier 15-minütigen
     * Zeitintervalle je Fahrbeziehung.
     *
     * @param zaehlung zur Extraktion der {@link Zeitintervall}e aus der Datenbank.
     * @param options zur Extraktion der {@link Zeitintervall}e aus der Datenbank.
     * @return der {@link Zeitintervall} der Spitzenstunde.
     */
    public List<Zeitintervall> extractZeitintervalleSpitzenstunde(final Zaehlung zaehlung,
            final OptionsDTO options) {
        final TypeZeitintervall chosenSpitzenstunde;
        if (StringUtils.equals(options.getZeitauswahl(), LadeZaehldatenService.ZEITAUSWAHL_SPITZENSTUNDE_KFZ)) {
            chosenSpitzenstunde = TypeZeitintervall.SPITZENSTUNDE_KFZ;
        } else if (StringUtils.equals(options.getZeitauswahl(), LadeZaehldatenService.ZEITAUSWAHL_SPITZENSTUNDE_RAD)) {
            chosenSpitzenstunde = TypeZeitintervall.SPITZENSTUNDE_RAD;
        } else {
            chosenSpitzenstunde = TypeZeitintervall.SPITZENSTUNDE_FUSS;
        }
        final List<Zeitintervall> spitzenstunden = ladeZaehldatenService.extractZeitintervalle(
                UUID.fromString(zaehlung.getId()),
                options.getZeitblock().getStart(),
                options.getZeitblock().getEnd(),
                options.getVonKnotenarm(),
                options.getNachKnotenarm(),
                (FahrbewegungKreisverkehr) null,
                SetUtils.hashSet(chosenSpitzenstunde));
        if (!spitzenstunden.isEmpty()) {

            /*
             * Bei Auswahl des Zeitblocks für den gesamten Tag werden alle Spitzenstunden zurückgegeben.
             * d.h. die Spitzenstunden je Zeitblock und die Spitzenstunde über den ganzen Tag.
             * Hier ist dann die am Ende der Liste befindliche Spitzenstunde über den ganzen Tag zu extrahieren.
             *
             * Bei Auswahl eines bestimmten Zeitblocks (nicht gesamter Tag) wird nur diese eine Spitzenstunde
             * in der Liste zurückgegeben. Diese wird ebenfalls vom Ende der Liste extrahiert.
             */
            final Zeitintervall spitzenStunde = spitzenstunden.get(spitzenstunden.size() - 1);
            final List<Zeitintervall> zeitintervalle = zeitintervallRepository
                    .findByZaehlungIdAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndFahrbeziehungVonNotNullAndTypeOrderBySortingIndexAsc(
                            UUID.fromString(zaehlung.getId()),
                            spitzenStunde.getStartUhrzeit(),
                            spitzenStunde.getEndeUhrzeit(),
                            TypeZeitintervall.STUNDE_VIERTEL);
            return ZeitintervallGleitendeSpitzenstundeUtil.getGleitendeSpitzenstunden(zeitintervalle)
                    .stream()
                    .filter(zeitintervall -> zeitintervall.getType().equals(chosenSpitzenstunde))
                    .filter(zeitintervall -> {
                        /*
                         * Erforderlich, da in Klasse {@link ZeitintervallSortingIndexUtil} immer jeweils für
                         * alle Zeitblöcke eine Berechnung der Spitzenstunde durchgeführt wird.
                         */
                        if (options.getZeitblock().equals(Zeitblock.ZB_00_24)) {
                            return containsSortingIndexForCompleteDay(zeitintervall);
                        } else {
                            return !containsSortingIndexForCompleteDay(zeitintervall);
                        }
                    })
                    .collect(Collectors.toList());
        } else {
            return List.of();
        }
    }

    /**
     * Liefert eine {@link BelastungsplanDataDTO} pro Fahrzeugklasse mit den Daten für den
     * Belastungsplan
     *
     * @param zaehldatenJeFahrbeziehung aus der DB ermittelten Werte
     * @param zaehlung wird benötigt zur überprüfung, ob welche Fahrzeug gezählt wurden
     * @return eine Map mit Key: Fahrzeug und Value:BelastungsplanDataDTO.
     */
    public Map<Fahrzeug, BelastungsplanDataDTO> getBelastungsplanData(final Map<Fahrbeziehung, TupelTageswertZaehldatum> zaehldatenJeFahrbeziehung,
            final Zaehlung zaehlung) {
        final Map<Fahrzeug, BelastungsplanDataDTO> returnValue = new HashMap<>();

        final BelastungsplanDataDTO belastungsplanDataKfz = new BelastungsplanDataDTO();
        belastungsplanDataKfz.setFilled(zaehlung.getKategorien().contains(Fahrzeug.KFZ));
        belastungsplanDataKfz.setPercent(false);
        belastungsplanDataKfz.setLabel(Fahrzeug.KFZ.getName());
        belastungsplanDataKfz.setValues(getEmptyDatastructure());

        final BelastungsplanDataDTO belastungsplanDataSv = new BelastungsplanDataDTO();
        belastungsplanDataSv.setFilled(zaehlung.getKategorien().contains(Fahrzeug.SV));
        belastungsplanDataSv.setPercent(false);
        belastungsplanDataSv.setLabel(Fahrzeug.SV.getName());
        belastungsplanDataSv.setValues(getEmptyDatastructure());

        final BelastungsplanDataDTO belastungsplanDataGv = new BelastungsplanDataDTO();
        belastungsplanDataGv.setFilled(zaehlung.getKategorien().contains(Fahrzeug.GV));
        belastungsplanDataGv.setPercent(false);
        belastungsplanDataGv.setLabel(Fahrzeug.GV.getName());
        belastungsplanDataGv.setValues(getEmptyDatastructure());

        final BelastungsplanDataDTO belastungsplanDataRad = new BelastungsplanDataDTO();
        belastungsplanDataRad.setFilled(zaehlung.getKategorien().contains(Fahrzeug.RAD));
        belastungsplanDataRad.setPercent(false);
        belastungsplanDataRad.setLabel(Fahrzeug.RAD.getName());
        belastungsplanDataRad.setValues(getEmptyDatastructure());

        final BelastungsplanDataDTO belastungsplanDataFuss = new BelastungsplanDataDTO();
        belastungsplanDataFuss.setFilled(zaehlung.getKategorien().contains(Fahrzeug.FUSS));
        belastungsplanDataFuss.setPercent(false);
        belastungsplanDataFuss.setLabel(Fahrzeug.FUSS.getName());
        belastungsplanDataFuss.setValues(getEmptyDatastructure());

        final BelastungsplanDataDTO belastungsplanDataSvProzent = new BelastungsplanDataDTO();
        belastungsplanDataSvProzent.setFilled(belastungsplanDataKfz.isFilled() && belastungsplanDataSv.isFilled());
        belastungsplanDataSvProzent.setPercent(true);
        belastungsplanDataSvProzent.setLabel(Fahrzeug.SV_P.getName());
        belastungsplanDataSvProzent.setValues(getEmptyDatastructure());

        final BelastungsplanDataDTO belastungsplanDataGvProzent = new BelastungsplanDataDTO();
        belastungsplanDataGvProzent.setFilled(belastungsplanDataKfz.isFilled() && belastungsplanDataGv.isFilled());
        belastungsplanDataGvProzent.setPercent(true);
        belastungsplanDataGvProzent.setLabel(Fahrzeug.GV_P.getName());
        belastungsplanDataGvProzent.setValues(getEmptyDatastructure());

        zaehldatenJeFahrbeziehung.forEach((fahrbeziehung, tupelTageswertZaehldatum) -> {
            final int index1;
            final int index2;
            if (isKreisverkehr(fahrbeziehung)) {
                // Von-Knotennummer - 1
                index1 = fahrbeziehung.getVon() - 1;
                // HINEIN = 0, VORBEI = 1, HERAUS = 2
                switch (fahrbeziehung.getFahrbewegungKreisverkehr()) {
                case HINEIN:
                    index2 = 0;
                    break;
                case VORBEI:
                    index2 = 1;
                    break;
                case HERAUS:
                    index2 = 2;
                    break;
                default:
                    index2 = -1;
                }
            } else {
                index1 = fahrbeziehung.getVon() - 1;
                index2 = fahrbeziehung.getNach() - 1;
            }

            belastungsplanDataKfz.getValues()[index1][index2] = tupelTageswertZaehldatum.getLadeZaehldatum().getKfz();
            belastungsplanDataSv.getValues()[index1][index2] = tupelTageswertZaehldatum.getLadeZaehldatum().getSchwerverkehr();
            belastungsplanDataGv.getValues()[index1][index2] = tupelTageswertZaehldatum.getLadeZaehldatum().getGueterverkehr();

            if (belastungsplanDataSvProzent.isFilled()) {
                belastungsplanDataSvProzent.getValues()[index1][index2] = CalculationUtil
                        .calculateAnteilProzent(belastungsplanDataSv.getValues()[index1][index2], belastungsplanDataKfz.getValues()[index1][index2]);
            }
            if (belastungsplanDataGvProzent.isFilled()) {
                belastungsplanDataGvProzent.getValues()[index1][index2] = CalculationUtil
                        .calculateAnteilProzent(belastungsplanDataGv.getValues()[index1][index2], belastungsplanDataKfz.getValues()[index1][index2]);
            }

            belastungsplanDataRad.getValues()[index1][index2] = BigDecimal.valueOf(
                    ObjectUtils.defaultIfNull(
                            tupelTageswertZaehldatum.getLadeZaehldatum().getFahrradfahrer(),
                            0));

            if (!tupelTageswertZaehldatum.getIsTageswert()) {
                belastungsplanDataFuss.getValues()[index1][index2] = BigDecimal.valueOf(
                        ObjectUtils.defaultIfNull(
                                tupelTageswertZaehldatum.getLadeZaehldatum().getFussgaenger(),
                                0));
            }
        });

        if (belastungsplanDataKfz.isFilled()) {
            returnValue.put(Fahrzeug.KFZ, belastungsplanDataKfz);
        }
        if (belastungsplanDataSv.isFilled()) {
            returnValue.put(Fahrzeug.SV, belastungsplanDataSv);
        }
        if (belastungsplanDataGv.isFilled()) {
            returnValue.put(Fahrzeug.GV, belastungsplanDataGv);
        }
        if (belastungsplanDataRad.isFilled()) {
            returnValue.put(Fahrzeug.RAD, belastungsplanDataRad);
        }
        if (belastungsplanDataFuss.isFilled()) {
            returnValue.put(Fahrzeug.FUSS, belastungsplanDataFuss);
        }
        if (belastungsplanDataSvProzent.isFilled()) {
            returnValue.put(Fahrzeug.SV_P, belastungsplanDataSvProzent);
        }
        if (belastungsplanDataGvProzent.isFilled()) {
            returnValue.put(Fahrzeug.GV_P, belastungsplanDataGvProzent);
        }
        return returnValue;
    }

    /**
     * Auf Basis der Zaehlungs-Id im Parameter wird die Zaehlung aus der DB extrahiert.
     *
     * @param zaehlungId Die Zaehlungs-ID für die {@link Zaehlung}.
     * @return die {@link Zaehlung} zur Zaehlungs-ID.
     * @throws DataNotFoundException falls die {@link Zaehlstelle} oder die {@link Zaehlung} nicht aus
     *             den DBs extrahiert werden kann.
     */
    public Zaehlung findByZaehlungenId(final String zaehlungId) throws DataNotFoundException {
        final Optional<Zaehlstelle> zaehlstelleOptional = zaehlstelleIndex.findByZaehlungenId(zaehlungId);
        if (!zaehlstelleOptional.isPresent()) {
            throw new DataNotFoundException("Zaehlstelle not found");
        }
        final Optional<Zaehlung> zaehlungOptional = zaehlstelleOptional.get().getZaehlungen().stream()
                .filter(zaehlungToCheck -> zaehlungToCheck.getId().equals(zaehlungId))
                .findFirst();
        if (!zaehlungOptional.isPresent()) {
            throw new DataNotFoundException("Zaehlung not found");
        }
        return zaehlungOptional.get();
    }

    @AllArgsConstructor
    @Getter
    @Setter
    @EqualsAndHashCode
    @ToString
    public static class TupelTageswertZaehldatum {

        private Boolean isTageswert;

        private LadeZaehldatumDTO ladeZaehldatum;

    }

}
