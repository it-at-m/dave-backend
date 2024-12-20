package de.muenchen.dave.domain;

import de.muenchen.dave.domain.enums.Fahrzeugklasse;
import de.muenchen.dave.domain.enums.ZaehldatenIntervall;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Embeddable
@Data
@EqualsAndHashCode
public class OptionsmenueSettingsKey {

    @Column(nullable = false)
    private Fahrzeugklasse fahrzeugklasse;

    @Column(nullable = false)
    private ZaehldatenIntervall intervall;

}
