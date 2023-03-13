package de.muenchen.dave.domain.dtos.laden;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;


@Data
public class BelastungsplanDataDTO implements Serializable {

    private BigDecimal[][] values;

    private String label;

    private boolean filled;

    private boolean percent;

    private BigDecimal[] sumIn;
    private BigDecimal[] sumOut;
    private BigDecimal[] sum;

}
