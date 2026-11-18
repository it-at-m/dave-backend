package de.muenchen.dave.services.processzaehldaten;

import de.muenchen.dave.domain.PkwEinheit;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.dtos.laden.AbstractLadeBelastungsplanDTO;
import de.muenchen.dave.relational.zaehlstellen.ZaehlungEntity;
import java.util.List;

public interface IBelastungsplanDataService {

    AbstractLadeBelastungsplanDTO<?> buildLadeBelastungsplanDTO(final OptionsDTO options,
            final ZaehlungEntity zaehlung,
            final PkwEinheit pkwEinheit,
            final List<Zeitintervall> zeitintervalle);

}
