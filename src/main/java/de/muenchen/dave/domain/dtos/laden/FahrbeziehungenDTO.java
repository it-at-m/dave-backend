/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.domain.dtos.laden;

import java.util.Map;
import java.util.Set;
import lombok.Data;

/**
 * Klasse stellt die möglichen Fahrbeziehung zur Verfügung welche ausgewählt werden können.
 */
@Data
public class FahrbeziehungenDTO {

    /**
     * Die Nummer aller in die Kreuzung/in den Kreisverkehr führenden Knotenarme.
     */
    Set<Integer> vonKnotenarme;

    /**
     * Je in die in die Kreuzung/in den Kreisverkehr führenden Knotenarm
     * die möglichen Zielknotenarme.
     */
    Map<Integer, Set<Integer>> nachKnotenarme;

}
