package de.muenchen.dave.migrationespg.domain.dtos.laden;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.muenchen.dave.domain.dtos.laden.StepLineSeriesEntryBaseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.Getter;

@Data
public class StepLineSeriesEntryIntegerDTO extends StepLineSeriesEntryBaseDTO {

    @Serial
    private static final long serialVersionUID = 1L;

    @Getter(onMethod_ = { @Schema(name = "yAxisData") })
    @JsonProperty("yaxisData")
    private List<Integer> yAxisData = new ArrayList<>();

}
