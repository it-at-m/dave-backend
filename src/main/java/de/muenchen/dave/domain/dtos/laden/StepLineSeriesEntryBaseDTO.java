/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.domain.dtos.laden;

import lombok.Data;

import java.io.Serializable;


@Data
public class StepLineSeriesEntryBaseDTO implements Serializable {

    private String name;

    private Integer xAxisIndex;

    private Integer yAxisIndex;

}
