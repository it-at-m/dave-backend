package de.muenchen.dave.domain.dtos;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class PkwEinheitDTO {

    BigDecimal pkw;

    BigDecimal lkw;

    BigDecimal lastzuege;

    BigDecimal busse;

    BigDecimal kraftraeder;

    BigDecimal fahrradfahrer;

}
