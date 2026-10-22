package de.muenchen.dave.domain.dtos.laden;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class StepLineSeriesEntryIntegerDTO extends StepLineSeriesEntryBaseDTO {

    @Serial
    private static final long serialVersionUID = 1L;

    private List<Integer> yAxisData = new ArrayList<>();

}
