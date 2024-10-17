package de.muenchen.dave.domain.dtos.laden;

import lombok.Data;

import java.io.Serializable;

@Data
public class LadeBelastungsplanDTO implements Serializable {

    private BelastungsplanDataDTO value1;
    private BelastungsplanDataDTO value2;
    private BelastungsplanDataDTO value3;
    private String[] streets;
    private boolean kreisverkehr;
}
