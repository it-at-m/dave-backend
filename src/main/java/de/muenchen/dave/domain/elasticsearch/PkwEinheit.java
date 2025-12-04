package de.muenchen.dave.domain.elasticsearch;

import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PkwEinheit implements Serializable {

    BigDecimal pkw;

    BigDecimal lkw;

    BigDecimal lastzuege;

    BigDecimal busse;

    BigDecimal kraftraeder;

    BigDecimal fahrradfahrer;

}
