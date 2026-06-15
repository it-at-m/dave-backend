package de.muenchen.dave.domain.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Zaehldauer {

    /**
     * Kurzzeiterhebung (6 bis 10 Uhr; 15 bis 19 Uhr)
     */
    DAUER_2_X_4_STUNDEN(
            Arrays.asList("Kurzzeiterhebung", "Kurzzeiterhebung (2x4h)", "Kurzzeiterhebung (6 bis 10 Uhr; 15 bis 19 Uhr)", "2x4h", "2*4h", "2*4 Stunden",
                    "2x4 Stunden", "2*4Stunden", "2x4Stunden"),
            32,
            getZeitbloeckeFor2x4h()),

    /**
     * 24 Stunden
     */
    DAUER_24_STUNDEN(
            Arrays.asList("Tageszählung", "Ganztageszählung", "Tag", "24-Stundenzählung", "24Stundenzählung", "24h", "24Stunden"),
            96,
            getZeitbloeckeFor24h()),

    /**
     * 16 Stunden
     */
    DAUER_16_STUNDEN(
            Arrays.asList("16h", "16Stunden"),
            64,
            getZeitbloeckeFor16h()),

    /**
     * Kurzzeiterhebung (6 bis 19Uhr)
     */
    DAUER_13_STUNDEN(
            Arrays.asList("13h", "13Stunden"),
            52,
            getZeitbloeckeFor13h()),

    /**
     * Sonstige
     */
    SONSTIGE(
            Arrays.asList("Sonderzähldauer", "Sonstige-Zähldauer"),
            0,
            getZeitbloeckeFor24h());

    private final List<String> suchwoerter;

    private final int anzahlZeitintervalle;

    private final List<Zeitblock> zeitbloecke;

    public static List<Zeitblock> getZeitbloeckeFor24h() {
        final var bloecke = List.of(
                Zeitblock.ZB_00_06,
                Zeitblock.ZB_06_10,
                Zeitblock.ZB_10_15,
                Zeitblock.ZB_15_19,
                Zeitblock.ZB_19_24,
                Zeitblock.ZB_00_24);

        final var hours = List.of(
                Zeitblock.ZB_00_01,
                Zeitblock.ZB_01_02,
                Zeitblock.ZB_02_03,
                Zeitblock.ZB_03_04,
                Zeitblock.ZB_04_05,
                Zeitblock.ZB_05_06,
                Zeitblock.ZB_06_07,
                Zeitblock.ZB_07_08,
                Zeitblock.ZB_08_09,
                Zeitblock.ZB_09_10,
                Zeitblock.ZB_10_11,
                Zeitblock.ZB_11_12,
                Zeitblock.ZB_12_13,
                Zeitblock.ZB_13_14,
                Zeitblock.ZB_14_15,
                Zeitblock.ZB_15_16,
                Zeitblock.ZB_16_17,
                Zeitblock.ZB_17_18,
                Zeitblock.ZB_18_19,
                Zeitblock.ZB_19_20,
                Zeitblock.ZB_20_21,
                Zeitblock.ZB_21_22,
                Zeitblock.ZB_22_23,
                Zeitblock.ZB_23_24);

        final var halfHours = List.of(
                Zeitblock.ZB_0000_0030,
                Zeitblock.ZB_0030_0100,
                Zeitblock.ZB_0100_0130,
                Zeitblock.ZB_0130_0200,
                Zeitblock.ZB_0200_0230,
                Zeitblock.ZB_0230_0300,
                Zeitblock.ZB_0300_0330,
                Zeitblock.ZB_0330_0400,
                Zeitblock.ZB_0400_0430,
                Zeitblock.ZB_0430_0500,
                Zeitblock.ZB_0500_0530,
                Zeitblock.ZB_0530_0600,
                Zeitblock.ZB_0600_0630,
                Zeitblock.ZB_0630_0700,
                Zeitblock.ZB_0700_0730,
                Zeitblock.ZB_0730_0800,
                Zeitblock.ZB_0800_0830,
                Zeitblock.ZB_0830_0900,
                Zeitblock.ZB_0900_0930,
                Zeitblock.ZB_0930_1000,
                Zeitblock.ZB_1000_1030,
                Zeitblock.ZB_1030_1100,
                Zeitblock.ZB_1100_1130,
                Zeitblock.ZB_1130_1200,
                Zeitblock.ZB_1200_1230,
                Zeitblock.ZB_1230_1300,
                Zeitblock.ZB_1300_1330,
                Zeitblock.ZB_1330_1400,
                Zeitblock.ZB_1400_1430,
                Zeitblock.ZB_1430_1500,
                Zeitblock.ZB_1500_1530,
                Zeitblock.ZB_1530_1600,
                Zeitblock.ZB_1600_1630,
                Zeitblock.ZB_1630_1700,
                Zeitblock.ZB_1700_1730,
                Zeitblock.ZB_1730_1800,
                Zeitblock.ZB_1800_1830,
                Zeitblock.ZB_1830_1900,
                Zeitblock.ZB_1900_1930,
                Zeitblock.ZB_1930_2000,
                Zeitblock.ZB_2000_2030,
                Zeitblock.ZB_2030_2100,
                Zeitblock.ZB_2100_2130,
                Zeitblock.ZB_2130_2200,
                Zeitblock.ZB_2200_2230,
                Zeitblock.ZB_2230_2300,
                Zeitblock.ZB_2300_2330,
                Zeitblock.ZB_2330_2400);

        return Stream
                .of(bloecke.stream(), hours.stream(), halfHours.stream())
                .flatMap(Stream::distinct)
                .toList();
    }

    public static List<Zeitblock> getZeitbloeckeFor16h() {
        final var bloecke = List.of(
                Zeitblock.ZB_06_10,
                Zeitblock.ZB_10_15,
                Zeitblock.ZB_15_19,
                Zeitblock.ZB_19_22,
                Zeitblock.ZB_06_22,
                Zeitblock.ZB_00_24);

        final var hours = List.of(
                Zeitblock.ZB_06_07,
                Zeitblock.ZB_07_08,
                Zeitblock.ZB_08_09,
                Zeitblock.ZB_09_10,
                Zeitblock.ZB_10_11,
                Zeitblock.ZB_11_12,
                Zeitblock.ZB_12_13,
                Zeitblock.ZB_13_14,
                Zeitblock.ZB_14_15,
                Zeitblock.ZB_15_16,
                Zeitblock.ZB_16_17,
                Zeitblock.ZB_17_18,
                Zeitblock.ZB_18_19,
                Zeitblock.ZB_19_20,
                Zeitblock.ZB_20_21,
                Zeitblock.ZB_21_22);

        final var halfHours = List.of(
                Zeitblock.ZB_0600_0630,
                Zeitblock.ZB_0630_0700,
                Zeitblock.ZB_0700_0730,
                Zeitblock.ZB_0730_0800,
                Zeitblock.ZB_0800_0830,
                Zeitblock.ZB_0830_0900,
                Zeitblock.ZB_0900_0930,
                Zeitblock.ZB_0930_1000,
                Zeitblock.ZB_1000_1030,
                Zeitblock.ZB_1030_1100,
                Zeitblock.ZB_1100_1130,
                Zeitblock.ZB_1130_1200,
                Zeitblock.ZB_1200_1230,
                Zeitblock.ZB_1230_1300,
                Zeitblock.ZB_1300_1330,
                Zeitblock.ZB_1330_1400,
                Zeitblock.ZB_1400_1430,
                Zeitblock.ZB_1430_1500,
                Zeitblock.ZB_1500_1530,
                Zeitblock.ZB_1530_1600,
                Zeitblock.ZB_1600_1630,
                Zeitblock.ZB_1630_1700,
                Zeitblock.ZB_1700_1730,
                Zeitblock.ZB_1730_1800,
                Zeitblock.ZB_1800_1830,
                Zeitblock.ZB_1830_1900,
                Zeitblock.ZB_1900_1930,
                Zeitblock.ZB_1930_2000,
                Zeitblock.ZB_2000_2030,
                Zeitblock.ZB_2030_2100,
                Zeitblock.ZB_2100_2130,
                Zeitblock.ZB_2130_2200);

        return Stream
                .of(bloecke.stream(), hours.stream(), halfHours.stream())
                .flatMap(Stream::distinct)
                .toList();
    }

    public static List<Zeitblock> getZeitbloeckeFor13h() {
        final var bloecke = List.of(
                Zeitblock.ZB_06_10,
                Zeitblock.ZB_10_15,
                Zeitblock.ZB_15_19,
                Zeitblock.ZB_06_19,
                Zeitblock.ZB_00_24);

        final var hours = List.of(
                Zeitblock.ZB_06_07,
                Zeitblock.ZB_07_08,
                Zeitblock.ZB_08_09,
                Zeitblock.ZB_09_10,
                Zeitblock.ZB_10_11,
                Zeitblock.ZB_11_12,
                Zeitblock.ZB_12_13,
                Zeitblock.ZB_13_14,
                Zeitblock.ZB_14_15,
                Zeitblock.ZB_15_16,
                Zeitblock.ZB_16_17,
                Zeitblock.ZB_17_18,
                Zeitblock.ZB_18_19);

        final var halfHours = List.of(
                Zeitblock.ZB_0600_0630,
                Zeitblock.ZB_0630_0700,
                Zeitblock.ZB_0700_0730,
                Zeitblock.ZB_0730_0800,
                Zeitblock.ZB_0800_0830,
                Zeitblock.ZB_0830_0900,
                Zeitblock.ZB_0900_0930,
                Zeitblock.ZB_0930_1000,
                Zeitblock.ZB_1000_1030,
                Zeitblock.ZB_1030_1100,
                Zeitblock.ZB_1100_1130,
                Zeitblock.ZB_1130_1200,
                Zeitblock.ZB_1200_1230,
                Zeitblock.ZB_1230_1300,
                Zeitblock.ZB_1300_1330,
                Zeitblock.ZB_1330_1400,
                Zeitblock.ZB_1400_1430,
                Zeitblock.ZB_1430_1500,
                Zeitblock.ZB_1500_1530,
                Zeitblock.ZB_1530_1600,
                Zeitblock.ZB_1600_1630,
                Zeitblock.ZB_1630_1700,
                Zeitblock.ZB_1700_1730,
                Zeitblock.ZB_1730_1800,
                Zeitblock.ZB_1800_1830,
                Zeitblock.ZB_1830_1900);

        return Stream
                .of(bloecke.stream(), hours.stream(), halfHours.stream())
                .flatMap(Stream::distinct)
                .toList();
    }

    public static List<Zeitblock> getZeitbloeckeFor2x4h() {
        final var bloecke = List.of(
                Zeitblock.ZB_06_10,
                Zeitblock.ZB_15_19,
                Zeitblock.ZB_00_24);

        final var hours = List.of(
                Zeitblock.ZB_06_07,
                Zeitblock.ZB_07_08,
                Zeitblock.ZB_08_09,
                Zeitblock.ZB_09_10,
                Zeitblock.ZB_15_16,
                Zeitblock.ZB_16_17,
                Zeitblock.ZB_17_18,
                Zeitblock.ZB_18_19);

        final var halfHours = List.of(
                Zeitblock.ZB_0600_0630,
                Zeitblock.ZB_0630_0700,
                Zeitblock.ZB_0700_0730,
                Zeitblock.ZB_0730_0800,
                Zeitblock.ZB_0800_0830,
                Zeitblock.ZB_0830_0900,
                Zeitblock.ZB_0900_0930,
                Zeitblock.ZB_0930_1000,
                Zeitblock.ZB_1500_1530,
                Zeitblock.ZB_1530_1600,
                Zeitblock.ZB_1600_1630,
                Zeitblock.ZB_1630_1700,
                Zeitblock.ZB_1700_1730,
                Zeitblock.ZB_1730_1800,
                Zeitblock.ZB_1800_1830,
                Zeitblock.ZB_1830_1900);

        return Stream
                .of(bloecke.stream(), hours.stream(), halfHours.stream())
                .flatMap(Stream::distinct)
                .toList();
    }

}