package de.muenchen.dave.domain.dtos;

import de.muenchen.dave.domain.enums.ZaehldatenIntervall;
import lombok.Data;

import java.util.List;

@Data
public class OptionsmenueSettingsDTO {

    private OptionsmenueSettingsKeyDTO fahrzeugklassenAndIntervall;

    private List<ZaehldatenIntervall> kraftfahrzeugverkehrChoosableIntervals;

    private List<ZaehldatenIntervall> schwerverkehrChoosableIntervals;

    private List<ZaehldatenIntervall> gueterverkehrChoosableIntervals;

    private List<ZaehldatenIntervall> schwerverkehrsanteilProzentChoosableIntervals;

    private List<ZaehldatenIntervall> gueterverkehrsanteilProzentChoosableIntervals;

    private List<ZaehldatenIntervall> radverkehrChoosableIntervals;

    private List<ZaehldatenIntervall> fussverkehrChoosableIntervals;

    private List<ZaehldatenIntervall> lastkraftwagenChoosableIntervals;

    private List<ZaehldatenIntervall> lastzuegeChoosableIntervals;

    private List<ZaehldatenIntervall> busseChoosableIntervals;

    private List<ZaehldatenIntervall> kraftraederChoosableIntervals;

    private List<ZaehldatenIntervall> personenkraftwagenChoosableIntervals;

    private List<ZaehldatenIntervall> lieferwagenChoosableIntervals;
}
