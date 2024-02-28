/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2023
 */
package de.muenchen.dave.util.messstelle;

import de.muenchen.dave.domain.dtos.laden.messwerte.LadeMesswerteDTO;
import de.muenchen.dave.domain.enums.Zeitblock;
import de.muenchen.dave.geodateneai.gen.model.MeasurementValuesPerInterval;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MesswerteBaseUtil {

    public static boolean isTimeWithinBlock(final LocalTime toCheck, final Zeitblock block) {
        return isTimeBetweenStartAndEnd(toCheck, block.getStart().toLocalTime(), block.getEnd().toLocalTime());
    }

    public static boolean isTimeBetweenStartAndEnd(final LocalTime toCheck, final LocalTime start, final LocalTime end) {
        return (toCheck.isAfter(start) || toCheck.equals(start)) && toCheck.isBefore(end);
    }

    public static boolean isZeitintervallWithinZeitblock(final LadeMesswerteDTO zeitintervall, final Zeitblock zeitblock) {
        return isZeitintervallWithinTimeParameters(zeitintervall, zeitblock.getStart().toLocalTime(), zeitblock.getEnd().toLocalTime());
    }

    private static boolean isZeitintervallWithinTimeParameters(final LadeMesswerteDTO zeitintervall,
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

    public static LadeMesswerteDTO calculateSum(final List<MeasurementValuesPerInterval> intervals) {
        final LadeMesswerteDTO dto = new LadeMesswerteDTO();
        dto.setPkw(intervals.stream().mapToInt(MeasurementValuesPerInterval::getSummeAllePkw).sum());
        dto.setLkw(intervals.stream().mapToInt(MeasurementValuesPerInterval::getAnzahlLkw).sum());
        dto.setLfw(intervals.stream().mapToInt(MeasurementValuesPerInterval::getAnzahlLfw).sum());
        dto.setLastzuege(intervals.stream().mapToInt(MeasurementValuesPerInterval::getSummeLastzug).sum());
        dto.setBusse(intervals.stream().mapToInt(MeasurementValuesPerInterval::getAnzahlBus).sum());
        dto.setKraftraeder(intervals.stream().mapToInt(MeasurementValuesPerInterval::getAnzahlKrad).sum());
        dto.setFahrradfahrer(intervals.stream().mapToInt(MeasurementValuesPerInterval::getAnzahlRad).sum());
        dto.setKfz(intervals.stream().mapToInt(MeasurementValuesPerInterval::getSummeKraftfahrzeugverkehr).sum());
        dto.setSchwerverkehr(intervals.stream().mapToInt(MeasurementValuesPerInterval::getSummeSchwerverkehr).sum());
        dto.setGueterverkehr(intervals.stream().mapToInt(MeasurementValuesPerInterval::getSummeGueterverkehr).sum());
        dto.setAnteilSchwerverkehrAnKfzProzent(calculateAnteilProzent(dto.getSchwerverkehr(), dto.getKfz()));
        dto.setAnteilGueterverkehrAnKfzProzent(calculateAnteilProzent(dto.getGueterverkehr(), dto.getKfz()));
        return dto;
    }

    protected static double calculateAnteilProzent(final Integer dividend, final Integer divisor) {
        final double percentage = (Double.valueOf(dividend) / divisor) * 100;
        return BigDecimal.valueOf(percentage).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }
}
