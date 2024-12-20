package de.muenchen.dave.domain.dtos;

import de.muenchen.dave.domain.enums.ZaehldatenIntervall;
import lombok.Data;

import java.util.List;

@Data
public class OptionsmenueSettingsDTO {

    private OptionsmenueSettingsKeyDTO fahrzeugklassenAndIntervall;

    private List<ZaehldatenIntervall> choosableIntervals;

    private boolean kraftfahrzeugverkehr;

    private boolean schwerverkehr;

    private boolean gueterverkehr;

    private boolean schwerverkehrsanteilProzent;

    private boolean gueterverkehrsanteilProzent;

    private boolean radverkehr;

    private boolean fussverkehr;

    private boolean lastkraftwagen;

    private boolean lastzuege;

    private boolean busse;

    private boolean kraftraeder;

    private boolean personenkraftwagen;

    private boolean lieferwagen;

}
