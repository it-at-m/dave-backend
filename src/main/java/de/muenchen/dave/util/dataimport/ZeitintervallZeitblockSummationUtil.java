package de.muenchen.dave.util.dataimport;

import de.muenchen.dave.domain.Bewegungsbeziehung;
import de.muenchen.dave.domain.Laengsverkehr;
import de.muenchen.dave.domain.Querungsverkehr;
import de.muenchen.dave.domain.Verkehrsbeziehung;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.enums.TypeZeitintervall;
import de.muenchen.dave.domain.enums.Zeitblock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;

/**
 * Diese Klasse bildet die Summen je möglichen {@link Zeitblock} je {@link Verkehrsbeziehung}.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ZeitintervallZeitblockSummationUtil {

    /**
     * In dieser Methode werden die {@link Zeitintervall}e je {@link Bewegungsbeziehung} über die
     * {@link Zeitblock}e summiert.
     *
     * @param zeitintervalle Die zur Summierung vorgesehenen Zeitintervalle.
     * @return Die Summen je {@link Bewegungsbeziehung} und je {@link Zeitblock}.
     */
    public static List<Zeitintervall> getSummen(final List<Zeitintervall> zeitintervalle) {
        final Map<ZeitintervallBaseUtil.Intervall, List<Zeitintervall>> zeitintervalleGroupedByIntervall = ZeitintervallBaseUtil
                .createByIntervallGroupedZeitintervalle(zeitintervalle);
        final Set<Bewegungsbeziehung> possibleBewegungsbeziehungen = ZeitintervallBaseUtil.getAllPossibleBewegungsbeziehungen(zeitintervalle);
        final List<Zeitintervall> blockSummen = new ArrayList<>();
        possibleBewegungsbeziehungen
                .forEach(bewegungsbeziehung -> blockSummen.addAll(getSummenForBewegungsbeziehung(bewegungsbeziehung, zeitintervalleGroupedByIntervall)));
        return blockSummen;
    }

    /**
     * Summierung der {@link Zeitintervall}e der {@link Bewegungsbeziehung} über alle
     * {@link Zeitblock}e.
     *
     * @param bewegungsbeziehung Die für die Summierung relevante {@link Bewegungsbeziehung}.
     * @param zeitintervalleGroupedByIntervall Die Zeitintervalle gruppiert nach den einzelnen
     *            Intervallen.
     * @return Die Summen je {@link Zeitblock} für die im Parameter übergebene
     *         {@link Verkehrsbeziehung}.
     */
    protected static List<Zeitintervall> getSummenForBewegungsbeziehung(
            final Bewegungsbeziehung bewegungsbeziehung,
            final Map<ZeitintervallBaseUtil.Intervall, List<Zeitintervall>> zeitintervalleGroupedByIntervall) {
        final List<Zeitintervall> zeitintervalleForBewegungsbeziehung = ZeitintervallBaseUtil.getZeitintervalleForBewegungsbeziehung(
                bewegungsbeziehung,
                zeitintervalleGroupedByIntervall);
        final Optional<UUID> zaehlungId = zeitintervalleForBewegungsbeziehung.stream()
                .map(Zeitintervall::getZaehlungId)
                .findFirst();
        final StartEndeUhrzeit startEndeUhrzeit = getStartAndEndeuhrzeit(zeitintervalleForBewegungsbeziehung);
        List<Zeitintervall> summen = new ArrayList<>();
        if (zaehlungId.isPresent()) {
            Stream.of(Zeitblock.values())
                    .filter(zeitblock -> shouldZeitblockBeCreated(zeitblock, startEndeUhrzeit))
                    .forEach(zeitblock -> getSumme(zaehlungId.get(), zeitblock, bewegungsbeziehung, zeitintervalleForBewegungsbeziehung)
                            .ifPresent(summen::add));
        }
        return summen;
    }

    /**
     * Summierung der {@link Zeitintervall} einer Verkehrsbeziehung.
     *
     * @param zaehlungId Die ID der Zaehlung.
     * @param zeitblock Der {@link Zeitblock} für welchen die Summe ermittelt werden soll.
     * @param bewegungsbeziehung Die im Rückgabewert der Methode gesetzte Bewegungsbeziehung.
     * @param sortedZeitintervalle Die aufsteigend sortierten {@link Zeitintervall}e einer
     *            {@link Verkehrsbeziehung}.
     * @return Die Summe für den {@link Zeitblock} als {@link Zeitintervall}.
     */
    protected static Optional<Zeitintervall> getSumme(
            final UUID zaehlungId,
            final Zeitblock zeitblock,
            final Bewegungsbeziehung bewegungsbeziehung,
            final List<Zeitintervall> sortedZeitintervalle) {
        final Optional<Zeitintervall> summeOptional;
        Verkehrsbeziehung verkehrsbeziehung = null;
        Laengsverkehr laengsverkehr = null;
        Querungsverkehr querungsverkehr = null;
        if (Verkehrsbeziehung.class.equals(bewegungsbeziehung.getClass())) {
            verkehrsbeziehung = (Verkehrsbeziehung) bewegungsbeziehung;
        } else if (Laengsverkehr.class.equals(bewegungsbeziehung.getClass())) {
            laengsverkehr = (Laengsverkehr) bewegungsbeziehung;
        } else {
            querungsverkehr = (Querungsverkehr) bewegungsbeziehung;
        }
        Zeitintervall zeitintervallSumme = ZeitintervallBaseUtil.createZeitintervallWithoutCountingValues(
                zaehlungId,
                zeitblock.getStart(),
                zeitblock.getEnd(),
                zeitblock.getTypeZeitintervall(),
                verkehrsbeziehung,
                laengsverkehr,
                querungsverkehr);
        // Holen der Zeitintervalle eines Zeitblocks
        final List<Zeitintervall> zeitintervalleWithinBlock = sortedZeitintervalle.stream()
                .filter(zeitintervall -> ZeitintervallBaseUtil.isZeitintervallWithinZeitblock(zeitintervall, zeitblock))
                .collect(Collectors.toList());
        // Erstellen der Summe des Zeitblocks
        if (zeitintervalleWithinBlock.isEmpty()) {
            summeOptional = Optional.empty();
        } else {
            // Summierung der Zeitintervalle
            zeitintervallSumme = zeitintervalleWithinBlock.stream()
                    .reduce(
                            zeitintervallSumme, //
                            ZeitintervallBaseUtil::summation);
            // Setzen des Sortierindex
            if (zeitblock.equals(Zeitblock.ZB_00_24)) {
                zeitintervallSumme.setSortingIndex(ZeitintervallSortingIndexUtil.getSortingIndexGesamtCompleteDay());
            } else if (zeitblock.getTypeZeitintervall().equals(TypeZeitintervall.BLOCK_SPEZIAL)) {
                zeitintervallSumme.setSortingIndex(ZeitintervallSortingIndexUtil.getSortingIndexBlockSpezial());
            } else {
                zeitintervallSumme.setSortingIndex(
                        ZeitintervallSortingIndexUtil.getSortingIndexWithinBlock(zeitintervallSumme));
            }
            // Ermitteln der Start- und Endeuhrzeit aus Zeitintervallen des Zeitblocks
            final StartEndeUhrzeit startEndeUhrzeit = getStartAndEndeuhrzeit(zeitintervalleWithinBlock);
            zeitintervallSumme.setStartUhrzeit(startEndeUhrzeit.getStartUhrzeit());
            zeitintervallSumme.setEndeUhrzeit(startEndeUhrzeit.getEndeUhrzeit());
            summeOptional = Optional.of(zeitintervallSumme);
        }
        return summeOptional;
    }

    /**
     * Mit dieser Methode wird speziell auf eine Erstellung der Zeitblöcke {@link Zeitblock#ZB_06_19}
     * und {@link Zeitblock#ZB_06_22} geprüft.
     *
     * @param zeitblock zur Prüfung.
     * @param startEndeUhrzeit zur Prüfung.
     * @return Handelt es sich im Parameter zeitblock um einen dieser beiden Zeitblöcke und die Anzahl
     *         an Stunden zwischen der Start- und Endeuhrzeit entspricht
     *         nicht dem Zeitblock, so wird false zurückgegeben. Andernfalls wird true zurückgegeben.
     */
    private static boolean shouldZeitblockBeCreated(final Zeitblock zeitblock,
            final StartEndeUhrzeit startEndeUhrzeit) {
        boolean shouldCreated = !zeitblock.getTypeZeitintervall().equals(TypeZeitintervall.BLOCK_SPEZIAL)
                || (startEndeUhrzeit.getStartUhrzeit().equals(zeitblock.getStart())
                        && startEndeUhrzeit.getEndeUhrzeit().equals(zeitblock.getEnd()));
        return shouldCreated;
    }

    /**
     * Diese Methode ermittelt für die im Parameter übergebenen {@link Zeitintervall}e die Startuhrzeit
     * des frühesten Zeitintervalls und die Endeuhrzeit des
     * ältesten Zeitintervalls.
     *
     * @param zeitintervalle zur Ermittlung der Start- und Endeuhrzeit.
     * @return die Startuhrzeit des frühesten Zeitintervalls und Endeuhrzeit des ältesten
     *         Zeitintervalls.
     */
    private static StartEndeUhrzeit getStartAndEndeuhrzeit(final List<Zeitintervall> zeitintervalle) {
        final StartEndeUhrzeit startEndeUhrzeit = new StartEndeUhrzeit();
        zeitintervalle.forEach(zeitintervall -> {
            if (ObjectUtils.isEmpty(startEndeUhrzeit.getStartUhrzeit())
                    || zeitintervall.getStartUhrzeit().isBefore(startEndeUhrzeit.getStartUhrzeit())) {
                startEndeUhrzeit.setStartUhrzeit(zeitintervall.getStartUhrzeit());
            }
            if (ObjectUtils.isEmpty(startEndeUhrzeit.getEndeUhrzeit())
                    || zeitintervall.getEndeUhrzeit().isAfter(startEndeUhrzeit.getEndeUhrzeit())) {
                startEndeUhrzeit.setEndeUhrzeit(zeitintervall.getEndeUhrzeit());
            }
        });
        return startEndeUhrzeit;
    }

    @Data
    public static class StartEndeUhrzeit {

        private LocalDateTime startUhrzeit;

        private LocalDateTime endeUhrzeit;

    }

}
