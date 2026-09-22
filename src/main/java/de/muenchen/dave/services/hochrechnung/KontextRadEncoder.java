package de.muenchen.dave.services.hochrechnung;

import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.enums.Fahrzeug;
import de.muenchen.dave.domain.enums.ModelInputSchema;
import de.muenchen.dave.domain.enums.Zaehldauer;
import de.muenchen.dave.domain.mapper.KIZeitintervallMapper;
import de.muenchen.dave.exceptions.PredictionFailedException;
import java.util.Arrays;
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
public class KontextRadEncoder extends AbstractModelInputEncoder {

    private final KIZeitintervallMapper kiZeitintervallMapper;

    public KontextRadEncoder(final KIZeitintervallMapper kiZeitintervallMapper) {
        this.kiZeitintervallMapper = kiZeitintervallMapper;
    }

    @Override
    public ModelInputSchema getSchema() {
        return ModelInputSchema.KONTEXT_RAD;
    }

    @Override
    public long[][] encode(final Zaehldauer zaehldauer,
            final Fahrzeug fahrzeug,
            final List<List<Zeitintervall>> groupedZeitintervalle)
            throws PredictionFailedException {
        // Jeder Mapper-Output wird in seiner unveraenderten Merkmalsreihenfolge in die ONNX-Zeile uebernommen.
        return filterAndSort(zaehldauer, groupedZeitintervalle).stream()
                .map(this::encodeBewegungsbeziehung)
                .toArray(long[][]::new);
    }

    private long[] encodeBewegungsbeziehung(final List<Zeitintervall> zeitintervalleJeBewegungsbeziehung) {
        return zeitintervalleJeBewegungsbeziehung.stream()
                .flatMapToLong(zeitintervall -> Arrays.stream(
                        kiZeitintervallMapper.zeitintervallToKIZeitintervall(zeitintervall).toArray()))
                .toArray();
    }

}
