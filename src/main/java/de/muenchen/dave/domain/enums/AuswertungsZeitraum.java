package de.muenchen.dave.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@AllArgsConstructor
@Getter
public enum AuswertungsZeitraum {

    // Monate
    JANUAR(LocalDate.of(0, 1, 1), LocalDate.of(0, 1, 31)), FEBRUAR(LocalDate.of(0, 2, 1), LocalDate.of(0, 2, 28)), MAERZ(LocalDate.of(0, 3, 1),
            LocalDate.of(0, 3, 31)), APRIL(LocalDate.of(0, 4, 1), LocalDate.of(0, 4, 30)), MAI(LocalDate.of(0, 5, 1),
                    LocalDate.of(0, 5, 31)), JUNI(LocalDate.of(0, 6, 1), LocalDate.of(0, 6, 30)), JULI(LocalDate.of(0, 7, 1),
                            LocalDate.of(0, 7, 31)), AUGUST(LocalDate.of(0, 8, 1), LocalDate.of(0, 8, 31)), SEPTEMBER(LocalDate.of(0, 9, 1),
                                    LocalDate.of(0, 9, 30)), OKTOBER(LocalDate.of(0, 10, 1), LocalDate.of(0, 10, 31)), NOVEMBER(LocalDate.of(0, 11, 1),
                                            LocalDate.of(0, 11, 30)), DEZEMBER(LocalDate.of(0, 12, 1), LocalDate.of(0, 12, 31)),
    // Quartale
    QUARTAL_1(JANUAR.zeitraumStart, MAERZ.zeitraumEnd), QUARTAL_2(APRIL.zeitraumStart, JUNI.zeitraumEnd), QUARTAL_3(JULI.zeitraumStart,
            SEPTEMBER.zeitraumEnd), QUARTAL_4(OKTOBER.zeitraumStart, DEZEMBER.zeitraumEnd),
    // Halbjahre
    HALBJAHR_1(QUARTAL_1.zeitraumStart, QUARTAL_2.zeitraumEnd), HALBJAHR_2(QUARTAL_3.zeitraumStart, QUARTAL_4.zeitraumEnd),
    // Jahr
    JAHRE(HALBJAHR_1.zeitraumStart, HALBJAHR_2.zeitraumEnd);

    private final LocalDate zeitraumStart;
    private final LocalDate zeitraumEnd;
}
