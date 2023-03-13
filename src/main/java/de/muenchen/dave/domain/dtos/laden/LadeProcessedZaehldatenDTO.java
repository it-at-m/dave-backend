/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.domain.dtos.laden;

import lombok.Data;

import java.io.Serializable;


@Data
public class LadeProcessedZaehldatenDTO implements Serializable {

    LadeZaehldatenTableDTO zaehldatenTable;

    LadeZaehldatenSteplineDTO zaehldatenStepline;

    LadeZaehldatenHeatmapDTO zaehldatenHeatmap;

}
