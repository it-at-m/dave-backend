/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.domain.dtos.laden;

import java.io.Serializable;
import lombok.Data;

@Data
public class StepLineSeriesEntryBaseDTO implements Serializable {

    private String name;

    private Integer xAxisIndex;

    private Integer yAxisIndex;

}
