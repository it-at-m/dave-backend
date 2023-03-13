package de.muenchen.dave.exceptions;

import lombok.Data;

/**
 * Allgemeine Exception, die für Fehler bei der Arbeit mit ONNX-Vorhersagen genutzt wird.
 */
@Data
public class PredictionFailedException extends Exception {

    public static final String NO_FAHRBEZIEHUNGEN = "Outer list (Fahrbeziehungen) is empty";
    public static final String ONNX_INVALID_INPUT_DIMENSION = "ONNX invalid input dimension";

    public static final String ONNX_SESSION_CREATION_ERROR = "ONNX session could not be created";
    public static final String ONNX_TENSOR_CREATION_ERROR = "ONNX tensor could not be created";
    public static final String ONNX_NO_PREDICTION_RESULTS_ERROR = "No results returned by prediction";
    public static final String ONNX_PREDICTION_UNKNOWN_RESULTTYPE_ERROR = "Unknown result type returned";
    public static final String ONNX_RUN_MODEL_ERROR = "Error running the model";
    public PredictionFailedException(final String message) {
        super(message);
    }

}
