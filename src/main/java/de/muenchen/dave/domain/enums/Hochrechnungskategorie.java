package de.muenchen.dave.domain.enums;

/**
 * Fachliche Zielgroesse eines Hochrechnungsmodells.
 *
 * <p>
 * Bei Modellen mit {@link ModelInputSchema#REINE_FAHRZEUGWERTE} bestimmt die Kategorie zugleich,
 * welches Zaehlfeld aus einem Zeitintervall als Eingabe verwendet wird.
 * </p>
 */
public enum Hochrechnungskategorie {

    RAD,
    PKW,
    LKW,
    LZ,
    BUS,
    KRAD

}
