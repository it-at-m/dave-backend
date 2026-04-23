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
            @JsonSubTypes.Type(value = LadeBelastungsplanQjsDTO.class, name = "QJS")
    }
)
public abstract class AbstractLadeBelastungsplanDTO<T> implements Serializable {

    protected String[] streets;
    protected boolean kreisverkehr;

    protected T value1;
    protected T value2;
    protected T value3;
}
