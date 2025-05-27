/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.domain.dtos.laden;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class StepLineSeriesEntryIntegerDTO extends StepLineSeriesEntryBaseDTO {

    private List<Integer> yAxisData = new ArrayList<>();

}
