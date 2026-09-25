package de.muenchen.dave.exceptions;

import lombok.Getter;

/**
 * Allgemeine Exception, die für Fehler bei der Arbeit mit ONNX-Vorhersagen genutzt wird.
 */
@Getter
public class PredictionFailedException extends Exception {

    private final String modelId;

    private final String details;

    public static final String NO_VERKEHRSBEZIEHUNGEN = "Outer list (Verkehrsbeziehungen) is empty";
    public static final String ONNX_INVALID_INPUT_DIMENSION = "ONNX invalid input dimension";
    public static final String ONNX_MISSING_INPUT_VALUE = "ONNX input contains a missing counting value";
    public static final String ONNX_UNSUPPORTED_INPUT_VEHICLE = "ONNX input contains an unsupported vehicle type";

    public static final String ONNX_SESSION_CREATION_ERROR = "ONNX session could not be created";
    public static final String ONNX_TENSOR_CREATION_ERROR = "ONNX tensor could not be created";
    public static final String ONNX_NO_PREDICTION_RESULTS_ERROR = "No results returned by prediction";
    public static final String ONNX_PREDICTION_UNKNOWN_RESULTTYPE_ERROR = "Unknown result type returned";
    public static final String ONNX_RUN_MODEL_ERROR = "Error running the model";

    public PredictionFailedException(final String details) {
        super(details);
        this.modelId = null;
        this.details = details;
    }

    public PredictionFailedException(final String modelId, final String details) {
        super(String.format("Modell '%s': %s", modelId, details));
        this.modelId = modelId;
        this.details = details;
    }

}
