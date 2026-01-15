package de.muenchen.dave.domain.elasticsearch;

import java.io.Serializable;
import java.math.BigDecimal;

import org.springframework.data.annotation.Transient;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PkwEinheit implements Serializable {

    @Transient
    String id;

    @Transient
    Long version;

    BigDecimal pkw;

    BigDecimal lkw;

    BigDecimal lastzuege;

    BigDecimal busse;

    BigDecimal kraftraeder;

    BigDecimal fahrradfahrer;

}
