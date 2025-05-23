package de.muenchen.dave.domain.dtos.laden;

import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Data;

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
