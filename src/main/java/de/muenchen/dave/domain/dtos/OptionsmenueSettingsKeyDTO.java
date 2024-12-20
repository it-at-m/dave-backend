package de.muenchen.dave.domain.dtos;

import de.muenchen.dave.domain.enums.Fahrzeugklasse;
import de.muenchen.dave.domain.enums.ZaehldatenIntervall;
import lombok.Data;

@Data
public class OptionsmenueSettingsKeyDTO {

    private Fahrzeugklasse fahrzeugklasse;

    private ZaehldatenIntervall intervall;

}
