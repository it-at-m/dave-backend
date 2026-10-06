package de.muenchen.dave.domain.dtos.laden;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class BelastungsplanDataDTO extends AbstractBelastungsplanDataDTO {

    @NotNull
    private BigDecimal[][] values;

    private boolean percent;

    @NotNull
    private BigDecimal[] sumIn;
    @NotNull
    private BigDecimal[] sumOut;

    @NotNull
    private BigDecimal[] sum;

}
