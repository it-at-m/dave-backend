package de.muenchen.dave.domain.dtos.laden;

import java.io.Serializable;
import java.util.List;
import lombok.Data;

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
