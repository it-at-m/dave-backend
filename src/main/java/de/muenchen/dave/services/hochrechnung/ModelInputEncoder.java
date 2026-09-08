package de.muenchen.dave.services.hochrechnung;

import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.enums.ModelInputSchema;
import de.muenchen.dave.domain.enums.Zaehldauer;
import de.muenchen.dave.exceptions.PredictionFailedException;
import java.util.List;

/**
 * Kodiert die Zeitintervalle einer Zaehlung in das Eingabeformat eines ONNX-Modells.
 *
 * <p>
 * Das Eingabeschema wird in der Modellkonfiguration angegeben und dient der
 * {@link OnnxModelRegistry} zur Auswahl des passenden Encoders. Jede Zeile des erzeugten
 * zweidimensionalen Arrays repraesentiert genau eine Bewegungsbeziehung.
 * </p>
 */
public interface ModelInputEncoder {

    /**
     * Liefert das von diesem Encoder unterstuetzte Schema.
     *
     * @return Schema, das in {@code dave.onnx.modelle[*].input-schema} konfiguriert wird
     */
    ModelInputSchema getSchema();

    /**
     * Filtert, sortiert und kodiert die Zeitintervalle fuer die ONNX-Inferenz.
     *
     * @param zaehldauer bestimmt die Eingabezeitbloecke und die erwartete Intervallzahl
     * @param groupedZeitintervalle Zeitintervalle, je Bewegungsbeziehung gruppiert
     * @return ONNX-Eingabetensor als {@code [bewegungsbeziehung][merkmale]}
     * @throws PredictionFailedException wenn keine Bewegungsbeziehung oder nicht die erwartete
     *             Anzahl von Intervallen vorliegt
     */
    long[][] encode(final Zaehldauer zaehldauer, final List<List<Zeitintervall>> groupedZeitintervalle)
            throws PredictionFailedException;

}
