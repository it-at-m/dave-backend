package de.muenchen.dave.services.hochrechnung;

import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.enums.ModelleingabeSchema;
import de.muenchen.dave.domain.enums.Zaehldauer;
import de.muenchen.dave.exceptions.PredictionFailedException;
import java.util.List;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

@Component
public class ReineRadwerteV1Encoder extends AbstrakterModelleingabeEncoder {

    @Override
    public ModelleingabeSchema getSchema() {
        return ModelleingabeSchema.REINE_RADWERTE_V1;
    }

    @Override
    public long[][] encodiere(final Zaehldauer zaehldauer, final List<List<Zeitintervall>> gruppierteZeitintervalle)
            throws PredictionFailedException {
        final List<List<Zeitintervall>> gefilterteZeitintervalle = filtereUndSortiere(zaehldauer, gruppierteZeitintervalle);
        final long[][] eingabedaten = new long[gefilterteZeitintervalle.size()][];
        for (int gruppenIndex = 0; gruppenIndex < gefilterteZeitintervalle.size(); gruppenIndex++) {
            final List<Zeitintervall> zeitintervalleJeBewegungsbeziehung = gefilterteZeitintervalle.get(gruppenIndex);
            final long[] eingabedatenJeBewegungsbeziehung = new long[zeitintervalleJeBewegungsbeziehung.size()];
            for (int intervallIndex = 0; intervallIndex < zeitintervalleJeBewegungsbeziehung.size(); intervallIndex++) {
                eingabedatenJeBewegungsbeziehung[intervallIndex] = ObjectUtils.defaultIfNull(
                        zeitintervalleJeBewegungsbeziehung.get(intervallIndex).getFahrradfahrer(), 0);
            }
            eingabedaten[gruppenIndex] = eingabedatenJeBewegungsbeziehung;
        }
        return eingabedaten;
    }

}
