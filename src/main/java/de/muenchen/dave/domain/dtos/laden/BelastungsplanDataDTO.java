package de.muenchen.dave.domain.dtos.laden;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class BelastungsplanDataDTO extends AbstractBelastungsplanDataDTO {

    @java.io.Serial
    private static final long serialVersionUID = 1L;

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
