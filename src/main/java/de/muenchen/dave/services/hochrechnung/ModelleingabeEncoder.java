package de.muenchen.dave.services.hochrechnung;

import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.enums.ModelleingabeSchema;
import de.muenchen.dave.domain.enums.Zaehldauer;
import de.muenchen.dave.exceptions.PredictionFailedException;
import java.util.List;

public interface ModelleingabeEncoder {

    ModelleingabeSchema getSchema();

    long[][] encodiere(final Zaehldauer zaehldauer, final List<List<Zeitintervall>> gruppierteZeitintervalle)
            throws PredictionFailedException;

}
