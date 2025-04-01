package de.muenchen.dave.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;

/**
 * Definiert die möglichen unterjährigen Auswertungszeiträume
 * mit dem jeweiligen Start- und Enddatum.
 */
@AllArgsConstructor
@Getter
public enum AuswertungsZeitraum {

    // Monate
    JANUAR(LocalDate.of(0, 1, 1), LocalDate.of(0, 1, 31), "01", "Januar"),

    FEBRUAR(LocalDate.of(0, 2, 1), LocalDate.of(0, 2, 28), "02", "Februar"),

    MAERZ(LocalDate.of(0, 3, 1), LocalDate.of(0, 3, 31), "03", "März"),

    APRIL(LocalDate.of(0, 4, 1), LocalDate.of(0, 4, 30), "04", "April"),

    MAI(LocalDate.of(0, 5, 1), LocalDate.of(0, 5, 31), "05", "Mai"),

    JUNI(LocalDate.of(0, 6, 1), LocalDate.of(0, 6, 30), "06", "Juni"),

    JULI(LocalDate.of(0, 7, 1), LocalDate.of(0, 7, 31), "07", "Juli"),

    AUGUST(LocalDate.of(0, 8, 1), LocalDate.of(0, 8, 31), "08", "August"),

    SEPTEMBER(LocalDate.of(0, 9, 1), LocalDate.of(0, 9, 30), "09", "September"),

    OKTOBER(LocalDate.of(0, 10, 1), LocalDate.of(0, 10, 31), "10", "Oktober"),

    NOVEMBER(LocalDate.of(0, 11, 1), LocalDate.of(0, 11, 30), "11", "November"),

    DEZEMBER(LocalDate.of(0, 12, 1), LocalDate.of(0, 12, 31), "12", "Dezember"),
    // Quartale

    QUARTAL_1(JANUAR.getZeitraumStart(), MAERZ.getZeitraumEnd(), "Q1", "Quartal 1"),

    QUARTAL_2(APRIL.getZeitraumStart(), JUNI.getZeitraumEnd(), "Q2", "Quartal 2"),

    QUARTAL_3(JULI.getZeitraumStart(), SEPTEMBER.getZeitraumEnd(), "Q3", "Quartal 3"),

    QUARTAL_4(OKTOBER.getZeitraumStart(), DEZEMBER.getZeitraumEnd(), "Q4", "Quartal 4"),

    // Halbjahre
    HALBJAHR_1(QUARTAL_1.getZeitraumStart(), QUARTAL_2.getZeitraumEnd(), "H1", "Halbjahr 1"),

    HALBJAHR_2(QUARTAL_3.getZeitraumStart(), QUARTAL_4.getZeitraumEnd(), "H2", "Halbjahr 2"),

    // Jahr
    JAHRE(HALBJAHR_1.getZeitraumStart(), HALBJAHR_2.getZeitraumEnd(), StringUtils.EMPTY, "Jahre");

    private final LocalDate zeitraumStart;

    private final LocalDate zeitraumEnd;

    private final String text;
    private final String longText;

    public static boolean isJahr(final AuswertungsZeitraum zeitraum) {
        return StringUtils.equals(zeitraum.getLongText(), "Jahre");
    }

    public static boolean isHalbjahr(final AuswertungsZeitraum zeitraum) {
        return StringUtils.contains(zeitraum.getLongText(), "Halbjahr");
    }

    public static boolean isQuartal(final AuswertungsZeitraum zeitraum) {
        return StringUtils.contains(zeitraum.getLongText(), "Quartal");
    }
}
