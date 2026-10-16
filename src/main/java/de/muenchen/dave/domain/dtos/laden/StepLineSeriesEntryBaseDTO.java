package de.muenchen.dave.domain.dtos.laden;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import lombok.Data;
import lombok.Getter;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type", visible = true)
@JsonSubTypes(
    {
            @JsonSubTypes.Type(value = StepLineSeriesEntryBigDecimalDTO.class, name = "BIGDECIMAL"),
            @JsonSubTypes.Type(value = StepLineSeriesEntryIntegerDTO.class, name = "INTEGER") }
)
@Data
public class StepLineSeriesEntryBaseDTO implements Serializable {

    @NotNull
    private String name;
    @Getter(onMethod_ = { @NotNull })
    private Integer xAxisIndex;
    @Getter(onMethod_ = { @NotNull })
    private Integer yAxisIndex;

}
