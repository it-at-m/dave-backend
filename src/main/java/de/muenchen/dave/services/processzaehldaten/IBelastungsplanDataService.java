package de.muenchen.dave.services.processzaehldaten;

import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.dtos.OptionsDto;
import de.muenchen.dave.domain.dtos.laden.AbstractLadeBelastungsplanDTO;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import java.util.List;

public interface IBelastungsplanDataService {

    AbstractLadeBelastungsplanDTO<?> buildLadeBelastungsplanDTO(final OptionsDto options,
            final Zaehlung zaehlung,
            final List<Zeitintervall> zeitintervalle);

}
