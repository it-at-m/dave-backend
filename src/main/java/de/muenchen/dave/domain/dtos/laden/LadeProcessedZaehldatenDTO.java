/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.domain.dtos.laden;

import java.io.Serializable;
import lombok.Data;

@Data
public class LadeProcessedZaehldatenDTO implements Serializable {

    LadeZaehldatenTableDTO zaehldatenTable;
    LadeZaehldatenSteplineDTO zaehldatenStepline;
    LadeZaehldatenHeatmapDTO zaehldatenHeatmap;
    LadeBelastungsplanDTO zaehldatenBelastungsplan;
    LadeZaehldatenZeitreiheDTO zaehldatenZeitreihe;

}
