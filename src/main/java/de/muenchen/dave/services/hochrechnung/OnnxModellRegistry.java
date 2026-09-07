package de.muenchen.dave.services.hochrechnung;

import de.muenchen.dave.domain.enums.Hochrechnungsziel;
import de.muenchen.dave.domain.enums.ModelleingabeSchema;
import de.muenchen.dave.domain.enums.Zaehldauer;
import de.muenchen.dave.exceptions.PredictionFailedException;
import de.muenchen.dave.properties.OnnxModellDefinition;
import de.muenchen.dave.properties.OnnxModellProperties;
import jakarta.annotation.PreDestroy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OnnxModellRegistry {

    private final Map<Modellschluessel, Hochrechnungsmodell> modelle = new HashMap<>();

    public OnnxModellRegistry(final OnnxModellProperties onnxModellProperties,
            final List<ModelleingabeEncoder> modelleingabeEncoder) {
        for (final OnnxModellDefinition definition : onnxModellProperties.getModelle()) {
            initialisiereModell(definition, modelleingabeEncoder);
        }
    }

    public Optional<Hochrechnungsmodell> findeModell(final Zaehldauer zaehldauer, final Hochrechnungsziel ziel) {
        return Optional.ofNullable(modelle.get(new Modellschluessel(zaehldauer, ziel)));
    }

    @PreDestroy
    public void schliesseModelle() {
        modelle.values().forEach(modell -> {
            if (modell instanceof OnnxHochrechnungsmodell) {
                try {
                    ((OnnxHochrechnungsmodell) modell).schliessen();
                } catch (final IllegalStateException exception) {
                    log.error("ONNX-Modell {} konnte nicht geschlossen werden", modell.getDefinition().getId(), exception);
                }
            }
        });
    }

    private void initialisiereModell(final OnnxModellDefinition definition, final List<ModelleingabeEncoder> modelleingabeEncoder) {
        if (istUnvollstaendig(definition)) {
            log.error("ONNX-Modellkonfiguration ist unvollstaendig und wird uebersprungen: {}", definition);
            return;
        }
        final Optional<ModelleingabeEncoder> encoder = findeEncoder(definition.getInputSchema(), modelleingabeEncoder);
        if (encoder.isEmpty()) {
            log.error("Kein Modelleingabe-Encoder fuer ONNX-Modell {} mit Schema {} vorhanden", definition.getId(), definition.getInputSchema());
            return;
        }

        final Modellschluessel modellschluessel = new Modellschluessel(definition.getZaehldauer(), definition.getZiel());
        if (modelle.containsKey(modellschluessel)) {
            log.error("ONNX-Modell {} wird wegen doppelter Zaehlungsdauer und Zielgroesse uebersprungen", definition.getId());
            return;
        }
        try {
            modelle.put(modellschluessel, new OnnxHochrechnungsmodell(definition, encoder.get()));
            log.info("ONNX-Modell {} wurde initialisiert", definition.getId());
        } catch (final PredictionFailedException exception) {
            log.error("ONNX-Modell {} konnte nicht initialisiert werden und wird uebersprungen", definition.getId(), exception);
        }
    }

    private Optional<ModelleingabeEncoder> findeEncoder(final ModelleingabeSchema inputSchema,
            final List<ModelleingabeEncoder> modelleingabeEncoder) {
        return modelleingabeEncoder.stream().filter(encoder -> encoder.getSchema() == inputSchema).findFirst();
    }

    private boolean istUnvollstaendig(final OnnxModellDefinition definition) {
        return definition.getId() == null
                || definition.getZiel() == null
                || definition.getZaehldauer() == null
                || definition.getResourcePath() == null
                || definition.getInputTensorName() == null
                || definition.getInputSchema() == null;
    }

    private record Modellschluessel(Zaehldauer zaehldauer, Hochrechnungsziel ziel) {
    }

}
