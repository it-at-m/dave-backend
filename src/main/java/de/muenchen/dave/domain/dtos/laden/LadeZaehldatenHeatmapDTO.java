package de.muenchen.dave.domain.dtos.laden;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import lombok.Data;
import lombok.Getter;

@Data
public class LadeZaehldatenHeatmapDTO implements Serializable {

    @NotNull
    private List<String> legend;
    @NotNull
    private Integer rangeMin;
    @NotNull
    private Integer rangeMax;
    @Getter(onMethod_ = { @NotNull }) // NotNull alleine reicht nicht
    @NotNull
    private List<String> xAxisDataFirstChart;
    private List<String> xAxisDataSecondChart;
    @NotNull
    private List<List<Integer>> seriesEntriesFirstChart;
    private List<List<Integer>> seriesEntriesSecondChart;

}
