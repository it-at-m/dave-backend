package de.muenchen.dave.services.hochrechnung;

import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.enums.Zaehldauer;
import de.muenchen.dave.exceptions.PredictionFailedException;
import de.muenchen.dave.util.dataimport.ZeitintervallBaseUtil;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

abstract class AbstractModelInputEncoder implements ModelInputEncoder {

    protected List<List<Zeitintervall>> filterAndSort(final Zaehldauer zaehldauer,
            final List<List<Zeitintervall>> groupedZeitintervalle) throws PredictionFailedException {
        if (groupedZeitintervalle.isEmpty()) {
            throw new PredictionFailedException(PredictionFailedException.NO_VERKEHRSBEZIEHUNGEN);
        }

        final List<List<Zeitintervall>> filteredZeitintervalle = new ArrayList<>();
        for (final List<Zeitintervall> zeitintervalleJeBewegungsbeziehung : groupedZeitintervalle) {
            final List<Zeitintervall> filteredZeitintervalleJeBewegungsbeziehung = new ArrayList<>();
            for (final Zeitintervall zeitintervall : zeitintervalleJeBewegungsbeziehung) {
                if (isInModelInputZeitblock(zaehldauer, zeitintervall)) {
                    filteredZeitintervalleJeBewegungsbeziehung.add(zeitintervall);
                }
            }
            filteredZeitintervalleJeBewegungsbeziehung.sort(Comparator.comparing(Zeitintervall::getSortingIndex));
            if (filteredZeitintervalleJeBewegungsbeziehung.size() != zaehldauer.getAnzahlZeitintervalle()) {
                throw new PredictionFailedException(PredictionFailedException.ONNX_INVALID_INPUT_DIMENSION);
            }
            filteredZeitintervalle.add(filteredZeitintervalleJeBewegungsbeziehung);
        }
        return filteredZeitintervalle;
    }

    private boolean isInModelInputZeitblock(final Zaehldauer zaehldauer, final Zeitintervall zeitintervall) {
        for (final var zeitblock : zaehldauer.getModelInputZeitbloecke()) {
            if (ZeitintervallBaseUtil.isZeitintervallWithinZeitblock(zeitintervall, zeitblock)) {
                return true;
            }
        }
        return false;
    }

}
