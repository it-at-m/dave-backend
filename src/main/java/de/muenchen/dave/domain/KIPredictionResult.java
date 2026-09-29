package de.muenchen.dave.domain;

import lombok.Value;

/**
 * Ergebnis einer ONNX-Vorhersage fuer die Tagessumme einer Bewegungsbeziehung.
 *
 * <p>
 * Aktuell liefern alle konfigurierten Modelle nur die Rad-Tagessumme. Weitere Zielgroessen
 * werden erst mit einem erweiterten Ergebnisformat aufgenommen.
 * </p>
 */
@Value
public class KIPredictionResult {

    int radTagessumme;

    /**
     * Erstellt ein KIPredictionResult basierend auf einem long-Array, so wie es von der ONNX-Runtime
     * zurückgegeben wird.
     *
     * @param predictionResults einzelne Ergebniszeile der ONNX-Ausgabe
     * @return vorhergesagte Rad-Tagessumme
     * @throws IllegalArgumentException wenn die falsche Anzahl an Elementen übergeben wurde.
     */
    public static KIPredictionResult fromArray(long[] predictionResults) throws IllegalArgumentException, ArithmeticException {
        if (predictionResults.length != 1)
            throw new IllegalArgumentException("Incorrect amount of elements provided. Array must contain 1 element.");
        return new KIPredictionResult(Math.toIntExact(predictionResults[0]));
    }

}
