package de.muenchen.dave.domain.dtos.laden;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class BelastungsplanDataDTO extends AbstractBelastungsplanDataDTO {

    @NotNull
    private BigDecimal[][] values;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean percent;

    @NotNull
    private BigDecimal[] sumIn;
    @NotNull
    private BigDecimal[] sumOut;

    @NotNull
    private BigDecimal[] sum;

}
