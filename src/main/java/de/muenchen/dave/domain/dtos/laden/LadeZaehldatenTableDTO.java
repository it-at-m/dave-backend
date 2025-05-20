/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.domain.dtos.laden;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class LadeZaehldatenTableDTO implements Serializable {

    List<LadeZaehldatumDTO> zaehldaten;

}
