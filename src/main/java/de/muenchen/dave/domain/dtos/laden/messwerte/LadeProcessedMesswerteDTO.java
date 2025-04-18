/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.domain.dtos.laden.messwerte;

import de.muenchen.dave.domain.dtos.laden.LadeZaehldatenHeatmapDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatenSteplineDTO;
import de.muenchen.dave.domain.enums.TagesTyp;
import lombok.Data;

import java.io.Serializable;

@Data
public class LadeProcessedMesswerteDTO implements Serializable {

    TagesTyp tagesTyp;

    LadeMesswerteListenausgabeDTO zaehldatenTable;

    LadeZaehldatenSteplineDTO zaehldatenStepline;

    LadeZaehldatenHeatmapDTO zaehldatenHeatmap;

    BelastungsplanMessquerschnitteDTO belastungsplanMessquerschnitte;

    private Long requestedMeasuringDays;
    private Integer includedMeasuringDays;
}
