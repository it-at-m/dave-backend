/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.util.dataimport;

import de.muenchen.dave.domain.Hochrechnung;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.enums.TypeZeitintervall;
import de.muenchen.dave.domain.enums.Zeitblock;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.ObjectUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Hilfklasse zur Ermittlung der gleitenden Spitzenstunde
 * in {@link ZeitintervallGleitendeSpitzenstundeUtil}.
 * <p>
 * Das Ergebnis der Klasse wird zur Prüfung bezüglich der Spitzenstunde in
 * {@link ZeitintervallGleitendeSpitzenstundeUtil}.berechneGleitendeSpitzenstunde(UUID, Zeitblock,
 * Fahrbeziehung, List) verwendet.
 */
@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PRIVATE)
@EqualsAndHashCode
@ToString
public class GleitenderZeitintervall {

    private static final int DEFAULT_MINUTES_ZEITINTERVALL = 15;

    private static final int MINUTES_PER_HOUR = 60;

    private final int zeitintervallePerHour;

    private final List<Zeitintervall> zeitintervalle;

    private GleitenderZeitintervall(final int zeitintervallePerHour) {
        this.zeitintervallePerHour = zeitintervallePerHour;
        this.zeitintervalle = new ArrayList<>(zeitintervallePerHour);
    }

    private static GleitenderZeitintervall createEmptyInstance(final int zeitintervallePerHour) {
        return new GleitenderZeitintervall(zeitintervallePerHour);
    }

    /**
     * Diese Methode ermittelt die Zeitintervalle auf Basis der <code>sortedZeitintervalle</code> welche
     * zusammen 1h umfassen.
     * Die Ermittlung startet rückwirkend beginnend bei der Position <code>index</code> in den
     * <code>sortedZeitintervalle</code>.
     * Es werden in <code>sortedZeitintervalle</code> nur die Zeitintervalle berücksichtigt, welche
     * innerhalb des in
     * Parameter <code>zeitblock</code> definierten Zeitblocks liegen.
     *
     * @param sortedZeitintervalle in aufsteigend sortierter Reihenfolge
     * @param index in den <code>sortedZeitintervalle</code>.
     * @param zeitblock zur Prüfung auf relevante Zeitintervalle.
     * @return den {@link GleitenderZeitintervall} bestehen aus den Zeitintervallen welche rückwirkend
     *         beginnend bei
     *         Position <code>index</code> 1h umfassen,
     */
    public static GleitenderZeitintervall createInstanceWithIndexParameterAsNewestIndex(final List<Zeitintervall> sortedZeitintervalle,
            final int index,
            final Zeitblock zeitblock) {
        final var zeitintervallePerHour = calcNumberOfZeitintervallePerHour(sortedZeitintervalle);
        final GleitenderZeitintervall gleitenderZeitintervall = GleitenderZeitintervall.createEmptyInstance(zeitintervallePerHour);
        /*
         * Rückwärtiges iterieren über die Zeitintervalle beginnend bei Position gegeben in
         * Methodenparameter index.
         * Die Anzahl der Iterationen definiert sich aus den Zeitintervallen pro Stunde.
         * Des Weiteren werden nur die Zeitintervalle berücksichtigt die innerhalb des Parameters Zeitblock
         * liegen.
         */
        for (int hourIndex = zeitintervallePerHour - 1; hourIndex >= 0; hourIndex--) {
            final int positionZeitintervall = index - hourIndex;
            if (positionZeitintervall >= 0
                    && ZeitintervallBaseUtil.isZeitintervallWithinZeitblock(sortedZeitintervalle.get(positionZeitintervall), zeitblock)) {
                // Ist ein Zeitintervall gefunden, welcher innerhalb der einen Stunde und im Zeitblock liegt,
                // so wird dieser in die Liste aufgenommen.
                gleitenderZeitintervall.add(sortedZeitintervalle.get(positionZeitintervall));
            }
        }
        return gleitenderZeitintervall;
    }

    private void add(final Zeitintervall zeitintervall) {
        zeitintervalle.add(zeitintervall);
    }

    /**
     * @return Den Zeitintervall mit den Summen der einzelnen Zeitintervalle welche
     *         ermittelt in
     *         {@link GleitenderZeitintervall#createInstanceWithIndexParameterAsNewestIndex(List, int, Zeitblock)}
     *         zusammen 1h definieren.
     */
    public Zeitintervall getSummedZeitintervall() {
        final Zeitintervall summedZeitintervall = new Zeitintervall();
        summedZeitintervall.setStartUhrzeit(getStartUhrzeit());
        summedZeitintervall.setEndeUhrzeit(getEndeUhrzeit());
        summedZeitintervall.setPkw(getSumPkw());
        summedZeitintervall.setLkw(getSumLkw());
        summedZeitintervall.setLastzuege(getSumLastzuege());
        summedZeitintervall.setBusse(getSumBusse());
        summedZeitintervall.setKraftraeder(getSumKraftraeder());
        summedZeitintervall.setFahrradfahrer(getSumFahrradfahrer());
        summedZeitintervall.setFussgaenger(getSumFussgaenger());
        summedZeitintervall.setHochrechnung(new Hochrechnung());
        return summedZeitintervall;
    }

    /**
     * @return die Summe aus {@link GleitenderZeitintervall#getSummedZeitintervall()} mit
     *         {@link TypeZeitintervall#SPITZENSTUNDE_KFZ}.
     */
    public Zeitintervall getSummedZeitintervallKfz() {
        final Zeitintervall summedZeitintervall = getSummedZeitintervall();
        summedZeitintervall.setType(TypeZeitintervall.SPITZENSTUNDE_KFZ);
        return summedZeitintervall;
    }

    /**
     * @return die Summe aus {@link GleitenderZeitintervall#getSummedZeitintervall()} mit
     *         {@link TypeZeitintervall#SPITZENSTUNDE_RAD}.
     */
    public Zeitintervall getSummedZeitintervallRad() {
        final Zeitintervall summedZeitintervall = getSummedZeitintervall();
        summedZeitintervall.setType(TypeZeitintervall.SPITZENSTUNDE_RAD);
        return summedZeitintervall;
    }

    /**
     * @return die Summe aus {@link GleitenderZeitintervall#getSummedZeitintervall()} mit
     *         {@link TypeZeitintervall#SPITZENSTUNDE_FUSS}.
     */
    public Zeitintervall getSummedZeitintervallFuss() {
        final Zeitintervall summedZeitintervall = getSummedZeitintervall();
        summedZeitintervall.setType(TypeZeitintervall.SPITZENSTUNDE_FUSS);
        return summedZeitintervall;
    }

    public Integer getSumKfz() {
        return ObjectUtils.defaultIfNull(getSumPkw(), 0)
                + ObjectUtils.defaultIfNull(getSumLkw(), 0)
                + ObjectUtils.defaultIfNull(getSumLastzuege(), 0)
                + ObjectUtils.defaultIfNull(getSumBusse(), 0)
                + ObjectUtils.defaultIfNull(getSumKraftraeder(), 0);
    }

    private Integer getSumPkw() {
        final List<Integer> numberPkws = zeitintervalle.stream()
                .map(Zeitintervall::getPkw)
                .collect(Collectors.toList());
        return getSumOrNullWhenAllValuesNull(numberPkws);
    }

    private Integer getSumLkw() {
        final List<Integer> numberLkw = zeitintervalle.stream()
                .map(Zeitintervall::getLkw)
                .collect(Collectors.toList());
        return getSumOrNullWhenAllValuesNull(numberLkw);
    }

    private Integer getSumLastzuege() {
        final List<Integer> numberLz = zeitintervalle.stream()
                .map(Zeitintervall::getLastzuege)
                .collect(Collectors.toList());
        return getSumOrNullWhenAllValuesNull(numberLz);
    }

    private Integer getSumBusse() {
        final List<Integer> numberBusse = zeitintervalle.stream()
                .map(Zeitintervall::getBusse)
                .collect(Collectors.toList());
        return getSumOrNullWhenAllValuesNull(numberBusse);
    }

    private Integer getSumKraftraeder() {
        final List<Integer> numberKrad = zeitintervalle.stream()
                .map(Zeitintervall::getKraftraeder)
                .collect(Collectors.toList());
        return getSumOrNullWhenAllValuesNull(numberKrad);
    }

    public Integer getSumFahrradfahrer() {
        final List<Integer> numberRad = zeitintervalle.stream()
                .map(Zeitintervall::getFahrradfahrer)
                .collect(Collectors.toList());
        return getSumOrNullWhenAllValuesNull(numberRad);
    }

    public Integer getSumFussgaenger() {
        final List<Integer> numberFuss = zeitintervalle.stream()
                .map(Zeitintervall::getFussgaenger)
                .collect(Collectors.toList());
        return getSumOrNullWhenAllValuesNull(numberFuss);
    }

    private LocalDateTime getStartUhrzeit() {
        return zeitintervalle.stream()
                .map(Zeitintervall::getStartUhrzeit)
                .filter(ObjectUtils::isNotEmpty)
                .findFirst()
                .orElse(null);
    }

    private LocalDateTime getEndeUhrzeit() {
        if (zeitintervalle.isEmpty()) {
            return null;
        } else {
            return zeitintervalle.get(zeitintervalle.size() - 1).getEndeUhrzeit();
        }
    }

    private Integer getSumOrNullWhenAllValuesNull(final List<Integer> numbers) {
        final Integer result;
        if (ObjectUtils.allNull(numbers.toArray())) {
            result = null;
        } else {
            result = numbers.stream()
                    .mapToInt(number -> ObjectUtils.defaultIfNull(number, 0))
                    .sum();
        }
        return result;
    }

    public static int calcNumberOfZeitintervallePerHour(final List<Zeitintervall> zeitintervalle) {
        final var minutesPerZeitintervall = new AtomicLong(DEFAULT_MINUTES_ZEITINTERVALL);
        zeitintervalle.stream()
                .findFirst()
                .ifPresent(zeitintervall -> minutesPerZeitintervall.set(
                        zeitintervall.getStartUhrzeit().until(zeitintervall.getEndeUhrzeit(), ChronoUnit.MINUTES)));
        return MINUTES_PER_HOUR / minutesPerZeitintervall.intValue();
    }

}
