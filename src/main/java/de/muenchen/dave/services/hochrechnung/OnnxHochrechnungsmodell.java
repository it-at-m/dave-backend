package de.muenchen.dave.services.hochrechnung;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OnnxValue;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import de.muenchen.dave.domain.KIPredictionResult;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.exceptions.PredictionFailedException;
import de.muenchen.dave.properties.OnnxModelDefinition;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;

/**
 * Fuehrt ein ONNX-Modell aus, das als Klassenpfadressource ausgeliefert wird.
 *
 * <p>
 * Pro Modellinstanz wird eine ONNX-Session erstellt und bis zum Beenden der Anwendung
 * wiederverwendet. Die Ein- und Ausgabe dieses Modelladapters sind {@code long[][]}, passend zum
 * {@code int64}-Tensorvertrag der aktuell konfigurierten Radmodelle.
 * </p>
 */
public class OnnxHochrechnungsmodell implements Hochrechnungsmodell {

    private final OrtEnvironment environment;

    private final OrtSession session;

    private final OnnxModelDefinition definition;

    private final ModelInputEncoder modelInputEncoder;

    public OnnxHochrechnungsmodell(final OnnxModelDefinition definition,
            final ModelInputEncoder modelInputEncoder) throws PredictionFailedException {
        this.definition = definition;
        this.modelInputEncoder = modelInputEncoder;
        this.environment = OrtEnvironment.getEnvironment();
        this.session = initializeSession(definition.getResourcePath());
    }

    @Override
    public OnnxModelDefinition getDefinition() {
        return definition;
    }

    @Override
    public List<KIPredictionResult> calculate(final List<List<Zeitintervall>> groupedZeitintervalle) throws PredictionFailedException {
        final long[][] inputData = modelInputEncoder.encode(definition.getZaehldauer(), groupedZeitintervalle);
        final long[][] predictions = runPrediction(inputData);
        final List<KIPredictionResult> results = new ArrayList<>();
        for (final long[] prediction : predictions) {
            try {
                results.add(KIPredictionResult.fromArray(prediction));
            } catch (final IllegalArgumentException | ArithmeticException exception) {
                throw new PredictionFailedException(PredictionFailedException.ONNX_PREDICTION_UNKNOWN_RESULTTYPE_ERROR);
            }
        }
        return results;
    }

    /**
     * Schliesst die langlebige ONNX-Session. Wird durch die Registry beim Herunterfahren aufgerufen.
     */
    public void closeSession() {
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

    private long[][] runPrediction(final long[][] inputData) throws PredictionFailedException {
        try (OnnxTensor tensor = OnnxTensor.createTensor(environment, inputData);
                OrtSession.Result result = session.run(Map.of(definition.getInputTensorName(), tensor))) {
            if (result.size() == 0) {
                throw new PredictionFailedException(PredictionFailedException.ONNX_NO_PREDICTION_RESULTS_ERROR);
            }
            final OnnxValue onnxValue = result.get(0);
            if (onnxValue.getType() == OnnxValue.OnnxValueType.ONNX_TYPE_UNKNOWN) {
                throw new PredictionFailedException(PredictionFailedException.ONNX_PREDICTION_UNKNOWN_RESULTTYPE_ERROR);
            }
            // Der Outputvertrag der Radmodelle ist [bewegungsbeziehung][tagessumme].
            return (long[][]) onnxValue.getValue();
        } catch (final OrtException exception) {
            throw new PredictionFailedException(PredictionFailedException.ONNX_RUN_MODEL_ERROR);
        } catch (final ClassCastException exception) {
            throw new PredictionFailedException(PredictionFailedException.ONNX_PREDICTION_UNKNOWN_RESULTTYPE_ERROR);
        }
    }

}
