package de.muenchen.dave.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Zeitauswahl {
    ZEITRAUM("Zeitraum"),

    TAGESWERT("Tageswert"),

    BLOCK("Block"),

    STUNDE("Stunde"),

    SPITZENSTUNDE_KFZ("Spitzenstunde KFZ"),

    SPITZENSTUNDE_RAD("Spitzenstunde Rad"),

    SPITZENSTUNDE_FUSS("Spitzenstunde Fuß");

    private final String capitalizedName;
}
