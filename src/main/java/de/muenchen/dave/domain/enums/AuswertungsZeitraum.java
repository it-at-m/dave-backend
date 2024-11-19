package de.muenchen.dave.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.Month;

/**
 * Definiert die möglichen unterjährigen Auswertungszeiträume
 * mit dem jeweiligen Start- und Enddatum.
 */
@AllArgsConstructor
@Getter
public enum AuswertungsZeitraum {

    // Monate
    JANUAR(LocalDate.of(0, 1, 1), LocalDate.of(0, 1, 31), "01"),

    FEBRUAR(LocalDate.of(0, 2, 1), LocalDate.of(0, 2, 28), "02"),

    MAERZ(LocalDate.of(0, 3, 1), LocalDate.of(0, 3, 31), "03"),

    APRIL(LocalDate.of(0, 4, 1), LocalDate.of(0, 4, 30), "04"),

    MAI(LocalDate.of(0, 5, 1), LocalDate.of(0, 5, 31), "05"),

    JUNI(LocalDate.of(0, 6, 1), LocalDate.of(0, 6, 30), "06"),

    JULI(LocalDate.of(0, 7, 1), LocalDate.of(0, 7, 31), "07"),

    AUGUST(LocalDate.of(0, 8, 1), LocalDate.of(0, 8, 31), "08"),

    SEPTEMBER(LocalDate.of(0, 9, 1), LocalDate.of(0, 9, 30), "09"),

    OKTOBER(LocalDate.of(0, 10, 1), LocalDate.of(0, 10, 31), "10"),

    NOVEMBER(LocalDate.of(0, 11, 1), LocalDate.of(0, 11, 30), "11"),

    DEZEMBER(LocalDate.of(0, 12, 1), LocalDate.of(0, 12, 31), "12"),
    // Quartale

    QUARTAL_1(JANUAR.getZeitraumStart(), MAERZ.getZeitraumEnd(), "Q1"),

    QUARTAL_2(APRIL.getZeitraumStart(), JUNI.getZeitraumEnd(), "Q2"),

    QUARTAL_3(JULI.getZeitraumStart(), SEPTEMBER.getZeitraumEnd(), "Q3"),

    QUARTAL_4(OKTOBER.getZeitraumStart(), DEZEMBER.getZeitraumEnd(), "Q4"),

    // Halbjahre
    HALBJAHR_1(QUARTAL_1.getZeitraumStart(), QUARTAL_2.getZeitraumEnd(), "H1"),

    HALBJAHR_2(QUARTAL_3.getZeitraumStart(), QUARTAL_4.getZeitraumEnd(), "H2"),

    // Jahr
    JAHRE(HALBJAHR_1.getZeitraumStart(), HALBJAHR_2.getZeitraumEnd(), StringUtils.EMPTY);

    private final LocalDate zeitraumStart;

    private final LocalDate zeitraumEnd;

    private final String text;
}
