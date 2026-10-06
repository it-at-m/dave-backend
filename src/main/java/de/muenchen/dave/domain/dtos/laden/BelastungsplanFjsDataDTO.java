package de.muenchen.dave.domain.dtos.laden;

import de.muenchen.dave.domain.enums.Bewegungsrichtung;
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
public class BelastungsplanFjsDataDTO extends AbstractBelastungsplanDataDTO {

    @NotNull
    private List<KnotenarmValue> valuesKnotenarme;

    @Data
    @RequiredArgsConstructor
    public static class KnotenarmValue implements Serializable {
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private final int knotenarm;
        @NotNull
        private BigDecimal sumKnotenarm;
        @NotNull
        private final List<StrassenseiteValue> valuesStrassenseiten;
    }

    @Data
    @RequiredArgsConstructor
    public static class StrassenseiteValue implements Serializable {
        @NotNull
        private final Himmelsrichtung strassenseite;
        @NotNull
        private BigDecimal sumStrassenseite;
        @NotNull
        private final List<LaengsverkehrValue> valuesLaengsverkehre;
    }

    @Data
    @RequiredArgsConstructor
    public static class LaengsverkehrValue implements Serializable {
        @NotNull
        private final Bewegungsrichtung richtung;
        @NotNull
        private final BigDecimal value;
    }

}
