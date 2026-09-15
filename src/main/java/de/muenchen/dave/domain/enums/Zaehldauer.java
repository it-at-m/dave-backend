package de.muenchen.dave.domain.enums;

import java.util.Arrays;
import java.util.List;
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
            Arrays.asList(Zeitblock.ZB_06_10, Zeitblock.ZB_15_19)),

    /**
     * 24 Stunden
     */
    DAUER_24_STUNDEN(
            Arrays.asList("Tageszählung", "Ganztageszählung", "Tag", "24-Stundenzählung", "24Stundenzählung", "24h", "24Stunden"),
            96,
            List.of(Zeitblock.ZB_00_24)),

    /**
     * 16 Stunden
     */
    DAUER_16_STUNDEN(
            Arrays.asList("16h", "16Stunden"),
            64,
            List.of(Zeitblock.ZB_06_22)),

    /**
     * Kurzzeiterhebung (6 bis 19Uhr)
     */
    DAUER_13_STUNDEN(
            Arrays.asList("13h", "13Stunden"),
            52,
            List.of(Zeitblock.ZB_06_19)),

    /**
     * Sonstige
     */
    SONSTIGE(
            Arrays.asList("Sonderzähldauer", "Sonstige-Zähldauer"),
            0,
            List.of());

    private final List<String> suchwoerter;

    /** Erwartete Anzahl der Viertelstundenintervalle innerhalb der Modellzeitbloecke. */
    private final int anzahlZeitintervalle;

    /** Ausschliesslich die erfassten Zeitfenster, die als Modelleingabe verwendet werden. */
    private final List<Zeitblock> modelInputZeitbloecke;

}
