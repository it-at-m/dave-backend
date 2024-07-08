package de.muenchen.dave.services;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OnnxValue;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import de.muenchen.dave.domain.KIPredictionResult;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.enums.Zaehldauer;
import de.muenchen.dave.domain.mapper.KIZeitintervallMapper;
import de.muenchen.dave.exceptions.PredictionFailedException;
import de.muenchen.dave.util.dataimport.ZeitintervallSortingIndexUtil;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

/**
 * Dieser Service ist für die Vorhersagen mittels ONNX-Modellen zuständig.
 */
@Service
@Slf4j
public class KIService {

    private static final String TENSOR_KEY = "int64_input";
    private final KIZeitintervallMapper mapper;
    private OrtEnvironment environment;
    private OrtSession session;

    @Autowired
    public KIService(@Value("${dave.onnx.model-path}") String modelFilePath, KIZeitintervallMapper mapper) throws PredictionFailedException {
        this.mapper = mapper;
        initializeSession(modelFilePath);
    }

    /**
     * Diese Methode initialisiert die zur Vorhersage notwendige ONNX-Session und lädt hierzu das Modell
     * aus dem Classpath in eine ONNXSession.
     *
     * @param modelFilePath Pfad zum zu nutzenden ONNX-Modell innerhalb des Classpaths.
     */
    private void initializeSession(String modelFilePath) throws PredictionFailedException {
        try {
            environment = OrtEnvironment.getEnvironment();
            ClassPathResource resource = new ClassPathResource(modelFilePath);
            InputStream stream = resource.getInputStream();
            byte[] bytes = IOUtils.toByteArray(stream);
            session = environment.createSession(bytes, new OrtSession.SessionOptions());
            log.info("Successfully created ONNX session");
        } catch (OrtException | IOException e) {
            log.error("ONNX session could not be created\n" + e);
            throw new PredictionFailedException(PredictionFailedException.ONNX_SESSION_CREATION_ERROR);
        }
    }

    /**
     * Diese Methode berechnet für eine zweidimensionale Liste von Zeitintervallen einer Zählung (d.h.
     * für jede Fahrbeziehung der Zählung eine Liste von Zeitintervallen) die resultierenden Tagessummen
     * der einzelnen Fahrzeugklassen.
     *
     * @param zeitintervalle Alle Zeitintervalle einer Zählung für alle Fahrbeziehungen in Form einer
     *            zweidimensionalen Liste
     * @return Ergebnisse als Array von KIPredictionResult
     * @throws PredictionFailedException wenn Eingabedaten falsche Dimension aufweisen
     */
    public KIPredictionResult[] predictHochrechnungTageswerteForZeitIntervalleOfZaehlung(List<List<Zeitintervall>> zeitintervalle)
            throws PredictionFailedException {
        // Prüfung ob korrekter Parameter übergeben wurde
        if (zeitintervalle.isEmpty()) {
            throw new PredictionFailedException(PredictionFailedException.NO_FAHRBEZIEHUNGEN);
        }

        // Ueberfluessige Intervall entfernen
        zeitintervalle = zeitintervalle.stream()
                .map(fahrbeziehung -> fahrbeziehung.stream()
                        .filter(interval -> (interval.getSortingIndex() >= ZeitintervallSortingIndexUtil.SORTING_INDEX_ZB_06_10 &&
                                interval.getSortingIndex() < ZeitintervallSortingIndexUtil.SORTING_INDEX_ZB_10_15) ||
                                (interval.getSortingIndex() >= ZeitintervallSortingIndexUtil.SORTING_INDEX_ZB_15_19 &&
                                        interval.getSortingIndex() < ZeitintervallSortingIndexUtil.SORTING_INDEX_ZB_19_24))
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());

        final Optional<List<Zeitintervall>> wrongDimensions = zeitintervalle.stream()
                .filter(zeitintervall -> zeitintervall.size() != Zaehldauer.DAUER_2_X_4_STUNDEN.getAnzahlZeitintervalle())
                .findAny();
        if (wrongDimensions.isPresent())
            throw new PredictionFailedException(PredictionFailedException.ONNX_INVALID_INPUT_DIMENSION);

        final long[][] predictionData = convertToOnnxCompatibleFormat(zeitintervalle);

        final Map<String, OnnxTensor> tensorData = createTensorData(predictionData);
        final long[][] predictionResult = runPrediction(tensorData);

        return Arrays.stream(predictionResult).map(KIPredictionResult::fromArray).toArray(KIPredictionResult[]::new);
    }

    /**
     * Diese Methode wandelt die zweidimensionale Liste von Zeitintervallen in ein ONNX-kompatibles
     * long[][]-Array um
     *
     * @param zeitintervalle Inputdaten zur Vorhersage als zweidimensionale Liste von Zeitintervallen
     * @return Inputdaten zur Vorhersage als long[][]
     */
    private long[][] convertToOnnxCompatibleFormat(List<List<Zeitintervall>> zeitintervalle) {
        return zeitintervalle.stream()
                .map(fahrbeziehungIntervalle -> fahrbeziehungIntervalle.stream()
                        .map(zeitintervall -> mapper.zeitintervallToKIZeitintervall(zeitintervall).toArray())
                        .flatMapToLong(Arrays::stream)
                        .toArray())
                .toArray(long[][]::new);
    }

    /**
     * @param inputData zweidimensionales long[][]-Array (1. Ebene: Fahrbeziehungen der Zaehlung, 2.
     *            Ebene: Zählungdaten der einzelnen Fahrzeugtypen der Fahrbeziehung)
     * @return Map von OnnxTensor's, die zur Vorhersage benötigt wird.
     * @throws PredictionFailedException wenn bei der Erstellung des Tensors ein Fehler aufgetreten ist.
     */
    private Map<String, OnnxTensor> createTensorData(long[][] inputData) throws PredictionFailedException {
        try {
            final OnnxTensor tensor = OnnxTensor.createTensor(environment, inputData);
            return Map.of(TENSOR_KEY, tensor);
        } catch (OrtException e) {
            log.error(PredictionFailedException.ONNX_TENSOR_CREATION_ERROR + "\n" + e);
            throw new PredictionFailedException(PredictionFailedException.ONNX_TENSOR_CREATION_ERROR);
        }
    }

    /**
     * @param tensorData Inputdaten in Form einer Map von String zu OnnxTensor
     * @return Ergebnisse der Berechnung als long[][]-Array (1. Ebene: Fahrbeziehungen der Zaehlung, 2.
     *         Ebene: Tagessummen der einzelnen Fahrzeugtypen der Fahrbeziehung)
     * @throws PredictionFailedException wenn eine Inkompatibilität der Daten zum Modell vorliegt oder
     *             kein bzw. ein Ergebnis unbekannten Formates zurückgeliefert wurde.
     */
    private long[][] runPrediction(Map<String, OnnxTensor> tensorData) throws PredictionFailedException {
        try (var result = session.run(tensorData)) {
            if (result.size() == 0)
                throw new PredictionFailedException(PredictionFailedException.ONNX_NO_PREDICTION_RESULTS_ERROR);
            final OnnxValue onnxValue = result.get(0);
            if (onnxValue.getType() == OnnxValue.OnnxValueType.ONNX_TYPE_UNKNOWN)
                throw new PredictionFailedException(PredictionFailedException.ONNX_PREDICTION_UNKNOWN_RESULTTYPE_ERROR);
            tensorData.get(TENSOR_KEY).close();
            return (long[][]) onnxValue.getValue();
        } catch (OrtException e) {
            log.error(PredictionFailedException.ONNX_RUN_MODEL_ERROR + "\n" + e);
            throw new PredictionFailedException(PredictionFailedException.ONNX_RUN_MODEL_ERROR);
        }
    }

    /**
     * Diese Methode gibt die ONNXSession und das ONNXEnvironment wieder frei.
     */
    @PreDestroy
    private void closeSession() {
        try {
            session.close();
            environment.close();
            log.info("Closed ONNX session and environment");
        } catch (OrtException e) {
            log.error("ONNX environment could be closed\n" + e);
        }
    }

}
