package de.muenchen.dave.domain.dtos.laden;

import java.io.Serializable;
import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LadeZaehldatenHeatmapDTO implements Serializable {

    @NotNull
    private List<String> legend;
    @NotNull
    private Integer rangeMin;
    @NotNull
    private Integer rangeMax;
    @NotNull
    private List<String> xAxisDataFirstChart;
    private List<String> xAxisDataSecondChart;
    @NotNull
    private List<List<Integer>> seriesEntriesFirstChart;
    private List<List<Integer>> seriesEntriesSecondChart;

}
