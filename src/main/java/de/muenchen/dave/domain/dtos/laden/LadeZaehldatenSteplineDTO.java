package de.muenchen.dave.domain.dtos.laden;

import java.io.Serializable;
import java.util.List;
import lombok.Data;

@Data
public class LadeZaehldatenSteplineDTO implements Serializable {

    private List<String> legend;

    private Integer rangeMax;

    private Integer rangeMaxPercent;

    private List<String> xAxisDataFirstChart;

    private List<String> xAxisDataSecondChart;

    private List<StepLineSeriesEntryBaseDTO> seriesEntriesFirstChart;

    private List<StepLineSeriesEntryBaseDTO> seriesEntriesSecondChart;



}
