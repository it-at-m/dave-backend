package de.muenchen.dave.services.hochrechnung;

import de.muenchen.dave.domain.enums.Hochrechnungskategorie;
import de.muenchen.dave.domain.enums.ModelInputSchema;
import de.muenchen.dave.domain.enums.Zaehldauer;
import de.muenchen.dave.exceptions.PredictionFailedException;
import de.muenchen.dave.properties.OnnxModelDefinition;
import de.muenchen.dave.properties.OnnxModelProperties;
import jakarta.annotation.PreDestroy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Initialisiert und verwaltet die konfigurierten ONNX-Modelle.
 *
 * <p>
 * Modelle werden ueber die Kombination aus Zaehlungsdauer und Hochrechnungskategorie
 * eindeutig identifiziert. Fehler eines einzelnen Artefakts werden beim Start isoliert behandelt;
 * das Modell wird dann nicht registriert und beeinflusst andere Modelle nicht.
 * </p>
 */
@Service
@Slf4j
public class OnnxModelRegistry {

    private final Map<ModelSet, Hochrechnungsmodell> models = new HashMap<>();

    public OnnxModelRegistry(final OnnxModelProperties onnxModelProperties,
            final List<ModelInputEncoder> modelInputEncoder) {
        for (final OnnxModelDefinition definition : onnxModelProperties.getModelle()) {
            initializeModels(definition, modelInputEncoder);
        }
    }

    /**
     * Sucht ein zur Zaehlung und Zielgroesse passendes, erfolgreich initialisiertes Modell.
     */
    public Optional<Hochrechnungsmodell> findModel(final Zaehldauer zaehldauer, final Hochrechnungskategorie hochrechnungskategorie) {
        return Optional.ofNullable(models.get(new ModelSet(zaehldauer, hochrechnungskategorie)));
    }

    @PreDestroy
    public void closeModels() {
        models.values().forEach(model -> {
            if (model instanceof OnnxHochrechnungsmodell) {
                try {
                    ((OnnxHochrechnungsmodell) model).closeSession();
                } catch (final IllegalStateException exception) {
                    log.error("ONNX-Modell {} konnte nicht geschlossen werden", model.getDefinition().getId(), exception);
                }
            }
        });
    }

    private void initializeModels(final OnnxModelDefinition definition, final List<ModelInputEncoder> modelInputEncoder) {
        if (isIncomplete(definition)) {
            log.error("ONNX-Modellkonfiguration ist unvollstaendig und wird uebersprungen: {}", definition);
            return;
        }
        final Optional<ModelInputEncoder> encoder = findEncoder(definition.getInputSchema(), modelInputEncoder);
        if (encoder.isEmpty()) {
            log.error("Kein Modelleingabe-Encoder fuer ONNX-Modell {} mit Schema {} vorhanden", definition.getId(), definition.getInputSchema());
            return;
        }

        // Eine Zielgroesse darf je Zaehlungsdauer nur von einem Modell beliefert werden.
        final ModelSet modelSet = new ModelSet(definition.getZaehldauer(), definition.getHochrechnungskategorie());
        if (models.containsKey(modelSet)) {
            log.error("ONNX-Modell {} wird wegen doppelter Zaehlungsdauer und Zielgroesse uebersprungen", definition.getId());
            return;
        }
        try {
            models.put(modelSet, new OnnxHochrechnungsmodell(definition, encoder.get()));
            log.info("ONNX-Modell {} wurde initialisiert", definition.getId());
        } catch (final PredictionFailedException exception) {
            log.error("ONNX-Modell {} konnte nicht initialisiert werden und wird uebersprungen", definition.getId(), exception);
        }
    }

    private Optional<ModelInputEncoder> findEncoder(final ModelInputSchema inputSchema,
            final List<ModelInputEncoder> modelInputEncoder) {
        return modelInputEncoder.stream().filter(encoder -> encoder.getSchema() == inputSchema).findFirst();
    }

    private boolean isIncomplete(final OnnxModelDefinition definition) {
        return definition.getId() == null
                || definition.getHochrechnungskategorie() == null
                || definition.getZaehldauer() == null
                || definition.getResourcePath() == null
                || definition.getInputTensorName() == null
                || definition.getInputSchema() == null;
    }

    private record ModelSet(Zaehldauer zaehldauer, Hochrechnungskategorie hochrechnungskategorie) {
    }

}
