package de.muenchen.dave.domain.dtos.laden;

import java.io.Serializable;
import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LadeZaehldatenSteplineDTO implements Serializable {

    @NotNull
    private List<String> legend;
    @NotNull
    private Integer rangeMax;
    @NotNull
    private Integer rangeMaxPercent;
    @NotNull
    private List<String> xAxisDataFirstChart;
    private List<String> xAxisDataSecondChart;
    @NotNull
    private List<StepLineSeriesEntryBaseDTO> seriesEntriesFirstChart;
    private List<StepLineSeriesEntryBaseDTO> seriesEntriesSecondChart;

}
