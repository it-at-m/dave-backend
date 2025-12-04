package de.muenchen.dave.domain.dtos.laden;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;
import lombok.Data;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type", visible = true)
@JsonSubTypes(
    {
            @JsonSubTypes.Type(value = StepLineSeriesEntryBigDecimalDTO.class, name = "BIGDECIMAL"),
            @JsonSubTypes.Type(value = StepLineSeriesEntryIntegerDTO.class, name = "INTEGER") }
)
@Data
public class StepLineSeriesEntryBaseDTO implements Serializable {

    private String name;

    private Integer xAxisIndex;

    private Integer yAxisIndex;

}
