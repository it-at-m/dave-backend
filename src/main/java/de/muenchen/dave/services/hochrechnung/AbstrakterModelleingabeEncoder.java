package de.muenchen.dave.services.hochrechnung;

import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.enums.Zaehldauer;
import de.muenchen.dave.exceptions.PredictionFailedException;
import de.muenchen.dave.util.dataimport.ZeitintervallBaseUtil;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

abstract class AbstrakterModelleingabeEncoder implements ModelleingabeEncoder {

    protected List<List<Zeitintervall>> filtereUndSortiere(final Zaehldauer zaehldauer,
            final List<List<Zeitintervall>> gruppierteZeitintervalle) throws PredictionFailedException {
        if (gruppierteZeitintervalle.isEmpty()) {
            throw new PredictionFailedException(PredictionFailedException.NO_VERKEHRSBEZIEHUNGEN);
        }

        final List<List<Zeitintervall>> gefilterteZeitintervalle = new ArrayList<>();
        for (final List<Zeitintervall> zeitintervalleJeBewegungsbeziehung : gruppierteZeitintervalle) {
            final List<Zeitintervall> gefilterteZeitintervalleJeBewegungsbeziehung = new ArrayList<>();
            for (final Zeitintervall zeitintervall : zeitintervalleJeBewegungsbeziehung) {
                if (liegtInModelleingabeZeitblock(zaehldauer, zeitintervall)) {
                    gefilterteZeitintervalleJeBewegungsbeziehung.add(zeitintervall);
                }
            }
            gefilterteZeitintervalleJeBewegungsbeziehung.sort(Comparator.comparing(Zeitintervall::getSortingIndex));
            if (gefilterteZeitintervalleJeBewegungsbeziehung.size() != zaehldauer.getAnzahlZeitintervalle()) {
                throw new PredictionFailedException(PredictionFailedException.ONNX_INVALID_INPUT_DIMENSION);
            }
            gefilterteZeitintervalle.add(gefilterteZeitintervalleJeBewegungsbeziehung);
        }
        return gefilterteZeitintervalle;
    }

    private boolean liegtInModelleingabeZeitblock(final Zaehldauer zaehldauer, final Zeitintervall zeitintervall) {
        for (final var zeitblock : zaehldauer.getModelleingabeZeitbloecke()) {
            if (ZeitintervallBaseUtil.isZeitintervallWithinZeitblock(zeitintervall, zeitblock)) {
                return true;
            }
        }
        return false;
    }

}
