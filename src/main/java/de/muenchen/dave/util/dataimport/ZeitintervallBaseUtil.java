package de.muenchen.dave.util.dataimport;

import de.muenchen.dave.domain.Bewegungsbeziehung;
import de.muenchen.dave.domain.Hochrechnung;
import de.muenchen.dave.domain.Laengsverkehr;
import de.muenchen.dave.domain.Querungsverkehr;
import de.muenchen.dave.domain.Verkehrsbeziehung;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.enums.TypeZeitintervall;
import de.muenchen.dave.domain.enums.Zeitblock;
import de.muenchen.dave.util.CalculationUtil;
import de.muenchen.dave.util.DaveConstants;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.ObjectUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ZeitintervallBaseUtil {

    public static final LocalDateTime TIME_VALUE_FOUND_END_OF_DAY = LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(23, 59));

    public static final LocalDateTime TIME_VALUE_FOUND_START_OF_DAY = LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(0, 0));

    /**
     * Diese Methode erstellt die grundlegende Datenstruktur zur weiteren Verarbeitung der
     * {@link Zeitintervall}e.
     *
     * @param zeitintervalle Die Zeitintervalle aus denen die Datenstruktur erstellt werden soll.
     * @return Datenstruktur mit {@link Zeitintervall}e gruppiert nach den entsprechenden
     *         {@link Intervall}en.
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
        zeitintervall.setType(type);
        return zeitintervall;
    }

    public static Zeitintervall createZeitintervallWithoutCountingValues(final UUID zaehlungId,
            final LocalDateTime startUhrzeit,
            final LocalDateTime endeUhrzeit,
            final TypeZeitintervall type,
            final Verkehrsbeziehung verkehrsbeziehung,
            final Laengsverkehr laengsverkehr,
            final Querungsverkehr querungsverkehr) {
        final Zeitintervall zeitintervall = createZeitintervallWithoutCountingValues(zaehlungId, startUhrzeit, endeUhrzeit, type);
        zeitintervall.setVerkehrsbeziehung(verkehrsbeziehung);
        zeitintervall.setLaengsverkehr(laengsverkehr);
        zeitintervall.setQuerungsverkehr(querungsverkehr);
        return zeitintervall;
    }

    public static Zeitintervall summation(
            final Zeitintervall zeitintervall1,
            final Zeitintervall zeitintervall2) {
        final Zeitintervall summedZeitintervall = ZeitintervallBaseUtil.createZeitintervallWithoutCountingValues(
                zeitintervall1.getZaehlungId(),
                zeitintervall1.getStartUhrzeit(),
                zeitintervall1.getEndeUhrzeit(),
                zeitintervall1.getType(),
                zeitintervall1.getVerkehrsbeziehung(),
                zeitintervall1.getLaengsverkehr(),
                zeitintervall1.getQuerungsverkehr());
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

    public static Bewegungsbeziehung getBewegungbeziehung(final Zeitintervall zeitinterval) {
        if (Objects.nonNull(zeitinterval.getVerkehrsbeziehung())) {
            return zeitinterval.getVerkehrsbeziehung();
        } else if (Objects.nonNull(zeitinterval.getLaengsverkehr())) {
            return zeitinterval.getLaengsverkehr();
        } else {
            return zeitinterval.getQuerungsverkehr();
        }
    }

    /**
     * @param zeitintervalle Die Zeitintervalle zur extraktion der Bewegungsbeziehungen.
     * @return Die möglichen Bewegungsbeziehungen aller Zeitintervalle.
     */
    public static Set<Bewegungsbeziehung> getAllPossibleBewegungsbeziehungen(List<Zeitintervall> zeitintervalle) {
        return zeitintervalle.stream()
                .flatMap(zeitintervall -> Stream.of(zeitintervall.getVerkehrsbeziehung(), zeitintervall.getLaengsverkehr(), zeitintervall.getQuerungsverkehr()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    /**
     * @param bewegungsbeziehung Die {@link Bewegungsbeziehung} der betroffenen Zeitintervalle.
     * @param zeitintervalleGroupedByIntervall Die nach {@link ZeitintervallBaseUtil.Intervall}
     *            gruppierten {@link Zeitintervall}e.
     * @return Alle {@link Zeitintervall}e welche die {@link Bewegungsbeziehung} besitzen.
     */
    public static List<Zeitintervall> getZeitintervalleForBewegungsbeziehung(
            final Bewegungsbeziehung bewegungsbeziehung,
            final Map<ZeitintervallBaseUtil.Intervall, List<Zeitintervall>> zeitintervalleGroupedByIntervall) {
        return zeitintervalleGroupedByIntervall.values()
                .stream()
                .flatMap(List::stream)
                .filter(zeitintervall -> containsZeitintervallSameBewegungsbeziehungWichIsNonNull(zeitintervall, bewegungsbeziehung))
                .toList();
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

    public static boolean containsZeitintervallSameBewegungsbeziehungWichIsNonNull(
            final Zeitintervall zeitintervall1,
            final Bewegungsbeziehung bewegungsbeziehung) {
        return Objects.nonNull(bewegungsbeziehung)
                && (isSameBewegungsbeziehungAndBothBewegungsbeziehungAreNotNull(zeitintervall1.getVerkehrsbeziehung(), bewegungsbeziehung)
                        || isSameBewegungsbeziehungAndBothBewegungsbeziehungAreNotNull(zeitintervall1.getLaengsverkehr(), bewegungsbeziehung)
                        || isSameBewegungsbeziehungAndBothBewegungsbeziehungAreNotNull(zeitintervall1.getQuerungsverkehr(), bewegungsbeziehung));
    }

    public static boolean haveBothZeitintervallSameBewegungsbeziehung(final Zeitintervall zeitintervall1, final Zeitintervall zeitintervall2) {
        return isSameBewegungsbeziehungOrBothNull(zeitintervall1.getVerkehrsbeziehung(), zeitintervall2.getVerkehrsbeziehung())
                && isSameBewegungsbeziehungOrBothNull(zeitintervall1.getLaengsverkehr(), zeitintervall2.getLaengsverkehr())
                && isSameBewegungsbeziehungOrBothNull(zeitintervall1.getQuerungsverkehr(), zeitintervall2.getQuerungsverkehr());
    }

    public static boolean isSameBewegungsbeziehungOrBothNull(final Bewegungsbeziehung bewegungsbeziehung1, final Bewegungsbeziehung bewegungsbeziehung2) {
        return ObjectUtils.allNull(bewegungsbeziehung1, bewegungsbeziehung2)
                || isSameBewegungsbeziehungAndBothBewegungsbeziehungAreNotNull(bewegungsbeziehung1, bewegungsbeziehung2);
    }

    public static boolean isSameBewegungsbeziehungAndBothBewegungsbeziehungAreNotNull(final Bewegungsbeziehung bewegungsbeziehung1,
            final Bewegungsbeziehung bewegungsbeziehung2) {
        return ObjectUtils.allNotNull(bewegungsbeziehung1, bewegungsbeziehung2)
                && bewegungsbeziehung1.equals(bewegungsbeziehung2);
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
