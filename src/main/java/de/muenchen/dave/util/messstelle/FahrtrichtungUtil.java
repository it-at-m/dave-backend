package de.muenchen.dave.util.messstelle;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FahrtrichtungUtil {

    public static String getLongTextOfFahrtrichtung(final String fahrtrichtung) {
        switch (fahrtrichtung) {
        case "N":
            return "Nord";
        case "O":
            return "Ost";
        case "S":
            return "Süd";
        case "W":
            return "West";
        default:
            return fahrtrichtung;
        }
    }
}
