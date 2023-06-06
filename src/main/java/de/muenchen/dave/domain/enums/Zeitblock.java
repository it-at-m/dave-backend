/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.domain.enums;

import de.muenchen.dave.util.DaveConstants;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Oracle-DBs koennen nicht mit der columnDefintion 'TIME' umgehen. Daher ist es noetig gewesen, die
 * Zeitintervalle mit einem konkreten Datum zu speichern.
 * Per Default wird sonst immer das aktuelle Datum verwendet, was dazu fuehrt, dass die Intervalle
 * nicht mehr anhand des Uhrzeit gefunden werden koennen.
 * Daher wird nun immer nach dem #DaveConstants.DEFAULT_LOCALDATE + der Zeit des Intervalls gesucht.
 */
@AllArgsConstructor
@Getter
public enum Zeitblock implements Serializable {

    /**
     * 00:00 bis 06:00 Uhr
     */
    ZB_00_06(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.MIDNIGHT), LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(6, 0)),
            TypeZeitintervall.BLOCK),

    /**
     * 06:00 bis 10:00 Uhr (vormittags)
     */
    ZB_06_10(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(6, 0)), LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 0)),
            TypeZeitintervall.BLOCK),

    /**
     * 10:00 bis 15:00 Uhr
     */
    ZB_10_15(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 0)), LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(15, 0)),
            TypeZeitintervall.BLOCK),

    /**
     * 15:00 bis 19:00 Uhr (nachmittags)
     */
    ZB_15_19(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(15, 0)), LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(19, 0)),
            TypeZeitintervall.BLOCK),

    /**
     * 19:00 bis 24:00 Uhr
     */
    ZB_19_24(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(19, 0)), LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.MAX),
            TypeZeitintervall.BLOCK),

    /**
     * 00:00 bis 24:00 Uhr (Tagesverkehr)
     */
    ZB_00_24(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.MIDNIGHT), LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.MAX),
            TypeZeitintervall.GESAMT),

    /*
     * Spezielle Zeitblöcke welche über mehrere kürzere Zeitblöcke des Typs
     * {@link TypeZeitintervall#BLOCK} gehen.
     * Diese Werden benötigt wenn z.B. kein Tageswert hochgerechnet werden soll.
     */

    /**
     * 06:00 bis 19:00 Uhr
     */
    ZB_06_19(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(6, 0)), LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(19, 0)),
            TypeZeitintervall.BLOCK_SPEZIAL),

    /**
     * 06:00 bis 22:00 Uhr
     */
    ZB_06_22(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(6, 0)), LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(22, 0)),
            TypeZeitintervall.BLOCK_SPEZIAL),

    /**
     * Stündlichen Zeitblöcke
     */

    ZB_00_01(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.MIDNIGHT), LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(1, 0)),
            TypeZeitintervall.STUNDE_KOMPLETT),

    ZB_01_02(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(1, 0)), LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(2, 0)),
            TypeZeitintervall.STUNDE_KOMPLETT),

    ZB_02_03(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(2, 0)), LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(3, 0)),
            TypeZeitintervall.STUNDE_KOMPLETT),

    ZB_03_04(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(3, 0)), LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(4, 0)),
            TypeZeitintervall.STUNDE_KOMPLETT),

    ZB_04_05(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(4, 0)), LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(5, 0)),
            TypeZeitintervall.STUNDE_KOMPLETT),

    ZB_05_06(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(5, 0)), LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(6, 0)),
            TypeZeitintervall.STUNDE_KOMPLETT),

    ZB_06_07(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(6, 0)), LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(7, 0)),
            TypeZeitintervall.STUNDE_KOMPLETT),

    ZB_07_08(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(7, 0)), LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(8, 0)),
            TypeZeitintervall.STUNDE_KOMPLETT),

    ZB_08_09(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(8, 0)), LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(9, 0)),
            TypeZeitintervall.STUNDE_KOMPLETT),

    ZB_09_10(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(9, 0)), LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 0)),
            TypeZeitintervall.STUNDE_KOMPLETT),

    ZB_10_11(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 0)), LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(11, 0)),
            TypeZeitintervall.STUNDE_KOMPLETT),

    ZB_11_12(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(11, 0)), LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(12, 0)),
            TypeZeitintervall.STUNDE_KOMPLETT),

    ZB_12_13(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(12, 0)), LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(13, 0)),
            TypeZeitintervall.STUNDE_KOMPLETT),

    ZB_13_14(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(13, 0)), LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(14, 0)),
            TypeZeitintervall.STUNDE_KOMPLETT),

    ZB_14_15(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(14, 0)), LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(15, 0)),
            TypeZeitintervall.STUNDE_KOMPLETT),

    ZB_15_16(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(15, 0)), LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(16, 0)),
            TypeZeitintervall.STUNDE_KOMPLETT),

    ZB_16_17(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(16, 0)), LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(17, 0)),
            TypeZeitintervall.STUNDE_KOMPLETT),

    ZB_17_18(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(17, 0)), LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(18, 0)),
            TypeZeitintervall.STUNDE_KOMPLETT),

    ZB_18_19(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(18, 0)), LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(19, 0)),
            TypeZeitintervall.STUNDE_KOMPLETT),

    ZB_19_20(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(19, 0)), LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(20, 0)),
            TypeZeitintervall.STUNDE_KOMPLETT),

    ZB_20_21(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(20, 0)), LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(21, 0)),
            TypeZeitintervall.STUNDE_KOMPLETT),

    ZB_21_22(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(21, 0)), LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(22, 0)),
            TypeZeitintervall.STUNDE_KOMPLETT),

    ZB_22_23(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(22, 0)), LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(23, 0)),
            TypeZeitintervall.STUNDE_KOMPLETT),

    ZB_23_24(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(23, 0)), LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.MAX),
            TypeZeitintervall.STUNDE_KOMPLETT),

    /**
     * Halbstündliche Zeitblöcke
     */

    ZB_0000_0030(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(0, 0)), LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(0, 30)),
            TypeZeitintervall.STUNDE_HALB),

    ZB_0030_0100(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(0, 30)), LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(1, 0)),
            TypeZeitintervall.STUNDE_HALB),

    ZB_0100_0130(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(1, 0)), LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(1, 30)),
            TypeZeitintervall.STUNDE_HALB),

    ZB_0130_0200(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(1, 30)), LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(2, 0)),
            TypeZeitintervall.STUNDE_HALB),

    ZB_0200_0230(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(2, 0)), LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(2, 30)),
            TypeZeitintervall.STUNDE_HALB),

    ZB_0230_0300(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(2, 30)), LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(3, 0)),
            TypeZeitintervall.STUNDE_HALB),

    ZB_0300_0330(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(3, 0)), LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(3, 30)),
            TypeZeitintervall.STUNDE_HALB),

    ZB_0330_0400(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(3, 30)), LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(4, 0)),
            TypeZeitintervall.STUNDE_HALB),

    ZB_0400_0430(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(4, 0)), LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(4, 30)),
            TypeZeitintervall.STUNDE_HALB),

    ZB_0430_0500(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(4, 30)), LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(5, 0)),
            TypeZeitintervall.STUNDE_HALB),

    ZB_0500_0530(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(5, 0)), LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(5, 30)),
            TypeZeitintervall.STUNDE_HALB),

    ZB_0530_0600(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(5, 30)), LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(6, 0)),
            TypeZeitintervall.STUNDE_HALB),

    ZB_0600_0630(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(6, 0)), LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(6, 30)),
            TypeZeitintervall.STUNDE_HALB),

    ZB_0630_0700(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(6, 30)), LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(7, 0)),
            TypeZeitintervall.STUNDE_HALB),

    ZB_0700_0730(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(7, 0)), LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(7, 30)),
            TypeZeitintervall.STUNDE_HALB),

    ZB_0730_0800(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(7, 30)), LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(8, 0)),
            TypeZeitintervall.STUNDE_HALB),

    ZB_0800_0830(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(8, 0)), LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(8, 30)),
            TypeZeitintervall.STUNDE_HALB),

    ZB_0830_0900(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(8, 30)), LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(9, 0)),
            TypeZeitintervall.STUNDE_HALB),

    ZB_0900_0930(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(9, 0)), LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(9, 30)),
            TypeZeitintervall.STUNDE_HALB),

    ZB_0930_1000(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(9, 30)), LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 0)),
            TypeZeitintervall.STUNDE_HALB),

    ZB_1000_1030(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 0)),
            LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 30)), TypeZeitintervall.STUNDE_HALB),

    ZB_1030_1100(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 30)),
            LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(11, 0)), TypeZeitintervall.STUNDE_HALB),

    ZB_1100_1130(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(11, 0)),
            LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(11, 30)), TypeZeitintervall.STUNDE_HALB),

    ZB_1130_1200(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(11, 30)),
            LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(12, 0)), TypeZeitintervall.STUNDE_HALB),

    ZB_1200_1230(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(12, 0)),
            LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(12, 30)), TypeZeitintervall.STUNDE_HALB),

    ZB_1230_1300(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(12, 30)),
            LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(13, 0)), TypeZeitintervall.STUNDE_HALB),

    ZB_1300_1330(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(13, 0)),
            LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(13, 30)), TypeZeitintervall.STUNDE_HALB),

    ZB_1330_1400(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(13, 30)),
            LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(14, 0)), TypeZeitintervall.STUNDE_HALB),

    ZB_1400_1430(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(14, 0)),
            LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(14, 30)), TypeZeitintervall.STUNDE_HALB),

    ZB_1430_1500(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(14, 30)),
            LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(15, 0)), TypeZeitintervall.STUNDE_HALB),

    ZB_1500_1530(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(15, 0)),
            LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(15, 30)), TypeZeitintervall.STUNDE_HALB),

    ZB_1530_1600(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(15, 30)),
            LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(16, 0)), TypeZeitintervall.STUNDE_HALB),

    ZB_1600_1630(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(16, 0)),
            LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(16, 30)), TypeZeitintervall.STUNDE_HALB),

    ZB_1630_1700(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(16, 30)),
            LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(17, 0)), TypeZeitintervall.STUNDE_HALB),

    ZB_1700_1730(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(17, 0)),
            LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(17, 30)), TypeZeitintervall.STUNDE_HALB),

    ZB_1730_1800(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(17, 30)),
            LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(18, 0)), TypeZeitintervall.STUNDE_HALB),

    ZB_1800_1830(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(18, 0)),
            LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(18, 30)), TypeZeitintervall.STUNDE_HALB),

    ZB_1830_1900(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(18, 30)),
            LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(19, 0)), TypeZeitintervall.STUNDE_HALB),

    ZB_1900_1930(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(19, 0)),
            LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(19, 30)), TypeZeitintervall.STUNDE_HALB),

    ZB_1930_2000(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(19, 30)),
            LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(20, 0)), TypeZeitintervall.STUNDE_HALB),

    ZB_2000_2030(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(20, 0)),
            LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(20, 30)), TypeZeitintervall.STUNDE_HALB),

    ZB_2030_2100(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(20, 30)),
            LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(21, 0)), TypeZeitintervall.STUNDE_HALB),

    ZB_2100_2130(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(21, 0)),
            LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(21, 30)), TypeZeitintervall.STUNDE_HALB),

    ZB_2130_2200(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(21, 30)),
            LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(22, 0)), TypeZeitintervall.STUNDE_HALB),

    ZB_2200_2230(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(22, 0)),
            LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(22, 30)), TypeZeitintervall.STUNDE_HALB),

    ZB_2230_2300(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(22, 30)),
            LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(23, 0)), TypeZeitintervall.STUNDE_HALB),

    ZB_2300_2330(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(23, 0)),
            LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(23, 30)), TypeZeitintervall.STUNDE_HALB),

    ZB_2330_2400(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(23, 30)), LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.MAX),
            TypeZeitintervall.STUNDE_HALB);

    private final LocalDateTime start;

    private final LocalDateTime end;

    private final TypeZeitintervall typeZeitintervall;

}
