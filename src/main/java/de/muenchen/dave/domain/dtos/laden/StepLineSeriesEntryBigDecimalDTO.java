package de.muenchen.dave.domain.dtos.laden;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class StepLineSeriesEntryBigDecimalDTO extends StepLineSeriesEntryBaseDTO {

    private List<BigDecimal> yAxisData = new ArrayList<>();

}
