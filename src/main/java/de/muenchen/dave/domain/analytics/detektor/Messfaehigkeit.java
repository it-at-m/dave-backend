package de.muenchen.dave.domain.analytics.detektor;

import de.muenchen.dave.domain.BaseEntity;
import de.muenchen.dave.domain.enums.Fahrzeugklasse;
import de.muenchen.dave.domain.enums.ZaehldatenIntervall;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
// Definition of getter, setter, ...
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Messfaehigkeit extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "messstelle", referencedColumnName = "id")
    private Messstelle messstelle;

    @Column(name = "gueltig_ab")
    LocalDate gueltigAb;

    @Column(name = "gueltig_bis")
    LocalDate gueltigBis;

    @Column(name = "fahrzeugklasse")
    @Enumerated(EnumType.STRING)
    Fahrzeugklasse fahrzeugklasse;

    @Column(name = "intervall")
    @Enumerated(EnumType.STRING)
    ZaehldatenIntervall intervall;
}
