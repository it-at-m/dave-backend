package de.muenchen.dave.domain.dtos.laden;

import de.muenchen.dave.domain.enums.Bewegungsrichtung;
import de.muenchen.dave.domain.enums.Himmelsrichtung;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
public class BelastungsplanFjsDataDTO extends AbstractBelastungsplanDataDTO {

    private List<KnotenarmValue> valuesKnotenarme;

    @Data
    @RequiredArgsConstructor
    public static class KnotenarmValue implements Serializable {
        private final int knotenarm;
        private BigDecimal sumKnotenarm;
        private final List<StrassenseiteValue> valuesStrassenseiten;
    }

    @Data
    @RequiredArgsConstructor
    public static class StrassenseiteValue implements Serializable {
        private final Himmelsrichtung strassenseite;
        private BigDecimal sumStrassenseite;
        private final List<LaengsverkehrValue> valuesLaengsverkehre;
    }

    @Data
    @RequiredArgsConstructor
    public static class LaengsverkehrValue implements Serializable {
        private final Bewegungsrichtung richtung;
        private final BigDecimal value;
    }

}
