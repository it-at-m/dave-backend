package de.muenchen.dave.domain.dtos.laden;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class StepLineSeriesEntryBigDecimalDTO extends StepLineSeriesEntryBaseDTO {

    @Serial
    private static final long serialVersionUID = 1L;

    private List<BigDecimal> yAxisData = new ArrayList<>();

}
