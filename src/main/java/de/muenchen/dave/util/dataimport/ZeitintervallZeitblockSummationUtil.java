package de.muenchen.dave.util.dataimport;

import de.muenchen.dave.domain.Bewegungsbeziehung;
import de.muenchen.dave.domain.Laengsverkehr;
import de.muenchen.dave.domain.Querungsverkehr;
import de.muenchen.dave.domain.Verkehrsbeziehung;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.enums.TypeZeitintervall;
import de.muenchen.dave.domain.enums.Zaehldauer;
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
     * {@link Zeitblock}e der gegebenen {@link Zaehldauer} summiert.
     *
     * @param zaehldauer auf Basis deren die Zeitblöcke zur Summierung ermittelt werden.
     * @param zeitintervalle Die zur Summierung vorgesehenen Zeitintervalle.
     * @return Die Summen je {@link Bewegungsbeziehung} und je {@link Zeitblock}.
     */
    public static List<Zeitintervall> getSummen(
            final Zaehldauer zaehldauer,
            final List<Zeitintervall> zeitintervalle) {
        final Map<ZeitintervallBaseUtil.Intervall, List<Zeitintervall>> zeitintervalleGroupedByIntervall = ZeitintervallBaseUtil
                .createByIntervallGroupedZeitintervalle(zeitintervalle);
        final Set<Bewegungsbeziehung> possibleBewegungsbeziehungen = ZeitintervallBaseUtil.getAllPossibleBewegungsbeziehungen(zeitintervalle);
        return possibleBewegungsbeziehungen
                .stream()
                .flatMap(bewegungsbeziehung -> getSummenForBewegungsbeziehungForEachZeitblockGivenInZaehldauer(zaehldauer, bewegungsbeziehung,
                        zeitintervalleGroupedByIntervall).stream())
                .toList();
    }

    /**
     * Summierung der {@link Zeitintervall}e der {@link Bewegungsbeziehung} über alle
     * {@link Zeitblock}e der gegebenen {@link Zaehldauer}.
     *
     * @param zaehldauer auf Basis deren die Zeitblöcke zur Summierung ermittelt werden.
     * @param bewegungsbeziehung Die für die Summierung relevante {@link Bewegungsbeziehung}.
     * @param zeitintervalleGroupedByIntervall Die Zeitintervalle gruppiert nach den einzelnen
     *            Intervallen.
     * @return Die Summen je {@link Zeitblock} für die im Parameter übergebene
     *         {@link Bewegungsbeziehung}.
     */
    static List<Zeitintervall> getSummenForBewegungsbeziehungForEachZeitblockGivenInZaehldauer(
            final Zaehldauer zaehldauer,
            final Bewegungsbeziehung bewegungsbeziehung,
            final Map<ZeitintervallBaseUtil.Intervall, List<Zeitintervall>> zeitintervalleGroupedByIntervall) {
        final List<Zeitintervall> zeitintervalleForBewegungsbeziehung = ZeitintervallBaseUtil.getZeitintervalleForBewegungsbeziehung(
                bewegungsbeziehung,
                zeitintervalleGroupedByIntervall);
        final Optional<UUID> zaehlungId = zeitintervalleForBewegungsbeziehung.stream()
                .map(Zeitintervall::getZaehlungId)
                .findFirst();
        List<Zeitintervall> summen = new ArrayList<>();
        if (zaehlungId.isPresent()) {
            summen = getZeitbloeckeAccordingZaehldauer(zaehldauer)
                    .stream()
                    .map(zeitblock -> getSumme(zaehlungId.get(), zeitblock, bewegungsbeziehung, zeitintervalleForBewegungsbeziehung))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .toList();
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
    static Optional<Zeitintervall> getSumme(
            final UUID zaehlungId,
            final Zeitblock zeitblock,
            final Bewegungsbeziehung bewegungsbeziehung,
            final List<Zeitintervall> sortedZeitintervalle) {
        final Optional<Zeitintervall> summeOptional;
        Verkehrsbeziehung verkehrsbeziehung = null;
        Laengsverkehr laengsverkehr = null;
        Querungsverkehr querungsverkehr = null;
        if (bewegungsbeziehung instanceof Verkehrsbeziehung) {
            verkehrsbeziehung = (Verkehrsbeziehung) bewegungsbeziehung;
        } else if (bewegungsbeziehung instanceof Laengsverkehr) {
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

    public static List<Zeitblock> getZeitbloeckeAccordingZaehldauer(final Zaehldauer zaehldauer) {
        if (Zaehldauer.DAUER_2_X_4_STUNDEN.equals(zaehldauer)) {
            return getZeitbloeckeFor2x4h();
        } else if (Zaehldauer.DAUER_13_STUNDEN.equals(zaehldauer)) {
            return getZeitbloeckeFor13h();
        } else if (Zaehldauer.DAUER_16_STUNDEN.equals(zaehldauer)) {
            return getZeitbloeckeFor16h();
        } else if (Zaehldauer.DAUER_24_STUNDEN.equals(zaehldauer)) {
            return getZeitbloeckeFor24h();
        } else {
            // Zaehldauer.SONSTIGE
            return getZeitbloeckeFor24h();
        }
    }

    public static List<Zeitblock> getZeitbloeckeFor24h() {
        final var bloecke = Stream.of(
                Zeitblock.ZB_00_06,
                Zeitblock.ZB_06_10,
                Zeitblock.ZB_10_15,
                Zeitblock.ZB_15_19,
                Zeitblock.ZB_19_24,
                Zeitblock.ZB_00_24);

        final var hours = Stream.of(
                Zeitblock.ZB_00_01,
                Zeitblock.ZB_01_02,
                Zeitblock.ZB_02_03,
                Zeitblock.ZB_03_04,
                Zeitblock.ZB_04_05,
                Zeitblock.ZB_05_06,
                Zeitblock.ZB_06_07,
                Zeitblock.ZB_07_08,
                Zeitblock.ZB_08_09,
                Zeitblock.ZB_09_10,
                Zeitblock.ZB_10_11,
                Zeitblock.ZB_11_12,
                Zeitblock.ZB_12_13,
                Zeitblock.ZB_13_14,
                Zeitblock.ZB_14_15,
                Zeitblock.ZB_15_16,
                Zeitblock.ZB_16_17,
                Zeitblock.ZB_17_18,
                Zeitblock.ZB_18_19,
                Zeitblock.ZB_19_20,
                Zeitblock.ZB_20_21,
                Zeitblock.ZB_21_22,
                Zeitblock.ZB_22_23,
                Zeitblock.ZB_23_24);

        final var halfHours = Stream.of(
                Zeitblock.ZB_0000_0030,
                Zeitblock.ZB_0030_0100,
                Zeitblock.ZB_0100_0130,
                Zeitblock.ZB_0130_0200,
                Zeitblock.ZB_0200_0230,
                Zeitblock.ZB_0230_0300,
                Zeitblock.ZB_0300_0330,
                Zeitblock.ZB_0330_0400,
                Zeitblock.ZB_0400_0430,
                Zeitblock.ZB_0430_0500,
                Zeitblock.ZB_0500_0530,
                Zeitblock.ZB_0530_0600,
                Zeitblock.ZB_0600_0630,
                Zeitblock.ZB_0630_0700,
                Zeitblock.ZB_0700_0730,
                Zeitblock.ZB_0730_0800,
                Zeitblock.ZB_0800_0830,
                Zeitblock.ZB_0830_0900,
                Zeitblock.ZB_0900_0930,
                Zeitblock.ZB_0930_1000,
                Zeitblock.ZB_1000_1030,
                Zeitblock.ZB_1030_1100,
                Zeitblock.ZB_1100_1130,
                Zeitblock.ZB_1130_1200,
                Zeitblock.ZB_1200_1230,
                Zeitblock.ZB_1230_1300,
                Zeitblock.ZB_1300_1330,
                Zeitblock.ZB_1330_1400,
                Zeitblock.ZB_1400_1430,
                Zeitblock.ZB_1430_1500,
                Zeitblock.ZB_1500_1530,
                Zeitblock.ZB_1530_1600,
                Zeitblock.ZB_1600_1630,
                Zeitblock.ZB_1630_1700,
                Zeitblock.ZB_1700_1730,
                Zeitblock.ZB_1730_1800,
                Zeitblock.ZB_1800_1830,
                Zeitblock.ZB_1830_1900,
                Zeitblock.ZB_1900_1930,
                Zeitblock.ZB_1930_2000,
                Zeitblock.ZB_2000_2030,
                Zeitblock.ZB_2030_2100,
                Zeitblock.ZB_2100_2130,
                Zeitblock.ZB_2130_2200,
                Zeitblock.ZB_2200_2230,
                Zeitblock.ZB_2230_2300,
                Zeitblock.ZB_2300_2330,
                Zeitblock.ZB_2330_2400);

        return Stream
                .of(bloecke, hours, halfHours)
                .flatMap(Stream::distinct)
                .toList();
    }

    public static List<Zeitblock> getZeitbloeckeFor16h() {
        final var bloecke = Stream.of(
                Zeitblock.ZB_06_10,
                Zeitblock.ZB_10_15,
                Zeitblock.ZB_15_19,
                Zeitblock.ZB_19_22,
                Zeitblock.ZB_06_22,
                Zeitblock.ZB_00_24);

        final var hours = Stream.of(
                Zeitblock.ZB_06_07,
                Zeitblock.ZB_07_08,
                Zeitblock.ZB_08_09,
                Zeitblock.ZB_09_10,
                Zeitblock.ZB_10_11,
                Zeitblock.ZB_11_12,
                Zeitblock.ZB_12_13,
                Zeitblock.ZB_13_14,
                Zeitblock.ZB_14_15,
                Zeitblock.ZB_15_16,
                Zeitblock.ZB_16_17,
                Zeitblock.ZB_17_18,
                Zeitblock.ZB_18_19,
                Zeitblock.ZB_19_20,
                Zeitblock.ZB_20_21,
                Zeitblock.ZB_21_22);

        final var halfHours = Stream.of(
                Zeitblock.ZB_0600_0630,
                Zeitblock.ZB_0630_0700,
                Zeitblock.ZB_0700_0730,
                Zeitblock.ZB_0730_0800,
                Zeitblock.ZB_0800_0830,
                Zeitblock.ZB_0830_0900,
                Zeitblock.ZB_0900_0930,
                Zeitblock.ZB_0930_1000,
                Zeitblock.ZB_1000_1030,
                Zeitblock.ZB_1030_1100,
                Zeitblock.ZB_1100_1130,
                Zeitblock.ZB_1130_1200,
                Zeitblock.ZB_1200_1230,
                Zeitblock.ZB_1230_1300,
                Zeitblock.ZB_1300_1330,
                Zeitblock.ZB_1330_1400,
                Zeitblock.ZB_1400_1430,
                Zeitblock.ZB_1430_1500,
                Zeitblock.ZB_1500_1530,
                Zeitblock.ZB_1530_1600,
                Zeitblock.ZB_1600_1630,
                Zeitblock.ZB_1630_1700,
                Zeitblock.ZB_1700_1730,
                Zeitblock.ZB_1730_1800,
                Zeitblock.ZB_1800_1830,
                Zeitblock.ZB_1830_1900,
                Zeitblock.ZB_1900_1930,
                Zeitblock.ZB_1930_2000,
                Zeitblock.ZB_2000_2030,
                Zeitblock.ZB_2030_2100,
                Zeitblock.ZB_2100_2130,
                Zeitblock.ZB_2130_2200);

        return Stream
                .of(bloecke, hours, halfHours)
                .flatMap(Stream::distinct)
                .toList();
    }

    public static List<Zeitblock> getZeitbloeckeFor13h() {
        final var bloecke = Stream.of(
                Zeitblock.ZB_06_10,
                Zeitblock.ZB_10_15,
                Zeitblock.ZB_15_19,
                Zeitblock.ZB_06_19,
                Zeitblock.ZB_00_24);

        final var hours = Stream.of(
                Zeitblock.ZB_06_07,
                Zeitblock.ZB_07_08,
                Zeitblock.ZB_08_09,
                Zeitblock.ZB_09_10,
                Zeitblock.ZB_10_11,
                Zeitblock.ZB_11_12,
                Zeitblock.ZB_12_13,
                Zeitblock.ZB_13_14,
                Zeitblock.ZB_14_15,
                Zeitblock.ZB_15_16,
                Zeitblock.ZB_16_17,
                Zeitblock.ZB_17_18,
                Zeitblock.ZB_18_19);

        final var halfHours = Stream.of(
                Zeitblock.ZB_0600_0630,
                Zeitblock.ZB_0630_0700,
                Zeitblock.ZB_0700_0730,
                Zeitblock.ZB_0730_0800,
                Zeitblock.ZB_0800_0830,
                Zeitblock.ZB_0830_0900,
                Zeitblock.ZB_0900_0930,
                Zeitblock.ZB_0930_1000,
                Zeitblock.ZB_1000_1030,
                Zeitblock.ZB_1030_1100,
                Zeitblock.ZB_1100_1130,
                Zeitblock.ZB_1130_1200,
                Zeitblock.ZB_1200_1230,
                Zeitblock.ZB_1230_1300,
                Zeitblock.ZB_1300_1330,
                Zeitblock.ZB_1330_1400,
                Zeitblock.ZB_1400_1430,
                Zeitblock.ZB_1430_1500,
                Zeitblock.ZB_1500_1530,
                Zeitblock.ZB_1530_1600,
                Zeitblock.ZB_1600_1630,
                Zeitblock.ZB_1630_1700,
                Zeitblock.ZB_1700_1730,
                Zeitblock.ZB_1730_1800,
                Zeitblock.ZB_1800_1830,
                Zeitblock.ZB_1830_1900);

        return Stream
                .of(bloecke, hours, halfHours)
                .flatMap(Stream::distinct)
                .toList();
    }

    public static List<Zeitblock> getZeitbloeckeFor2x4h() {
        final var bloecke = Stream.of(
                Zeitblock.ZB_06_10,
                Zeitblock.ZB_15_19,
                Zeitblock.ZB_00_24);

        final var hours = Stream.of(
                Zeitblock.ZB_06_07,
                Zeitblock.ZB_07_08,
                Zeitblock.ZB_08_09,
                Zeitblock.ZB_09_10,
                Zeitblock.ZB_15_16,
                Zeitblock.ZB_16_17,
                Zeitblock.ZB_17_18,
                Zeitblock.ZB_18_19);

        final var halfHours = Stream.of(
                Zeitblock.ZB_0600_0630,
                Zeitblock.ZB_0630_0700,
                Zeitblock.ZB_0700_0730,
                Zeitblock.ZB_0730_0800,
                Zeitblock.ZB_0800_0830,
                Zeitblock.ZB_0830_0900,
                Zeitblock.ZB_0900_0930,
                Zeitblock.ZB_0930_1000,
                Zeitblock.ZB_1500_1530,
                Zeitblock.ZB_1530_1600,
                Zeitblock.ZB_1600_1630,
                Zeitblock.ZB_1630_1700,
                Zeitblock.ZB_1700_1730,
                Zeitblock.ZB_1730_1800,
                Zeitblock.ZB_1800_1830,
                Zeitblock.ZB_1830_1900);

        return Stream
                .of(bloecke, hours, halfHours)
                .flatMap(Stream::distinct)
                .toList();
    }

}
