package de.muenchen.dave.domain.dtos.laden;

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

    private BigDecimal sumAll;
    private List<StrassenseiteValue> valuesStrassenseite;
    private List<LaengsverkehrValue> valuesLaengsverkehr;

    @Data
    @RequiredArgsConstructor
    public static class LaengsverkehrValue implements Serializable {
        private final int von;
        private final Himmelsrichtung strassenseite;
        private final BigDecimal value;
    }

    @Data
    @RequiredArgsConstructor
    public static class StrassenseiteValue implements Serializable {
        private final Himmelsrichtung strassenseite;
        private BigDecimal value;
    }
}
