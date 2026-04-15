package de.muenchen.dave.domain.dtos.laden;

import de.muenchen.dave.domain.enums.Himmelsrichtung;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
public class BelastungsplanQJSDataDTO extends AbstractBelastungsplanDataDTO {

    private BigDecimal sumAll;
    private List<StrassenseiteValue> valuesStrassenseite;
    private List<VerkehrsbeziehungValue> valuesVerkehrsbeziehungen;

    @Data
    @RequiredArgsConstructor
    public static class VerkehrsbeziehungValue {
        private final int von;
        private final int nach;
        private final Himmelsrichtung strassenseite;
        private final BigDecimal value;
    }

    @Data
    @RequiredArgsConstructor
    public static class StrassenseiteValue {
        private final Himmelsrichtung strassenseite;
        private BigDecimal value;
    }
}
