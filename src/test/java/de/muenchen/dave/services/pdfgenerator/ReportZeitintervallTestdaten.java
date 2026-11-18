package de.muenchen.dave.services.pdfgenerator;

import de.muenchen.dave.domain.Laengsverkehr;
import de.muenchen.dave.domain.Querungsverkehr;
import de.muenchen.dave.domain.Verkehrsbeziehung;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.enums.Bewegungsrichtung;
import de.muenchen.dave.domain.enums.Himmelsrichtung;
import de.muenchen.dave.domain.enums.TypeZeitintervall;
import de.muenchen.dave.domain.enums.Zaehlart;
import de.muenchen.dave.util.DaveConstants;
import de.muenchen.dave.util.dataimport.ZeitintervallSortingIndexUtil;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

final class ReportZeitintervallTestdaten {

    private ReportZeitintervallTestdaten() {
    }

    static List<Zeitintervall> intervalle(final UUID id, final Zaehlart zaehlart, final int arm, final Set<TypeZeitintervall> types) {
        final var result = new ArrayList<Zeitintervall>();
        final var start = LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(6, 0));
        for (int i = 0; i < 4; i++) {
            final var viertelstunde = zeitintervall(id, zaehlart, arm, TypeZeitintervall.STUNDE_VIERTEL,
                    start.plusMinutes(15L * i), start.plusMinutes(15L * (i + 1)), arm + 1);
            viertelstunde.setSortingIndex(ZeitintervallSortingIndexUtil.getSortingIndexWithinBlock(viertelstunde));
            result.add(viertelstunde);
        }
        final var block = zeitintervall(id, zaehlart, arm, TypeZeitintervall.BLOCK, start, start.plusHours(4), 4 * (arm + 1));
        block.setSortingIndex(ZeitintervallSortingIndexUtil.getSortingIndexWithinBlock(block));
        result.add(block);
        final var gesamt = zeitintervall(id, zaehlart, arm, TypeZeitintervall.GESAMT,
                start.withHour(0), start.withHour(23).withMinute(59), 4 * (arm + 1));
        gesamt.setSortingIndex(ZeitintervallSortingIndexUtil.SORTING_INDEX_GESAMT_DAY);
        result.add(gesamt);
        return result.stream().filter(interval -> types.contains(interval.getType())).toList();
    }

    private static Zeitintervall zeitintervall(final UUID id, final Zaehlart zaehlart, final int arm, final TypeZeitintervall type,
            final LocalDateTime start, final LocalDateTime ende, final int value) {
        final var interval = Zeitintervall.builder().zaehlungId(id).type(type).startUhrzeit(start).endeUhrzeit(ende)
                .pkw(value).lkw(value).lastzuege(value).busse(value).kraftraeder(value).fahrradfahrer(value).fussgaenger(value).build();
        switch (zaehlart) {
        case N, QJS -> {
            final var beziehung = new Verkehrsbeziehung();
            beziehung.setVon(arm);
            beziehung.setNach(arm == 1 ? 2 : 1);
            if (zaehlart == Zaehlart.QJS) {
                beziehung.setStrassenseite(arm == 1 ? Himmelsrichtung.N : Himmelsrichtung.S);
            }
            interval.setVerkehrsbeziehung(beziehung);
        }
        case FJS -> {
            final var beziehung = new Laengsverkehr();
            beziehung.setKnotenarm(arm);
            beziehung.setRichtung(Bewegungsrichtung.EIN);
            beziehung.setStrassenseite(Himmelsrichtung.N);
            interval.setLaengsverkehr(beziehung);
        }
        case QU -> {
            final var beziehung = new Querungsverkehr();
            beziehung.setKnotenarm(arm);
            beziehung.setRichtung(Himmelsrichtung.N);
            interval.setQuerungsverkehr(beziehung);
        }
        default -> throw new IllegalArgumentException();
        }
        return interval;
    }
}
