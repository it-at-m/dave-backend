package de.muenchen.dave.domain;

import lombok.Value;

/**
 * Diese Klasse repräsentiert das Ergebnis einer Vorhersage über Tagessummen der verschiedenen Fahrzeugklassen.
 */
@Value
public class KIPredictionResult {

    int radTagessumme;

    /**
     * Erstellt ein KIPredictionResult basierend auf einem long-Array, so wie es von der ONNX-Runtime zurückgegeben wird.
     *
     * @param predictionResults Rückgabe der Vorhersage mittels ONNX
     * @return Vorhersage über Tagessummen der verschiedenen Fahrzeugklassen
     * @throws IllegalArgumentException wenn die falsche Anzahl an Elementen übergeben wurde.
     */
    public static KIPredictionResult fromArray(long[] predictionResults) throws IllegalArgumentException, ArithmeticException {
        if (predictionResults.length != 1) throw new IllegalArgumentException("Incorrect amount of elements provided. Array must contain 1 element.");
        return new KIPredictionResult(Math.toIntExact(predictionResults[0]));
    }

}
