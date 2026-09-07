package de.muenchen.dave.services.hochrechnung;

import de.muenchen.dave.domain.KIPredictionResult;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.enums.Hochrechnungskategorie;
import de.muenchen.dave.domain.enums.Zaehldauer;
import de.muenchen.dave.exceptions.PredictionFailedException;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
public class HochrechnungsService {

    private final OnnxModelRegistry onnxModelRegistry;

    public HochrechnungsService(final OnnxModelRegistry onnxModelRegistry) {
        this.onnxModelRegistry = onnxModelRegistry;
    }

    public List<KIPredictionResult> calculateRadhochrechnung(final Zaehldauer zaehldauer,
                                                             final List<List<Zeitintervall>> groupedZeitintervalle) throws PredictionFailedException {
        final Optional<Hochrechnungsmodell> model = onnxModelRegistry.findModel(zaehldauer, Hochrechnungskategorie.RAD);
        if (model.isEmpty()) {
            return List.of();
        }
        return model.get().calculate(groupedZeitintervalle);
    }

}
