/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ZaehldatenIntervall {

    STUNDE_VIERTEL(1),

    STUNDE_HALB(2),

    STUNDE_KOMPLETT(4);

    private final Integer quarterPerIntervall;

}
