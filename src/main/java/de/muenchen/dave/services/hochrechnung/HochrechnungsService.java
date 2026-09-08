package de.muenchen.dave.services.hochrechnung;

import de.muenchen.dave.domain.KIPredictionResult;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.enums.Fahrzeug;
import de.muenchen.dave.domain.enums.Zaehldauer;
import de.muenchen.dave.exceptions.PredictionFailedException;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * Waehlt fuer eine Zaehlungsdauer das konfigurierte Modell und startet dessen Inferenz.
 *
 * <p>
 * Ein nicht verfuegbares Modell ist kein fachlicher Fehler: In diesem Fall wird eine leere
 * Ergebnisliste zurueckgegeben und die konventionelle Aufbereitung kann fortgesetzt werden.
 * </p>
 */
@Service
public class HochrechnungsService {

    private final OnnxModelRegistry onnxModelRegistry;

    public HochrechnungsService(final OnnxModelRegistry onnxModelRegistry) {
        this.onnxModelRegistry = onnxModelRegistry;
    }

    /**
     * Berechnet die KI-gestuetzte Rad-Tagessumme je Bewegungsbeziehung.
     *
     * @param zaehldauer Dauer der Zaehlung und damit Kriterium der Modellauswahl
     * @param groupedZeitintervalle Zeitintervalle, je Bewegungsbeziehung gruppiert
     * @return leere Liste, falls kein passendes Modell verfuegbar ist, sonst eine Vorhersage je Gruppe
     * @throws PredictionFailedException wenn das gefundene Modell nicht inferiert werden kann
     */
    public List<KIPredictionResult> calculateRadhochrechnung(final Zaehldauer zaehldauer,
            final List<List<Zeitintervall>> groupedZeitintervalle) throws PredictionFailedException {
        final Optional<Hochrechnungsmodell> model = onnxModelRegistry.findModel(zaehldauer, Fahrzeug.RAD);
        if (model.isEmpty()) {
            return List.of();
        }
        return model.get().calculate(groupedZeitintervalle);
    }

}
