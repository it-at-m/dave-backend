package de.muenchen.dave.domain.dtos.laden;

import java.io.Serializable;
import lombok.Data;

@Data
public abstract class AbstractLadeBelastungsplanDTO<T> implements Serializable {

    protected String[] streets;
    protected boolean kreisverkehr;

    protected T value1;
    protected T value2;
    protected T value3;
}
