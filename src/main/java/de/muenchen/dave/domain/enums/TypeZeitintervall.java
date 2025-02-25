/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.domain.enums;

import java.io.Serializable;

public enum TypeZeitintervall implements Serializable {

    BLOCK,
    BLOCK_SPEZIAL,
    GESAMT,
    GESAMT_KI,
    SPITZENSTUNDE_KFZ,
    SPITZENSTUNDE_RAD,
    SPITZENSTUNDE_FUSS,
    STUNDE_VIERTEL,
    STUNDE_HALB,
    STUNDE_KOMPLETT

}
