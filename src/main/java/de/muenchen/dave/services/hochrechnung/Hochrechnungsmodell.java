package de.muenchen.dave.services.hochrechnung;

import de.muenchen.dave.domain.KIPredictionResult;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.exceptions.PredictionFailedException;
import de.muenchen.dave.properties.OnnxModellDefinition;
import java.util.List;

public interface Hochrechnungsmodell {

    OnnxModellDefinition getDefinition();

    List<KIPredictionResult> berechne(final List<List<Zeitintervall>> gruppierteZeitintervalle) throws PredictionFailedException;

}
