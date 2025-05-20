/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2023
 */
package de.muenchen.dave.util.messstelle;

import de.muenchen.dave.domain.dtos.laden.messwerte.LadeMesswerteDTO;
import de.muenchen.dave.domain.enums.Zeitblock;
import de.muenchen.dave.geodateneai.gen.model.IntervalDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MesswerteBaseUtil {

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
}
