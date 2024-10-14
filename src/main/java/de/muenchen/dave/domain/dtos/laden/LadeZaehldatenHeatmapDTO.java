/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.domain.dtos.laden;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class LadeZaehldatenHeatmapDTO implements Serializable {

    private List<String> legend;

    private Integer rangeMin;

    private Integer rangeMax;

    private List<String> xAxisDataFirstChart;

    private List<String> xAxisDataSecondChart;

    private List<List<Integer>> seriesEntriesFirstChart;

    private List<List<Integer>> seriesEntriesSecondChart;

}
