package de.muenchen.dave.domain.dtos;

import de.muenchen.dave.domain.enums.Fahrzeugklasse;
import de.muenchen.dave.domain.enums.ZaehldatenIntervall;
import java.io.Serializable;
import java.util.List;
import lombok.Data;

@Data
public class OptionsmenueSettingsDTO implements Serializable {

    private Fahrzeugklasse fahrzeugklasse;

    private ZaehldatenIntervall intervall;

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
