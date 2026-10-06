package de.muenchen.dave.domain.dtos.laden;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "belastungsplanTyp")
@JsonSubTypes(
    {
            @JsonSubTypes.Type(value = LadeBelastungsplanDTO.class, name = "DEFAULT"),
            @JsonSubTypes.Type(value = LadeBelastungsplanQjsDTO.class, name = "QJS"),
            @JsonSubTypes.Type(value = LadeBelastungsplanFjsDTO.class, name = "FJS"),
            @JsonSubTypes.Type(value = LadeBelastungsplanQuDTO.class, name = "QU")
    }
)
public abstract class AbstractLadeBelastungsplanDTO<T> implements Serializable {

    @NotNull
    private String[] streets;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean kreisverkehr;

    @NotNull
    private T value1;
    @NotNull
    private T value2;
    @NotNull
    private T value3;
}
