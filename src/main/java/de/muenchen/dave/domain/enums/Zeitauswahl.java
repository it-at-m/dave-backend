package de.muenchen.dave.domain.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Schema(enumAsRef = true)
public enum Zeitauswahl {

    TAGESWERT("Tageswert"),

    BLOCK("Block"),

    STUNDE("Stunde"),

    SPITZENSTUNDE_KFZ("Spitzenstunde KFZ"),

    SPITZENSTUNDE_RAD("Spitzenstunde Rad"),

    SPITZENSTUNDE_FUSS("Spitzenstunde Fuß");

    private final String capitalizedName;
}
