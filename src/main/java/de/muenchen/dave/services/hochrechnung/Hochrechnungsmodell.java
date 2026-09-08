package de.muenchen.dave.services.hochrechnung;

import de.muenchen.dave.domain.KIPredictionResult;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.exceptions.PredictionFailedException;
import de.muenchen.dave.properties.OnnxModelDefinition;
import java.util.List;

/**
 * Technische Abstraktion eines Modells zur Hochrechnung einer fachlichen Zielgroesse.
 *
 * <p>
 * Die Abstraktion trennt die Auswahl und Orchestrierung eines Modells von dessen Laufzeit.
 * Dadurch kann der {@link HochrechnungsService} auch ohne ein ONNX-Artefakt getestet werden.
 * </p>
 */
public interface Hochrechnungsmodell {

    /**
     * @return die Konfiguration, mit der das Modell registriert wurde
     */
    OnnxModelDefinition getDefinition();

    /**
     * Berechnet eine Tagessumme je Bewegungsbeziehung.
     *
     * @param groupedZeitintervalle Eingabewerte, je Bewegungsbeziehung gruppiert
     * @return Vorhersagen in derselben Reihenfolge wie die Eingabegruppen
     * @throws PredictionFailedException wenn die Inferenz nicht ausgefuehrt werden kann
     */
    List<KIPredictionResult> calculate(final List<List<Zeitintervall>> groupedZeitintervalle) throws PredictionFailedException;

}
