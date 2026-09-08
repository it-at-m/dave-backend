package de.muenchen.dave.services.hochrechnung;

import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.enums.ModelInputSchema;
import de.muenchen.dave.domain.enums.Zaehldauer;
import de.muenchen.dave.domain.mapper.KIZeitintervallMapper;
import de.muenchen.dave.exceptions.PredictionFailedException;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Kodiert Radwerte zusammen mit den zehn Kontextmerkmalen des historischen 2x4h-Modells.
 *
 * <p>
 * Das Schema bleibt fuer vorhandene Modelle erhalten. Neue Modelle sollen ein eigenes
 * {@link ModelInputSchema} und einen eigenen Encoder erhalten, statt die Merkmalsreihenfolge
 * dieser Implementierung zu veraendern.
 * </p>
 */
@Component
public class KontextRadV1Encoder extends AbstractModelInputEncoder {

    private final KIZeitintervallMapper kiZeitintervallMapper;

    public KontextRadV1Encoder(final KIZeitintervallMapper kiZeitintervallMapper) {
        this.kiZeitintervallMapper = kiZeitintervallMapper;
    }

    @Override
    public ModelInputSchema getSchema() {
        return ModelInputSchema.KONTEXT_RAD_V1;
    }

    @Override
    public long[][] encode(final Zaehldauer zaehldauer, final List<List<Zeitintervall>> groupedZeitintervalle)
            throws PredictionFailedException {
        final List<List<Zeitintervall>> filteredZeitintervalle = filterAndSort(zaehldauer, groupedZeitintervalle);
        final long[][] inputData = new long[filteredZeitintervalle.size()][];
        for (int groupIndex = 0; groupIndex < filteredZeitintervalle.size(); groupIndex++) {
            final List<Zeitintervall> zeitintervalleJeBewegungsbeziehung = filteredZeitintervalle.get(groupIndex);
            final long[] inputdataJeBewegungsbeziehung = new long[zeitintervalleJeBewegungsbeziehung.size() * 10];
            int inputIndex = 0;
            for (final Zeitintervall zeitintervall : zeitintervalleJeBewegungsbeziehung) {
                // Die Mapper-Reihenfolge ist Teil des ONNX-Modellvertrags.
                for (final long feature : kiZeitintervallMapper.zeitintervallToKIZeitintervall(zeitintervall).toArray()) {
                    inputdataJeBewegungsbeziehung[inputIndex++] = feature;
                }
            }
            inputData[groupIndex] = inputdataJeBewegungsbeziehung;
        }
        return inputData;
    }

}
