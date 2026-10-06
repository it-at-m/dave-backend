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

@Data
@EqualsAndHashCode(callSuper = true)
public class BelastungsplanQuDataDTO extends AbstractBelastungsplanDataDTO {

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
        private final List<QuerungsverkehrValue> valuesQuerungsverkehre;
    }

    @Data
    @RequiredArgsConstructor
    public static class QuerungsverkehrValue implements Serializable {
        @NotNull
        private final Himmelsrichtung richtung;
        @NotNull
        private final BigDecimal value;
    }

}
