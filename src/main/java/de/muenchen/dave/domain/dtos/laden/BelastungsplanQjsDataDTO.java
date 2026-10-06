package de.muenchen.dave.domain.dtos.laden;

import de.muenchen.dave.domain.enums.Himmelsrichtung;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
public class BelastungsplanQjsDataDTO extends AbstractBelastungsplanDataDTO {

    @NotNull
    private BigDecimal sumAll;
    @NotNull
    private List<StrassenseiteValue> valuesStrassenseite;
    @NotNull
    private List<VerkehrsbeziehungValue> valuesVerkehrsbeziehungen;

    @Data
    @RequiredArgsConstructor
    public static class VerkehrsbeziehungValue implements Serializable {
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private final int von;
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private final int nach;
        @NotNull
        private final Himmelsrichtung strassenseite;
        @NotNull
        private final BigDecimal value;
    }

    @Data
    @RequiredArgsConstructor
    public static class StrassenseiteValue implements Serializable {
        @NotNull
        private final Himmelsrichtung strassenseite;
        @NotNull
        private BigDecimal value;
    }
}
