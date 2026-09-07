package de.muenchen.dave.services.hochrechnung;

import de.muenchen.dave.domain.KIPredictionResult;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.exceptions.PredictionFailedException;
import de.muenchen.dave.properties.OnnxModelDefinition;
import java.util.List;

public interface Hochrechnungsmodell {

    OnnxModelDefinition getDefinition();

    List<KIPredictionResult> calculate(final List<List<Zeitintervall>> groupedZeitintervalle) throws PredictionFailedException;

}
