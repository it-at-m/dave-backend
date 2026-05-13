package de.muenchen.dave.domain.dtos.laden;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;
import lombok.Data;

@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "belastungsplanTyp")
@JsonSubTypes(
    {
            @JsonSubTypes.Type(value = LadeBelastungsplanDTO.class, name = "DEFAULT"),
            @JsonSubTypes.Type(value = LadeBelastungsplanQjsDTO.class, name = "QJS"),
            @JsonSubTypes.Type(value = LadeBelastungsplanFjsDTO.class, name = "FJS")
    }
)
public abstract class AbstractLadeBelastungsplanDTO<T> implements Serializable {

    private String[] streets;
    private boolean kreisverkehr;

    private T value1;
    private T value2;
    private T value3;
}
