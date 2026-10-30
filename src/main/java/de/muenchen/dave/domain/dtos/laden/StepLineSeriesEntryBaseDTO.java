package de.muenchen.dave.domain.dtos.laden;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import lombok.Data;
import lombok.Getter;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type", visible = true)
@JsonSubTypes(
    {
            @JsonSubTypes.Type(value = StepLineSeriesEntryBigDecimalDTO.class, name = "BIGDECIMAL"),
            @JsonSubTypes.Type(value = StepLineSeriesEntryIntegerDTO.class, name = "INTEGER") }
)
@Data
public abstract class StepLineSeriesEntryBaseDTO implements Serializable {

    @java.io.Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    private String name;
    @Getter(onMethod_ = { @NotNull })
    private Integer xAxisIndex;
    @Getter(onMethod_ = { @NotNull })
    private Integer yAxisIndex;

    @JsonProperty("yAxisData")
    @Schema(name = "yAxisData")
    public abstract List<? extends Number> getYAxisData();

}
