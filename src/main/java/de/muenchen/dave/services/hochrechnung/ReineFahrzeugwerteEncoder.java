package de.muenchen.dave.services.hochrechnung;

import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.enums.Fahrzeug;
import de.muenchen.dave.domain.enums.ModelInputSchema;
import de.muenchen.dave.domain.enums.Zaehldauer;
import de.muenchen.dave.exceptions.PredictionFailedException;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
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
        final Function<Zeitintervall, Integer> zaehlwertExtractor = getZaehlwertExtractor(fahrzeug);
        // ONNX-Eingaben duerfen keine fehlenden Zaehlwerte enthalten.
        if (filteredZeitintervalle.stream().flatMap(List::stream).map(zaehlwertExtractor).anyMatch(Objects::isNull)) {
            throw new PredictionFailedException(PredictionFailedException.ONNX_MISSING_INPUT_VALUE);
        }
        // Jede Bewegungsbeziehung wird als eine ONNX-Zeile mit einem int64-Zaehlwert je Viertelstunde kodiert.
        return filteredZeitintervalle.stream()
                .map(zeitintervalleJeBewegungsbeziehung -> zeitintervalleJeBewegungsbeziehung.stream()
                        .map(zaehlwertExtractor)
                        .mapToLong(Integer::longValue)
                        .toArray())
                .toArray(long[][]::new);
    }

    private Function<Zeitintervall, Integer> getZaehlwertExtractor(final Fahrzeug fahrzeug) throws PredictionFailedException {
        return switch (fahrzeug) {
        case RAD -> Zeitintervall::getFahrradfahrer;
        case PKW -> Zeitintervall::getPkw;
        case LKW -> Zeitintervall::getLkw;
        case LZ -> Zeitintervall::getLastzuege;
        case BUS -> Zeitintervall::getBusse;
        case KRAD -> Zeitintervall::getKraftraeder;
        default -> throw new PredictionFailedException(PredictionFailedException.ONNX_UNSUPPORTED_INPUT_VEHICLE);
        };
    }

}
