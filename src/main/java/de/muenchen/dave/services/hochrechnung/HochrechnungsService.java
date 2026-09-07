package de.muenchen.dave.services.hochrechnung;

import de.muenchen.dave.domain.KIPredictionResult;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.enums.Hochrechnungsziel;
import de.muenchen.dave.domain.enums.Zaehldauer;
import de.muenchen.dave.exceptions.PredictionFailedException;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class HochrechnungsService {

    private final OnnxModellRegistry onnxModellRegistry;

    public HochrechnungsService(final OnnxModellRegistry onnxModellRegistry) {
        this.onnxModellRegistry = onnxModellRegistry;
    }

    public List<KIPredictionResult> berechneRadhochrechnung(final Zaehldauer zaehldauer,
            final List<List<Zeitintervall>> gruppierteZeitintervalle) throws PredictionFailedException {
        final var modell = onnxModellRegistry.findeModell(zaehldauer, Hochrechnungsziel.RAD);
        if (modell.isEmpty()) {
            return List.of();
        }
        return modell.get().berechne(gruppierteZeitintervalle);
    }

}
