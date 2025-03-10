package de.muenchen.dave.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Entity
// Definition of getter, setter, ...
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PkwEinheit extends BaseEntity {

    @Column(name = "pkw")
    private BigDecimal pkw;

    @Column(name = "lkw")
    private BigDecimal lkw;

    @Column(name = "lastzuege")
    private BigDecimal lastzuege;

    @Column(name = "busse")
    private BigDecimal busse;

    @Column(name = "kraftraeder")
    private BigDecimal kraftraeder;

    @Column(name = "fahrradfahrer")
    private BigDecimal fahrradfahrer;

}
