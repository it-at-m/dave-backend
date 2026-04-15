package de.muenchen.dave.domain.dtos.laden;

import java.math.BigDecimal;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class BelastungsplanDataDTO extends AbstractBelastungsplanDataDTO {

    private BigDecimal[][] values;

    private String label;

    private boolean percent;

    private BigDecimal[] sumIn;
    private BigDecimal[] sumOut;

    private BigDecimal[] sum;

}
