package de.muenchen.dave.services.hochrechnung;

import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.enums.Fahrzeug;
import de.muenchen.dave.domain.enums.ModelInputSchema;
import de.muenchen.dave.domain.enums.Zaehldauer;
import de.muenchen.dave.exceptions.PredictionFailedException;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Kodiert ausschliesslich die Zaehlwerte eines Fahrzeugs je Viertelstunde.
 *
 * <p>
 * Fuer 13h- und 16h-Modelle entstehen dadurch je Bewegungsbeziehung 52 beziehungsweise
 * 64 Merkmale. Fehlende Zaehlwerte verhindern die Vorhersage.
 * </p>
 */
@Component
public class ReineFahrzeugwerteEncoder extends AbstractModelInputEncoder {

    @Override
    public ModelInputSchema getSchema() {
        return ModelInputSchema.REINE_FAHRZEUGWERTE;
    }

    @Override
    public long[][] encode(final Zaehldauer zaehldauer,
            final Fahrzeug fahrzeug,
            final List<List<Zeitintervall>> groupedZeitintervalle)
            throws PredictionFailedException {
        final List<List<Zeitintervall>> filteredZeitintervalle = filterAndSort(zaehldauer, groupedZeitintervalle);
        final long[][] inputData = new long[filteredZeitintervalle.size()][];
        for (int groupIndex = 0; groupIndex < filteredZeitintervalle.size(); groupIndex++) {
            final List<Zeitintervall> zeitintervalleJeBewegungsbeziehung = filteredZeitintervalle.get(groupIndex);
            final long[] inputDataJeBewegungsbeziehung = new long[zeitintervalleJeBewegungsbeziehung.size()];
            for (int intervallIndex = 0; intervallIndex < zeitintervalleJeBewegungsbeziehung.size(); intervallIndex++) {
                // ONNX erwartet einen int64-Wert fuer jedes Viertelstundenintervall.
                inputDataJeBewegungsbeziehung[intervallIndex] = getZaehlwert(
                        zeitintervalleJeBewegungsbeziehung.get(intervallIndex), fahrzeug);
            }
            inputData[groupIndex] = inputDataJeBewegungsbeziehung;
        }
        return inputData;
    }

    private Integer getZaehlwert(final Zeitintervall zeitintervall, final Fahrzeug fahrzeug) throws PredictionFailedException {
        final Integer zaehlwert = switch (fahrzeug) {
        case RAD -> zeitintervall.getFahrradfahrer();
        case PKW -> zeitintervall.getPkw();
        case LKW -> zeitintervall.getLkw();
        case LZ -> zeitintervall.getLastzuege();
        case BUS -> zeitintervall.getBusse();
        case KRAD -> zeitintervall.getKraftraeder();
        default -> throw new PredictionFailedException(PredictionFailedException.ONNX_UNSUPPORTED_INPUT_VEHICLE);
        };
        if (zaehlwert == null) {
            throw new PredictionFailedException(PredictionFailedException.ONNX_MISSING_INPUT_VALUE);
        }
        return zaehlwert;
    }

}
