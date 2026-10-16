package de.muenchen.dave.domain.dtos.laden;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import lombok.Data;
import lombok.Getter;

@Data
public class LadeZaehldatenSteplineDTO implements Serializable {

    @NotNull
    private List<String> legend;
    @NotNull
    private Integer rangeMax;
    @NotNull
    private Integer rangeMaxPercent;
    @Getter(onMethod_ = { @NotNull }) // NotNull alleine reicht nicht
    @NotNull
    private List<String> xAxisDataFirstChart;
    private List<String> xAxisDataSecondChart;
    @NotNull
    private List<StepLineSeriesEntryBaseDTO> seriesEntriesFirstChart;
    private List<StepLineSeriesEntryBaseDTO> seriesEntriesSecondChart;

}
