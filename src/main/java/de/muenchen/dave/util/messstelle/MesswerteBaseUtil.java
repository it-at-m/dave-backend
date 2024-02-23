/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.util.messstelle;

import de.muenchen.dave.domain.dtos.laden.messwerte.LadeMesswerteDTO;
import de.muenchen.dave.domain.enums.Zeitblock;
import java.time.LocalTime;
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
}
