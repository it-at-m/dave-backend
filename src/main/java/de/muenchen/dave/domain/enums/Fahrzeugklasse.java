package de.muenchen.dave.domain.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(enumAsRef = true)
public enum Fahrzeugklasse {

    RAD,

    SUMME_KFZ,

    ZWEI_PLUS_EINS,

    ACHT_PLUS_EINS;

}
