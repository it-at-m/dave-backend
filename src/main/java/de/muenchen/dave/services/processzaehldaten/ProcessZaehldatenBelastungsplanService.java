package de.muenchen.dave.services.processzaehldaten;

import de.muenchen.dave.configuration.CachingConfiguration;
import de.muenchen.dave.domain.Verkehrsbeziehung;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.dtos.laden.AbstractLadeBelastungsplanDTO;
import de.muenchen.dave.domain.dtos.laden.BelastungsplanDataDTO;
import de.muenchen.dave.domain.dtos.laden.LadeBelastungsplanDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatumDTO;
import de.muenchen.dave.domain.elasticsearch.Zaehlstelle;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.enums.TypeZeitintervall;
import de.muenchen.dave.domain.enums.Zaehlart;
import de.muenchen.dave.domain.enums.Zaehldauer;
import de.muenchen.dave.domain.enums.Zeitblock;
import de.muenchen.dave.exceptions.DataNotFoundException;
import de.muenchen.dave.repositories.elasticsearch.ZaehlstelleIndex;
import de.muenchen.dave.repositories.relationaldb.ZeitintervallRepository;
import de.muenchen.dave.services.ladezaehldaten.LadeZaehldatenService;
import de.muenchen.dave.util.BelastungsplanCalculator;
import de.muenchen.dave.util.dataimport.ZeitintervallGleitendeSpitzenstundeUtil;
import de.muenchen.dave.util.dataimport.ZeitintervallSortingIndexUtil;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessZaehldatenBelastungsplanService {

    private final ZeitintervallRepository zeitintervallRepository;

    private final ZaehlstelleIndex zaehlstelleIndex;

    private final LadeZaehldatenService ladeZaehldatenService;

    private final BelastungsplanDataServiceFactory belastungsplanDataServiceFactory;

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
            differenzBelastungsplanDTO.setValue1(
                    calculateDifferenzBelastungsplanData(
                            basisBelastungsplan.getValue1(),
                            vergleichsBelastungsplan.getValue1()));
        } else {
            differenzBelastungsplanDTO.setValue1(basisBelastungsplan.getValue1());
        }
        if (basisBelastungsplan.getValue2().isFilled()) {
            differenzBelastungsplanDTO.setValue2(
                    calculateDifferenzBelastungsplanData(
                            basisBelastungsplan.getValue2(),
                            vergleichsBelastungsplan.getValue2()));
        } else {
            differenzBelastungsplanDTO.setValue2(basisBelastungsplan.getValue2());
        }
        if (basisBelastungsplan.getValue3().isFilled()) {
            differenzBelastungsplanDTO.setValue3(
                    calculateDifferenzBelastungsplanData(
                            basisBelastungsplan.getValue3(),
                            vergleichsBelastungsplan.getValue3()));
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
    public AbstractLadeBelastungsplanDTO<?> getBelastungsplanDTO(
            final String zaehlungId,
            final OptionsDTO options) throws DataNotFoundException {
        log.debug(String.format("Zugriff auf #getBelastungsplanDTO mit %s und %s", zaehlungId, options.toString()));
        // überprüfung, ob Zaehlung exisitert. Wenn nicht -> DataNotFoundException
        final Zaehlung zaehlung = findByZaehlungenId(zaehlungId);
        final var zaehlart = Zaehlart.valueOf(zaehlung.getZaehlart());
        if (Zaehlart.QU.equals(zaehlart)) {
            return new LadeBelastungsplanDTO();
        } else {
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
     * Datenaufbereitung die Tagessumme bzw. der Tageswert je einzelne {@link Verkehrsbeziehung}.
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
    public AbstractLadeBelastungsplanDTO<?> ladeProcessedZaehldatenBelastungsplan(final String zaehlungId,
            final OptionsDTO options) throws DataNotFoundException {
        final Zaehlung zaehlung = findByZaehlungenId(zaehlungId);
        final List<Zeitintervall> zeitintervalle;
        if (StringUtils.contains(options.getZeitauswahl(), LadeZaehldatenService.ZEITAUSWAHL_SPITZENSTUNDE)) {
            zeitintervalle = extractZeitintervalleSpitzenstunde(zaehlung, options);
        } else {
            zeitintervalle = extractZeitintervalle(zaehlungId, options.getZeitblock());
        }
        return belastungsplanDataServiceFactory.getBelastungsplanDataService(zaehlung).buildLadeBelastungsplanDTO(options, zaehlung,
                zeitintervalle);
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
        final LadeBelastungsplanDTO basisBelastungsplan = castLadeBelastungsplanDTO(ladeProcessedZaehldatenBelastungsplan(zaehlungId, options));

        // Fuer die zweite Zaehlung muss in den Optionen die korrekte Zaehldauer gesetzt werden, damit
        // der Vergleich auch klappt. Dies ist noetig, wenn zwei Zaehlungen mit unterschiedlicher Dauer verglichen
        // werden (Es macht aber nichts, wenn man den Wert immer neu setzt).
        // Stimmt die Zaehldauer aus den Options nicht mit der Zaehldauer aus der Zaehlung ueberein, so
        // liefert die Methode LadeZaehldatenService.isZeitintervallForTageswert(zeitintervall, options) ein falsches
        // Ergebnis zurueck.
        final Zaehlung zaehlung = this.findByZaehlungenId(options.getVergleichszaehlungsId());
        options.setZaehldauer(Zaehldauer.valueOf(zaehlung.getZaehldauer()));

        final LadeBelastungsplanDTO vergleichsBelastungsplan = castLadeBelastungsplanDTO(
                ladeProcessedZaehldatenBelastungsplan(options.getVergleichszaehlungsId(),
                        options));

        return calculateDifferenzdatenDTO(basisBelastungsplan, vergleichsBelastungsplan);
    }

    public List<Zeitintervall> extractZeitintervalle(
            final String zaehlungId,
            final Zeitblock zeitblock) {
        return zeitintervallRepository
                .findByZaehlungIdAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndTypeInOrderBySortingIndexAsc(
                        UUID.fromString(zaehlungId),
                        zeitblock.getStart(),
                        zeitblock.getEnd(),
                        Set.of(zeitblock.getTypeZeitintervall()));
    }

    /**
     * Diese Methode extrahiert die Zeitintervalle für die Zeitauswahl bezüglich Spitzenstunde.
     * <p>
     * Anhand der Informationen in den {@link OptionsDTO} wird die relevante Spitzenstunde extrahiert.
     * Diese Spitzenstunde dient mit der
     * {@link Zeitintervall}#getStartUhrzeit() und der {@link Zeitintervall}#getEndeUhrzeit() als
     * Zeitbasis zur Ermittlung der Summen über die vier 15-minütigen
     * Zeitintervalle je Verkehrsbeziehung.
     *
     * @param zaehlung zur Extraktion der {@link Zeitintervall}e aus der Datenbank.
     * @param options zur Extraktion der {@link Zeitintervall}e aus der Datenbank.
     * @return der {@link Zeitintervall} der Spitzenstunde.
     */
    public List<Zeitintervall> extractZeitintervalleSpitzenstunde(
            final Zaehlung zaehlung,
            final OptionsDTO options) {
        final TypeZeitintervall chosenSpitzenstunde;
        if (LadeZaehldatenService.ZEITAUSWAHL_SPITZENSTUNDE_KFZ.equals(options.getZeitauswahl())) {
            chosenSpitzenstunde = TypeZeitintervall.SPITZENSTUNDE_KFZ;
        } else if (LadeZaehldatenService.ZEITAUSWAHL_SPITZENSTUNDE_RAD.equals(options.getZeitauswahl())) {
            chosenSpitzenstunde = TypeZeitintervall.SPITZENSTUNDE_RAD;
        } else {
            chosenSpitzenstunde = TypeZeitintervall.SPITZENSTUNDE_FUSS;
        }
        final var zaehlart = Zaehlart.valueOf(zaehlung.getZaehlart());
        final List<Zeitintervall> spitzenstunden = ladeZaehldatenService.extractZeitintervalleSpitzenstundeFor15MinuteIntervals(
                UUID.fromString(zaehlung.getId()),
                zaehlart,
                zaehlung.getKreisverkehr(),
                options);
        if (!spitzenstunden.isEmpty()) {

            /*
             * Bei Auswahl des Zeitblocks für den gesamten Tag werden alle Spitzenstunden zurückgegeben.
             * d.h. die Spitzenstunden je Zeitblock und die Spitzenstunde über den ganzen Tag.
             * Hier ist dann die am Ende der Liste befindliche Spitzenstunde über den ganzen Tag zu extrahieren.
             *
             * Bei Auswahl eines bestimmten Zeitblocks (nicht gesamter Tag) wird nur diese eine Spitzenstunde
             * in der Liste zurückgegeben. Diese wird ebenfalls vom Ende der Liste extrahiert.
             */
            final Zeitintervall spitzenstunde = spitzenstunden.getLast();
            final List<Zeitintervall> zeitintervalle = zeitintervallRepository
                    .findByZaehlungIdAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndTypeInOrderBySortingIndexAsc(
                            UUID.fromString(zaehlung.getId()),
                            spitzenstunde.getStartUhrzeit(),
                            spitzenstunde.getEndeUhrzeit(),
                            // Spitzenstunden werden immer auf Basis der 15-Minuten-Intervalle ermittelt.
                            Set.of(TypeZeitintervall.STUNDE_VIERTEL));

            return ZeitintervallGleitendeSpitzenstundeUtil
                    .getGleitendeSpitzenstundenByBewegungsbeziehung(
                            UUID.fromString(zaehlung.getId()),
                            options.getZeitblock(),
                            zaehlart,
                            zeitintervalle,
                            Set.of(chosenSpitzenstunde))
                    .stream()
                    .peek(zeitintervall -> {
                        zeitintervall.setStartUhrzeit(spitzenstunde.getStartUhrzeit());
                        zeitintervall.setEndeUhrzeit(spitzenstunde.getEndeUhrzeit());
                    })
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
                    .toList();
        } else {
            return List.of();
        }
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
        if (zaehlstelleOptional.isEmpty()) {
            throw new DataNotFoundException("Die Zählstelle für Zählung " + zaehlungId + " wurde nicht gefunden");
        }
        final Optional<Zaehlung> zaehlungOptional = zaehlstelleOptional.get().getZaehlungen().stream()
                .filter(zaehlungToCheck -> zaehlungToCheck.getId().equals(zaehlungId))
                .findFirst();
        if (zaehlungOptional.isEmpty()) {
            throw new DataNotFoundException("Die Zählung " + zaehlungId + " wurde nicht gefunden");
        }
        return zaehlungOptional.get();
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
        belastungsplanData.setValues(BelastungsplanCalculator.subtractMatrice(basis.getValues(), vergleich.getValues()));

        belastungsplanData.setSum(subtractSums(basis.getSum(), vergleich.getSum()));
        belastungsplanData.setSumIn(subtractSums(basis.getSumIn(), vergleich.getSumIn()));
        belastungsplanData.setSumOut(subtractSums(basis.getSumOut(), vergleich.getSumOut()));

        return belastungsplanData;
    }

    private static BigDecimal[] subtractSums(final BigDecimal[] basis, final BigDecimal[] vergleich) {
        final int minLength = Math.min(basis.length, vergleich.length);
        final BigDecimal[] differences = new BigDecimal[minLength];
        for (int i = 0; i < minLength; i++) {
            differences[i] = basis[i].subtract(vergleich[i]);
        }
        return differences;
    }

    private LadeBelastungsplanDTO castLadeBelastungsplanDTO(AbstractLadeBelastungsplanDTO<?> ladeBelastungsplanDTO) {
        if (!(ladeBelastungsplanDTO instanceof LadeBelastungsplanDTO))
            throw new IllegalStateException("Fehler beim Erstellen der Belastungsplandaten");
        return (LadeBelastungsplanDTO) ladeBelastungsplanDTO;
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
