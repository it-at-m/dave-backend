package de.muenchen.dave.services.hochrechnung;

import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.enums.ModelInputSchema;
import de.muenchen.dave.domain.enums.Zaehldauer;
import de.muenchen.dave.exceptions.PredictionFailedException;
import java.util.List;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

/**
 * Kodiert ausschliesslich die gezahlten Radfahrer je Viertelstunde.
 *
 * <p>
 * Fuer 13h- und 16h-Modelle entstehen dadurch je Bewegungsbeziehung 52 beziehungsweise
 * 64 Merkmale. Nicht erfasste Radwerte werden als {@code 0} an das Modell uebergeben.
 * </p>
 */
@Component
public class ReineRadwerteV1Encoder extends AbstractModelInputEncoder {

    @Override
    public ModelInputSchema getSchema() {
        return ModelInputSchema.REINE_RADWERTE_V1;
    }

    @Override
    public long[][] encode(final Zaehldauer zaehldauer, final List<List<Zeitintervall>> groupedZeitintervalle)
            throws PredictionFailedException {
        final List<List<Zeitintervall>> filteredZeitintervalle = filterAndSort(zaehldauer, groupedZeitintervalle);
        final long[][] inputData = new long[filteredZeitintervalle.size()][];
        for (int groupIndex = 0; groupIndex < filteredZeitintervalle.size(); groupIndex++) {
            final List<Zeitintervall> zeitintervalleJeBewegungsbeziehung = filteredZeitintervalle.get(groupIndex);
            final long[] inputDataJeBewegungsbeziehung = new long[zeitintervalleJeBewegungsbeziehung.size()];
            for (int intervallIndex = 0; intervallIndex < zeitintervalleJeBewegungsbeziehung.size(); intervallIndex++) {
                // ONNX erwartet einen int64-Wert fuer jedes Viertelstundenintervall.
                inputDataJeBewegungsbeziehung[intervallIndex] = ObjectUtils.defaultIfNull(
                        zeitintervalleJeBewegungsbeziehung.get(intervallIndex).getFahrradfahrer(), 0);
            }
            inputData[groupIndex] = inputDataJeBewegungsbeziehung;
        }
        return inputData;
    }

}
