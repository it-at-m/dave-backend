package de.muenchen.dave.services.hochrechnung;

import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.enums.ModelleingabeSchema;
import de.muenchen.dave.domain.enums.Zaehldauer;
import de.muenchen.dave.domain.mapper.KIZeitintervallMapper;
import de.muenchen.dave.exceptions.PredictionFailedException;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class KontextRadV1Encoder extends AbstrakterModelleingabeEncoder {

    private final KIZeitintervallMapper kiZeitintervallMapper;

    public KontextRadV1Encoder(final KIZeitintervallMapper kiZeitintervallMapper) {
        this.kiZeitintervallMapper = kiZeitintervallMapper;
    }

    @Override
    public ModelleingabeSchema getSchema() {
        return ModelleingabeSchema.KONTEXT_RAD_V1;
    }

    @Override
    public long[][] encodiere(final Zaehldauer zaehldauer, final List<List<Zeitintervall>> gruppierteZeitintervalle)
            throws PredictionFailedException {
        final List<List<Zeitintervall>> gefilterteZeitintervalle = filtereUndSortiere(zaehldauer, gruppierteZeitintervalle);
        final long[][] eingabedaten = new long[gefilterteZeitintervalle.size()][];
        for (int gruppenIndex = 0; gruppenIndex < gefilterteZeitintervalle.size(); gruppenIndex++) {
            final List<Zeitintervall> zeitintervalleJeBewegungsbeziehung = gefilterteZeitintervalle.get(gruppenIndex);
            final long[] eingabedatenJeBewegungsbeziehung = new long[zeitintervalleJeBewegungsbeziehung.size() * 10];
            int eingabeIndex = 0;
            for (final Zeitintervall zeitintervall : zeitintervalleJeBewegungsbeziehung) {
                for (final long merkmal : kiZeitintervallMapper.zeitintervallToKIZeitintervall(zeitintervall).toArray()) {
                    eingabedatenJeBewegungsbeziehung[eingabeIndex++] = merkmal;
                }
            }
            eingabedaten[gruppenIndex] = eingabedatenJeBewegungsbeziehung;
        }
        return eingabedaten;
    }

}
