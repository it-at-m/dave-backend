package de.muenchen.dave.domain.dtos;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PkwEinheitDTO {

    BigDecimal pkw;

    BigDecimal lkw;

    BigDecimal lastzuege;

    BigDecimal busse;

    BigDecimal kraftraeder;

    BigDecimal fahrradfahrer;

}
