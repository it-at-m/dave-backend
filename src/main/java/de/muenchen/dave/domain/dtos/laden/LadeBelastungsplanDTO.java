package de.muenchen.dave.domain.dtos.laden;

import java.io.Serializable;
import lombok.Data;

@Data
public class LadeBelastungsplanDTO implements Serializable {

    private AbstractBelastungsplanDataDTO value1;
    private AbstractBelastungsplanDataDTO value2;
    private AbstractBelastungsplanDataDTO value3;
    private String[] streets;
    private boolean kreisverkehr;
}
