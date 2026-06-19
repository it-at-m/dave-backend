package de.muenchen.dave.domain.dtos.laden;

import de.muenchen.dave.domain.enums.Himmelsrichtung;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
public class BelastungsplanQuDataDTO extends AbstractBelastungsplanDataDTO {

    private List<KnotenarmValue> valuesKnotenarme;

    @Data
    @RequiredArgsConstructor
    public static class KnotenarmValue implements Serializable {
        private final int knotenarm;
        private BigDecimal sumKnotenarm;
        private final List<QuerungsverkehrValue> valuesQuerungsverkehre;
    }

    @Data
    @RequiredArgsConstructor
    public static class QuerungsverkehrValue implements Serializable {
        private final Himmelsrichtung richtung;
        private final BigDecimal value;
    }

}
