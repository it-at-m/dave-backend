package de.muenchen.dave.domain.dtos.laden;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import lombok.Data;

@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "belastungsplanTyp", visible = true)
@JsonSubTypes(
    {
            @JsonSubTypes.Type(value = LadeBelastungsplanDTO.class, name = "DEFAULT"),
            @JsonSubTypes.Type(value = LadeBelastungsplanQjsDTO.class, name = "QJS"),
            @JsonSubTypes.Type(value = LadeBelastungsplanFjsDTO.class, name = "FJS"),
            @JsonSubTypes.Type(value = LadeBelastungsplanQuDTO.class, name = "QU")
    }
)
@Schema(
        description = "AbstractLadeBelastungsplanDTO",
        discriminatorProperty = "belastungsplanTyp",
        discriminatorMapping = {
                @DiscriminatorMapping(
                        value = "DEFAULT",
                        schema = LadeBelastungsplanDTO.class
                ),
                @DiscriminatorMapping(
                        value = "QJS",
                        schema = LadeBelastungsplanQjsDTO.class
                ),
                @DiscriminatorMapping(
                        value = "FJS",
                        schema = LadeBelastungsplanFjsDTO.class
                ),
                @DiscriminatorMapping(
                        value = "QU",
                        schema = LadeBelastungsplanQuDTO.class
                )
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
