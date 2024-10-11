/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.util.dataimport;

import de.muenchen.dave.domain.Fahrbeziehung;
import de.muenchen.dave.domain.Hochrechnung;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.enums.TypeZeitintervall;
import de.muenchen.dave.domain.enums.Zeitblock;
import de.muenchen.dave.util.CalculationUtil;
import de.muenchen.dave.util.DaveConstants;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ZeitintervallBaseUtil {

    public static final LocalDateTime TIME_VALUE_FOUND_END_OF_DAY = LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(23, 59));

    public static final LocalDateTime TIME_VALUE_FOUND_START_OF_DAY = LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(0, 0));

    /**
     * Diese Methode erstellt die grundlegende Datenstruktur zur weiteren Verarbeitung der {@link Zeitintervall}e.
     *
     * @param zeitintervalle Die Zeitintervalle aus denen die Datenstruktur erstellt werden soll.
     * @return Datenstruktur mit {@link Zeitintervall}e gruppiert nach den entsprechenden {@link Intervall}en.
     */
    public static Map<Intervall, List<Zeitintervall>> createByIntervallGroupedZeitintervalle(final List<Zeitintervall> zeitintervalle) {
        final Map<Intervall, List<Zeitintervall>> zeitintervalleGroupedByIntervall = new TreeMap<>();
        zeitintervalle.forEach(zeitintervall -> {
            final Intervall intervallKey = new Intervall(zeitintervall.getStartUhrzeit(), zeitintervall.getEndeUhrzeit());
            if (zeitintervalleGroupedByIntervall.containsKey(intervallKey)) {
                zeitintervalleGroupedByIntervall.get(intervallKey).add(zeitintervall);
            } else {
                final ArrayList<Zeitintervall> zeitintervalleJeIntervall = new ArrayList<>();
                zeitintervalleJeIntervall.add(zeitintervall);
                zeitintervalleGroupedByIntervall.put(intervallKey, zeitintervalleJeIntervall);
            }
        });
        return zeitintervalleGroupedByIntervall;
    }

    public static Zeitintervall createZeitintervallWithoutCountingValues(final UUID zaehlungId,
            final LocalDateTime startUhrzeit,
            final LocalDateTime endeUhrzeit,
            final TypeZeitintervall type) {
        final Zeitintervall zeitintervall = new Zeitintervall();
        zeitintervall.setZaehlungId(zaehlungId);
        zeitintervall.setStartUhrzeit(startUhrzeit);
        zeitintervall.setEndeUhrzeit(endeUhrzeit);
        zeitintervall.setHochrechnung(new Hochrechnung());
        zeitintervall.setFahrbeziehung(new Fahrbeziehung());
        zeitintervall.setType(type);
        return zeitintervall;
    }

    public static Zeitintervall createZeitintervallWithoutCountingValues(final UUID zaehlungId,
            final LocalDateTime startUhrzeit,
            final LocalDateTime endeUhrzeit,
            final TypeZeitintervall type,
            final Fahrbeziehung fahrbeziehung) {
        final Zeitintervall zeitintervall = createZeitintervallWithoutCountingValues(zaehlungId, startUhrzeit, endeUhrzeit, type);
        zeitintervall.setFahrbeziehung(fahrbeziehung);
        return zeitintervall;
    }

    public static Zeitintervall summation(final Zeitintervall zeitintervall1,
            final Zeitintervall zeitintervall2) {
        final Zeitintervall summedZeitintervall = ZeitintervallBaseUtil.createZeitintervallWithoutCountingValues(
                zeitintervall1.getZaehlungId(),
                zeitintervall1.getStartUhrzeit(),
                zeitintervall1.getEndeUhrzeit(),
                zeitintervall1.getType(),
                zeitintervall1.getFahrbeziehung());
        summedZeitintervall.setPkw(CalculationUtil.nullSafeSummation(zeitintervall1.getPkw(), zeitintervall2.getPkw()));
        summedZeitintervall.setLkw(CalculationUtil.nullSafeSummation(zeitintervall1.getLkw(), zeitintervall2.getLkw()));
        summedZeitintervall.setLastzuege(CalculationUtil.nullSafeSummation(zeitintervall1.getLastzuege(), zeitintervall2.getLastzuege()));
        summedZeitintervall.setBusse(CalculationUtil.nullSafeSummation(zeitintervall1.getBusse(), zeitintervall2.getBusse()));
        summedZeitintervall.setKraftraeder(CalculationUtil.nullSafeSummation(zeitintervall1.getKraftraeder(), zeitintervall2.getKraftraeder()));
        summedZeitintervall.setFahrradfahrer(CalculationUtil.nullSafeSummation(zeitintervall1.getFahrradfahrer(), zeitintervall2.getFahrradfahrer()));
        summedZeitintervall.setFussgaenger(CalculationUtil.nullSafeSummation(zeitintervall1.getFussgaenger(), zeitintervall2.getFussgaenger()));
        summedZeitintervall.getHochrechnung().setHochrechnungKfz(
                CalculationUtil.nullSafeSummation(
                        zeitintervall1.getHochrechnung().getHochrechnungKfz(),
                        zeitintervall2.getHochrechnung().getHochrechnungKfz()));
        summedZeitintervall.getHochrechnung().setHochrechnungGv(
                CalculationUtil.nullSafeSummation(
                        zeitintervall1.getHochrechnung().getHochrechnungGv(),
                        zeitintervall2.getHochrechnung().getHochrechnungGv()));
        summedZeitintervall.getHochrechnung().setHochrechnungSv(
                CalculationUtil.nullSafeSummation(
                        zeitintervall1.getHochrechnung().getHochrechnungSv(),
                        zeitintervall2.getHochrechnung().getHochrechnungSv()));
        summedZeitintervall.getHochrechnung().setHochrechnungRad(
                CalculationUtil.nullSafeSummation(
                        zeitintervall1.getHochrechnung().getHochrechnungRad(),
                        zeitintervall2.getHochrechnung().getHochrechnungRad()));
        return summedZeitintervall;
    }

    /**
     * @param zeitintervalle Die Zeitintervalle zur extraktion der Fahrbeziehungen.
     * @return Die möglichen Fahrbeziehungen aller Zeitintervalle.
     */
    public static Set<Fahrbeziehung> getAllPossibleFahrbeziehungen(List<Zeitintervall> zeitintervalle) {
        return zeitintervalle.stream()
                .map(Zeitintervall::getFahrbeziehung)
                .collect(Collectors.toSet());
    }

    /**
     * @param fahrbeziehung                    Die {@link Fahrbeziehung} der betroffenen Zeitintervalle.
     * @param zeitintervalleGroupedByIntervall Die nach {@link ZeitintervallBaseUtil.Intervall} gruppierten {@link Zeitintervall}e.
     * @return Alle {@link Zeitintervall}e welche die {@link Fahrbeziehung} besitzen.
     */
    public static List<Zeitintervall> getZeitintervalleForFahrbeziehung(final Fahrbeziehung fahrbeziehung,
            final Map<ZeitintervallBaseUtil.Intervall, List<Zeitintervall>> zeitintervalleGroupedByIntervall) {
        final List<Zeitintervall> zeitintervalleForFahrbeziehung = new ArrayList<>();
        zeitintervalleGroupedByIntervall.keySet().forEach(intervall -> {
            zeitintervalleGroupedByIntervall.get(intervall).stream()
                    .filter(zeitintervall -> zeitintervall.getFahrbeziehung().equals(fahrbeziehung))
                    .findFirst()
                    .ifPresent(zeitintervalleForFahrbeziehung::add);
        });
        return zeitintervalleForFahrbeziehung;
    }

    public static boolean isZeitintervallWithinZeitblock(final Zeitintervall zeitintervall, final Zeitblock zeitblock) {
        return isZeitintervallWithinTimeParameters(zeitintervall, zeitblock.getStart(), zeitblock.getEnd());
    }

    private static boolean isZeitintervallWithinTimeParameters(final Zeitintervall zeitintervall,
            final LocalDateTime startTime,
            final LocalDateTime endTime) {
        return (zeitintervall.getStartUhrzeit().equals(startTime) || zeitintervall.getStartUhrzeit().isAfter(startTime))
                && isZeitintervallBeforeTimeParameters(zeitintervall, endTime);
    }

    private static boolean isZeitintervallBeforeTimeParameters(final Zeitintervall zeitintervall,
            final LocalDateTime endTime) {
        return (zeitintervall.getEndeUhrzeit().equals(endTime) || zeitintervall.getEndeUhrzeit().isBefore(endTime))
                && !(zeitintervall.getStartUhrzeit().equals(endTime) || zeitintervall.getStartUhrzeit().isAfter(endTime));
    }

    public static Zeitintervall checkAndCorrectEndeuhrzeitForLastZeitintervallOfDayIfNecessary(final Zeitintervall zeitintervall) {
        if ((zeitintervall.getEndeUhrzeit().equals(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.MAX)))
                || (zeitintervall.getEndeUhrzeit().isBefore(zeitintervall.getStartUhrzeit())
                && zeitintervall.getEndeUhrzeit().equals(TIME_VALUE_FOUND_START_OF_DAY))) {
            zeitintervall.setEndeUhrzeit(TIME_VALUE_FOUND_END_OF_DAY);
        }
        return zeitintervall;
    }

    @AllArgsConstructor
    @Getter
    @Setter
    @ToString
    @EqualsAndHashCode
    public static class Intervall implements Comparable<Intervall> {

        private LocalDateTime startUhrzeit;

        private LocalDateTime endeUhrzeit;

        public int compareTo(final Intervall intervall) {
            return startUhrzeit.compareTo(intervall.getStartUhrzeit())
                    + endeUhrzeit.compareTo(intervall.getEndeUhrzeit());
        }

    }

}
