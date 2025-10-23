package de.muenchen.dave.util.messstelle;

import de.muenchen.dave.domain.dtos.laden.messwerte.LadeMesswerteDTO;
import de.muenchen.dave.domain.enums.Zeitblock;
import de.muenchen.dave.geodateneai.gen.model.IntervalDto;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MesswerteBaseUtil {

    public static boolean isDateRange(final List<LocalDate> zeitraum) {
        return CollectionUtils.isNotEmpty(zeitraum) && zeitraum.size() == 2 && !zeitraum.getFirst().isEqual(zeitraum.getLast());
    }

    public static boolean isIntervalWithingZeitblock(final IntervalDto interval, final Zeitblock zeitblock) {
        return isTimeWithinZeitblock(interval.getDatumUhrzeitVon().toLocalTime(), zeitblock)
                && isTimeWithinZeitblock(interval.getDatumUhrzeitBis().toLocalTime(), zeitblock);
    }

    static boolean isTimeWithinZeitblock(final LocalTime toCheck, final Zeitblock zeitblock) {
        return isTimeWithinStartAndEnd(toCheck, zeitblock.getStart().toLocalTime(), zeitblock.getEnd().toLocalTime());
    }

    public static boolean isIntervalWithinStartAndEnd(final IntervalDto interval, final LocalTime start, final LocalTime end) {
        return isTimeWithinStartAndEnd(interval.getDatumUhrzeitVon().toLocalTime(), start, end)
                && isTimeWithinStartAndEnd(interval.getDatumUhrzeitBis().toLocalTime(), start, end);
    }

    static boolean isTimeWithinStartAndEnd(final LocalTime toCheck, final LocalTime start, final LocalTime end) {
        return (toCheck.isAfter(start) || toCheck.equals(start))
                && (toCheck.isBefore(end) || toCheck.equals(end));
    }

    public static boolean isZeitintervallWithinZeitblock(final LadeMesswerteDTO zeitintervall, final Zeitblock zeitblock) {
        return isZeitintervallWithinTimeParameters(zeitintervall, zeitblock.getStart().toLocalTime(), zeitblock.getEnd().toLocalTime());
    }

    private static boolean isZeitintervallWithinTimeParameters(
            final LadeMesswerteDTO zeitintervall,
            final LocalTime startTime,
            final LocalTime endTime) {
        return (zeitintervall.getStartUhrzeit().equals(startTime) || zeitintervall.getStartUhrzeit().isAfter(startTime))
                && isZeitintervallBeforeTimeParameters(zeitintervall, endTime);
    }

    private static boolean isZeitintervallBeforeTimeParameters(final LadeMesswerteDTO zeitintervall,
            final LocalTime endTime) {
        return (zeitintervall.getEndeUhrzeit().equals(endTime) || zeitintervall.getEndeUhrzeit().isBefore(endTime))
                && !(zeitintervall.getStartUhrzeit().equals(endTime) || zeitintervall.getStartUhrzeit().isAfter(endTime));
    }

    public static LadeMesswerteDTO calculateSum(final List<IntervalDto> intervals) {
        final LadeMesswerteDTO dto = new LadeMesswerteDTO();
        dto.setPkw(intervals.stream().mapToInt(interval -> ObjectUtils.defaultIfNull(interval.getSummeAllePkw(), BigDecimal.ZERO).intValue()).sum());
        dto.setLkw(intervals.stream().mapToInt(interval -> ObjectUtils.defaultIfNull(interval.getAnzahlLkw(), BigDecimal.ZERO).intValue()).sum());
        dto.setLfw(intervals.stream().mapToInt(interval -> ObjectUtils.defaultIfNull(interval.getAnzahlLfw(), BigDecimal.ZERO).intValue()).sum());
        dto.setLastzuege(intervals.stream().mapToInt(interval -> ObjectUtils.defaultIfNull(interval.getSummeLastzug(), BigDecimal.ZERO).intValue()).sum());
        dto.setBusse(intervals.stream().mapToInt(interval -> ObjectUtils.defaultIfNull(interval.getAnzahlBus(), BigDecimal.ZERO).intValue()).sum());
        dto.setKraftraeder(intervals.stream().mapToInt(interval -> ObjectUtils.defaultIfNull(interval.getAnzahlKrad(), BigDecimal.ZERO).intValue()).sum());
        dto.setFahrradfahrer(intervals.stream().mapToInt(interval -> ObjectUtils.defaultIfNull(interval.getAnzahlRad(), BigDecimal.ZERO).intValue()).sum());
        dto.setKfz(
                intervals.stream().mapToInt(interval -> ObjectUtils.defaultIfNull(interval.getSummeKraftfahrzeugverkehr(), BigDecimal.ZERO).intValue()).sum());
        dto.setSchwerverkehr(
                intervals.stream().mapToInt(interval -> ObjectUtils.defaultIfNull(interval.getSummeSchwerverkehr(), BigDecimal.ZERO).intValue()).sum());
        dto.setGueterverkehr(
                intervals.stream().mapToInt(interval -> ObjectUtils.defaultIfNull(interval.getSummeGueterverkehr(), BigDecimal.ZERO).intValue()).sum());
        dto.setAnteilSchwerverkehrAnKfzProzent(calculateAnteilProzent(dto.getSchwerverkehr(), dto.getKfz()));
        dto.setAnteilGueterverkehrAnKfzProzent(calculateAnteilProzent(dto.getGueterverkehr(), dto.getKfz()));
        return dto;
    }

    public static double calculateAnteilProzent(final Integer dividend, final Integer divisor) {
        return divisor == null || divisor == 0
                ? 0D
                : BigDecimal.valueOf(ObjectUtils.defaultIfNull(dividend, 0))
                        .divide(BigDecimal.valueOf(divisor), 3, RoundingMode.HALF_UP)
                        .scaleByPowerOfTen(2)
                        .doubleValue();
    }

    /**
     * Die Methode summiert die in den Parameter gegebenen Intervalle.
     * <p>
     * Der Messquerschnitt und der Tagestyp für den summierten Interval
     * wird aus dem zweiten Interval entnommen.
     * <p>
     * Das Attribut {@link IntervalDto#getDatumUhrzeitVon()} des summierten Intervalls wird auf das
     * kleinere gleichlautende Attribut der im Parameter gegebenen Intervalle gesetzt.
     * <p>
     * Das Attribut {@link IntervalDto#getDatumUhrzeitBis()} des summierten Intervalls wird auf das
     * größere gleichlautende Attribut der im Parameter gegebenen Intervalle gesetzt.
     *
     * @param interval1 zum Summieren
     * @param interval2 zum Summieren
     * @return einen neuen Interval mit den summierten Werten
     */
    public static IntervalDto sumIntervalsAndAdaptDatumUhrzeitVonAndBisAndReturnNewInterval(
            final IntervalDto interval1,
            final IntervalDto interval2) {
        final var interval = sumCountingValuesOfIntervalsAndReturnNewInterval(interval1, interval2);
        interval.setMqId(interval2.getMqId());
        interval.setTagesTyp(interval2.getTagesTyp());
        final var intervalVon = getMin(interval1.getDatumUhrzeitVon(), interval2.getDatumUhrzeitVon());
        interval.setDatumUhrzeitVon(intervalVon);
        final var intervalBis = getMax(interval1.getDatumUhrzeitBis(), interval2.getDatumUhrzeitBis());
        interval.setDatumUhrzeitBis(intervalBis);
        return interval;
    }

    public static IntervalDto sumCountingValuesOfIntervalsAndReturnNewInterval(
            final IntervalDto interval1,
            final IntervalDto interval2) {
        return sumCountingValuesOfAggregatesAndReturnNewTagesaggregatModelForMesswerte(interval1, interval2);
    }

    private static IntervalDto sumCountingValuesOfAggregatesAndReturnNewTagesaggregatModelForMesswerte(
            final IntervalDto interval1,
            final IntervalDto interval2) {
        final var interval = new IntervalDto();
        interval.setAnzahlLfw(sumValuesIfAnyNotNullOrReturnNull(interval1.getAnzahlLfw(), interval2.getAnzahlLfw()));
        interval.setAnzahlKrad(sumValuesIfAnyNotNullOrReturnNull(interval1.getAnzahlKrad(), interval2.getAnzahlKrad()));
        interval.setAnzahlLkw(sumValuesIfAnyNotNullOrReturnNull(interval1.getAnzahlLkw(), interval2.getAnzahlLkw()));
        interval.setAnzahlBus(sumValuesIfAnyNotNullOrReturnNull(interval1.getAnzahlBus(), interval2.getAnzahlBus()));
        interval.setAnzahlRad(sumValuesIfAnyNotNullOrReturnNull(interval1.getAnzahlRad(), interval2.getAnzahlRad()));
        interval.setSummeAllePkw(sumValuesIfAnyNotNullOrReturnNull(interval1.getSummeAllePkw(), interval2.getSummeAllePkw()));
        interval.setSummeLastzug(sumValuesIfAnyNotNullOrReturnNull(interval1.getSummeLastzug(), interval2.getSummeLastzug()));
        interval.setSummeGueterverkehr(sumValuesIfAnyNotNullOrReturnNull(interval1.getSummeGueterverkehr(), interval2.getSummeGueterverkehr()));
        interval.setSummeSchwerverkehr(sumValuesIfAnyNotNullOrReturnNull(interval1.getSummeSchwerverkehr(), interval2.getSummeSchwerverkehr()));
        interval.setSummeKraftfahrzeugverkehr(
                sumValuesIfAnyNotNullOrReturnNull(interval1.getSummeKraftfahrzeugverkehr(), interval2.getSummeKraftfahrzeugverkehr()));
        return interval;
    }

    public static BigDecimal sumValuesIfAnyNotNullOrReturnNull(final BigDecimal... values) {
        BigDecimal summedValue = null;
        if (ObjectUtils.anyNotNull(values)) {
            summedValue = Stream.of(values)
                    .reduce(BigDecimal.ZERO,
                            (value1, value2) -> ObjectUtils.defaultIfNull(value1, BigDecimal.ZERO).add(ObjectUtils.defaultIfNull(value2, BigDecimal.ZERO)));
        }
        return summedValue;
    }

    static LocalDateTime getMin(final LocalDateTime dateTime1, final LocalDateTime dateTime2) {
        return Stream.of(dateTime1, dateTime2)
                .filter(ObjectUtils::isNotEmpty)
                .min(Comparator.naturalOrder())
                .orElse(null);
    }

    static LocalDateTime getMax(final LocalDateTime dateTime1, final LocalDateTime dateTime2) {
        return Stream.of(dateTime1, dateTime2)
                .filter(ObjectUtils::isNotEmpty)
                .max(Comparator.naturalOrder())
                .orElse(null);
    }

}
