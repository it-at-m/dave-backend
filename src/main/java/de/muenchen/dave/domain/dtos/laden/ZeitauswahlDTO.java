/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.domain.dtos.laden;

import de.muenchen.dave.domain.enums.Zeitblock;
import lombok.Data;

import java.io.Serializable;
import java.util.Set;

/**
 * Klasse stellt die möglichen Zeitblöcke und Stunden zur Verfügung welche ausgewählt werden können.
 */
@Data
public class ZeitauswahlDTO implements Serializable {

    /**
     * Die möglichen Zeitblöcke
     */
    Set<Zeitblock> blocks;

    /**
     * Die möglichen Stunden
     */
    Set<Zeitblock> hours;

}
