package de.muenchen.dave.domain.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum AuswertungsZeitraum {

    // Monate
    JANUAR, FEBRUAR, MAERZ, APRIL, MAI, JUNI, JULI, AUGUST, SEPTEMBER, OKTOBER, NOVEMBER, DEZEMBER,
    // Quartale
    QUARTAL_1, QUARTAL_2, QUARTAL_3, QUARTAL_4,
    // Halbjahre
    HALBJAHR_1, HALBJAHR_2,
    // Jahr
    JAHRE

}
