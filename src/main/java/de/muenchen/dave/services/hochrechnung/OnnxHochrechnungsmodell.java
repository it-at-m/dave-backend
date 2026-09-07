package de.muenchen.dave.services.hochrechnung;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OnnxValue;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import de.muenchen.dave.domain.KIPredictionResult;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.exceptions.PredictionFailedException;
import de.muenchen.dave.properties.OnnxModellDefinition;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;

public class OnnxHochrechnungsmodell implements Hochrechnungsmodell {

    private final OrtEnvironment environment;

    private final OrtSession session;

    private final OnnxModellDefinition definition;

    private final ModelleingabeEncoder modelleingabeEncoder;

    public OnnxHochrechnungsmodell(final OnnxModellDefinition definition,
            final ModelleingabeEncoder modelleingabeEncoder) throws PredictionFailedException {
        this.definition = definition;
        this.modelleingabeEncoder = modelleingabeEncoder;
        this.environment = OrtEnvironment.getEnvironment();
        this.session = initializeSession(definition.getResourcePath());
    }

    @Override
    public OnnxModellDefinition getDefinition() {
        return definition;
    }

    @Override
    public List<KIPredictionResult> berechne(final List<List<Zeitintervall>> gruppierteZeitintervalle) throws PredictionFailedException {
        final long[][] eingabedaten = modelleingabeEncoder.encodiere(definition.getZaehldauer(), gruppierteZeitintervalle);
        final long[][] vorhersagen = fuehreVorhersageAus(eingabedaten);
        final List<KIPredictionResult> ergebnisse = new ArrayList<>();
        for (final long[] vorhersage : vorhersagen) {
            try {
                ergebnisse.add(KIPredictionResult.fromArray(vorhersage));
            } catch (final IllegalArgumentException | ArithmeticException exception) {
                throw new PredictionFailedException(PredictionFailedException.ONNX_PREDICTION_UNKNOWN_RESULTTYPE_ERROR);
            }
        }
        return ergebnisse;
    }

    public void schliessen() {
        try {
            session.close();
        } catch (final OrtException exception) {
            throw new IllegalStateException("ONNX-Session konnte nicht geschlossen werden", exception);
        }
    }

    private OrtSession initializeSession(final String resourcePath) throws PredictionFailedException {
        try (InputStream stream = new ClassPathResource(resourcePath).getInputStream()) {
            return environment.createSession(IOUtils.toByteArray(stream), new OrtSession.SessionOptions());
        } catch (final OrtException | IOException exception) {
            throw new PredictionFailedException(PredictionFailedException.ONNX_SESSION_CREATION_ERROR);
        }
    }

    private long[][] fuehreVorhersageAus(final long[][] eingabedaten) throws PredictionFailedException {
        try (OnnxTensor tensor = OnnxTensor.createTensor(environment, eingabedaten);
                OrtSession.Result ergebnis = session.run(Map.of(definition.getInputTensorName(), tensor))) {
            if (ergebnis.size() == 0) {
                throw new PredictionFailedException(PredictionFailedException.ONNX_NO_PREDICTION_RESULTS_ERROR);
            }
            final OnnxValue onnxWert = ergebnis.get(0);
            if (onnxWert.getType() == OnnxValue.OnnxValueType.ONNX_TYPE_UNKNOWN) {
                throw new PredictionFailedException(PredictionFailedException.ONNX_PREDICTION_UNKNOWN_RESULTTYPE_ERROR);
            }
            return (long[][]) onnxWert.getValue();
        } catch (final OrtException exception) {
            throw new PredictionFailedException(PredictionFailedException.ONNX_RUN_MODEL_ERROR);
        } catch (final ClassCastException exception) {
            throw new PredictionFailedException(PredictionFailedException.ONNX_PREDICTION_UNKNOWN_RESULTTYPE_ERROR);
        }
    }

}
