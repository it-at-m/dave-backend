package de.muenchen.dave.util.messstelle;

import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.dtos.laden.messwerte.LadeMesswerteDTO;
import de.muenchen.dave.domain.enums.TypeZeitintervall;
import de.muenchen.dave.domain.enums.Zeitblock;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Der Sortierindex ist erforderlich, um bei der Extraktion der Zeitintervalle einer Messung diese
 * in der richtigen Reihenfolge zu erhalten.
 * <p>
 * Der Sortierindex baut sich folgendermaßen auf.
 * <p>
 * Bedeutung der Dezimalstellen im Sortierindex:
 * - Stelle 9-8 (XX0000000): Zu welchem Block gehört der Messwert, Gesamt/Tagessumme oder SpStd
 * (Tag).
 * - Stelle 7 (00X000000): Die oberste Sortierreihenfolge innerhalb eines Blocks (Zeitintervall,
 * Blocksumme der SpStd).
 * - Stelle 6-4 (000XXX000): Der Index ermittelt aus der Endeuhrzeit auf Basis der
 * Viertelstundenintervalle eines Tages.
 * - Stelle 3-1 (000000XXX): Für Zeitintervalle des Typs {@link TypeZeitintervall#STUNDE_VIERTEL}
 * und {@link TypeZeitintervall#STUNDE_HALB} wird der Index der
 * Startuhrzeit ermittelt. Für Zeitintervalle des Typs {@link TypeZeitintervall#STUNDE_KOMPLETT}
 * wird der Index der Endeuhrzeit ermittelt.
 *
 * <p>
 * 00:00 - 00:15 = 011001000 ... 00:45 - 01:00 = 011004003 00:00 - 01:00 Stunde = 011004004 ...
 * 02:15 - 03:15 SpStd (Block) 012000000 00:00 - 06:00 Block
 * 013000000 ... 13:45 - 14:45 SpStd (Tag) 060000000 00:00 - 24:00 Gesamt/Tagessumme 070000000
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MesswerteSortingIndexUtil {

    public static final Integer MINUTES_PER_HOUR = 60;

    public static final Integer MINUTES_PER_QUARTER_HOUR = 15;

    /**
     * Summand zur Indexermittlung für {@link Zeitblock#ZB_00_06}.
     */
    public static final int SORTING_INDEX_ZB_00_06 = 10000000;

    /**
     * Summand zur Indexermittlung für {@link Zeitblock#ZB_06_10}.
     */
    public static final int SORTING_INDEX_ZB_06_10 = 20000000;

    /**
     * Summand zur Indexermittlung für {@link Zeitblock#ZB_10_15}.
     */
    public static final int SORTING_INDEX_ZB_10_15 = 30000000;

    /**
     * Summand zur Indexermittlung für {@link Zeitblock#ZB_15_19}.
     */
    public static final int SORTING_INDEX_ZB_15_19 = 40000000;

    /**
     * Summand zur Indexermittlung für {@link Zeitblock#ZB_19_24}.
     */
    public static final int SORTING_INDEX_ZB_19_24 = 50000000;

    /**
     * Der Index für die Spitzenstunde des gesamten Tages für KFZ-Verkehr.
     */
    public static final int SORTING_INDEX_SPITZEN_STUNDE_DAY_KFZ = 60000000;

    /**
     * Der Index für die Spitzenstunde des gesamten Tages für Radverkehr.
     */
    public static final int SORTING_INDEX_SPITZEN_STUNDE_DAY_RAD = 70000000;

    /**
     * Der Index für die Spitzenstunde des gesamten Tages für Fussverkehrs.
     */
    public static final int SORTING_INDEX_SPITZEN_STUNDE_DAY_FUSS = 80000000;

    /**
     * Der Index für die Summe des gesamten Tages.
     */
    public static final int SORTING_INDEX_GESAMT_DAY = 90000000;

    /**
     * Der Index für {@link Zeitblock#ZB_06_19} und {@link Zeitblock#ZB_06_22}.
     */
    public static final int SORTING_INDEX_BLOCK_SPEZIAL = 100000000;

    /**
     * Summand zur Indexermittlung für {@link Zeitintervall}e welche nicht als Blocksumme oder
     * Spitzenstunde innerhalb eines {@link Zeitblock}s dienen.
     */
    public static final int SORTING_INDEX_SECOND_STEP_INTERVALL = 1000000;

    /**
     * Summand zur Indexermittlung für die KFZ-Spitzenstunde innerhalb eines {@link Zeitblock}s.
     */
    public static final int SORTING_INDEX_SECOND_SPITZEN_STUNDE_KFZ = 2000000;

    /**
     * Summand zur Indexermittlung für die Rad-Spitzenstunde innerhalb eines {@link Zeitblock}s.
     */
    public static final int SORTING_INDEX_SECOND_SPITZEN_STUNDE_RAD = 3000000;

    /**
     * Summand zur Indexermittlung für die Fuss-Spitzenstunde innerhalb eines {@link Zeitblock}s.
     */
    public static final int SORTING_INDEX_SECOND_SPITZEN_STUNDE_FUSS = 4000000;

    /**
     * Summand zur Indexermittlung für die Blocksumme innerhalb eines {@link Zeitblock}s.
     */
    public static final int SORTING_INDEX_SECOND_STEP_BLOCK = 5000000;

    public static final int FACTOR_END_TIME = 1000;

    /**
     * Diese Methode ermittelt Sortierindex für einen LadeMesswerteDTO innerhalb eines
     * {@link Zeitblock}.
     *
     * @param ladeMesswerteDto Das LadeMesswerteDTO für den der Index ermittelt werden soll.
     * @return 0 falls der LadeMesswerteDTO nicht in einem {@link Zeitblock} vorkommt, ansonsten der
     *         Indexwert.
     */
    public static int getSortingIndexWithinBlock(final LadeMesswerteDTO ladeMesswerteDto, final TypeZeitintervall type) {
        int sortingIndex = 0;

        if (type.equals(TypeZeitintervall.STUNDE_KOMPLETT)
                || type.equals(TypeZeitintervall.STUNDE_HALB)
                || type.equals(TypeZeitintervall.STUNDE_VIERTEL)
                || type.equals(TypeZeitintervall.BLOCK)) {
            sortingIndex += getFirstStepSortingIndex(ladeMesswerteDto);
            sortingIndex += getSecondStepSortingIndex(type);
            sortingIndex += getThirdAndFourthStepSortingIndex(ladeMesswerteDto, type);
        }
        return sortingIndex;
    }

    /**
     * Diese Methode ermittelt den Summand zur Indexermittlung um den {@link Zeitintervall} dem
     * entsprechenden {@link Zeitblock} zuordnen zu können.
     *
     * @param ladeMesswerteDto Das LadeMesswerteDTO für den der Index ermittelt werden soll.
     * @return 0 falls der LadeMesswerteDTO nicht in einen entsprechenden {@link Zeitblock} verortet
     *         werden kann ansonsten der Indexsummand des Zeitblocks.
     */
    public static int getFirstStepSortingIndex(final LadeMesswerteDTO ladeMesswerteDto) {
        int sortingIndex = 0;
        if (MesswerteBaseUtil.isZeitintervallWithinZeitblock(ladeMesswerteDto, Zeitblock.ZB_00_06)) {
            sortingIndex = SORTING_INDEX_ZB_00_06;
        } else if (MesswerteBaseUtil.isZeitintervallWithinZeitblock(ladeMesswerteDto, Zeitblock.ZB_06_10)) {
            sortingIndex = SORTING_INDEX_ZB_06_10;
        } else if (MesswerteBaseUtil.isZeitintervallWithinZeitblock(ladeMesswerteDto, Zeitblock.ZB_10_15)) {
            sortingIndex = SORTING_INDEX_ZB_10_15;
        } else if (MesswerteBaseUtil.isZeitintervallWithinZeitblock(ladeMesswerteDto, Zeitblock.ZB_15_19)) {
            sortingIndex = SORTING_INDEX_ZB_15_19;
        } else if (MesswerteBaseUtil.isZeitintervallWithinZeitblock(ladeMesswerteDto, Zeitblock.ZB_19_24)) {
            sortingIndex = SORTING_INDEX_ZB_19_24;
        }
        return sortingIndex;
    }

    /**
     * Diese Methode ermittelt den Summand zur Indexermittlung um den {@link Zeitintervall} als
     * Blocksumme oder als eigentlichen Intervall zuordnen zu können.
     *
     * @return 0 falls der LadeMesswerteDTO nicht als Zeitblock oder normaler Intervall interpretiert
     *         werden kann. Ansonsten der entsprechende Indexsummand.
     */
    public static int getSecondStepSortingIndex(final TypeZeitintervall type) {
        int sortingIndex = 0;
        if (type.equals(TypeZeitintervall.STUNDE_KOMPLETT)
                || type.equals(TypeZeitintervall.STUNDE_HALB)
                || type.equals(TypeZeitintervall.STUNDE_VIERTEL)) {
            sortingIndex += SORTING_INDEX_SECOND_STEP_INTERVALL;
        } else if (type.equals(TypeZeitintervall.BLOCK)) {
            sortingIndex += SORTING_INDEX_SECOND_STEP_BLOCK;
        }
        return sortingIndex;
    }

    /**
     * Diese Methode ermittelt den Summand zur Indexermittlung für den {@link Zeitintervall}, damit die
     * Reihenfolge der normalen Intervalle innerhalb eines
     * Blocks erstellt werden kann.
     *
     * @param ladeMesswerteDto Das LadeMesswerteDTO für den der Index ermittelt werden soll.
     * @return 0 falls der LadeMesswerteDTO nicht als normaler Intervall interpretiert werden kann.
     *         Ansonsten der entsprechende Indexsummand.
     */
    public static int getThirdAndFourthStepSortingIndex(final LadeMesswerteDTO ladeMesswerteDto, final TypeZeitintervall type) {
        int sortingIndex = 0;
        if (type.equals(TypeZeitintervall.STUNDE_KOMPLETT)
                || type.equals(TypeZeitintervall.STUNDE_HALB)
                || type.equals(TypeZeitintervall.STUNDE_VIERTEL)) {
            sortingIndex += getQuarterHourIndexForTime(ladeMesswerteDto.getEndeUhrzeit())
                    * FACTOR_END_TIME;
            if (type.equals(TypeZeitintervall.STUNDE_KOMPLETT)) {
                sortingIndex += getQuarterHourIndexForTime(ladeMesswerteDto.getEndeUhrzeit());
            } else {
                sortingIndex += getQuarterHourIndexForTime(ladeMesswerteDto.getStartUhrzeit());
            }
        }
        return sortingIndex;
    }

    public static int getSortingIndexSpitzenStundeWithinBlockKfz() {
        return SORTING_INDEX_SECOND_SPITZEN_STUNDE_KFZ;
    }

    public static int getSortingIndexSpitzenStundeWithinBlockRad() {
        return SORTING_INDEX_SECOND_SPITZEN_STUNDE_RAD;
    }

    public static int getSortingIndexSpitzenStundeWithinBlockFuss() {
        return SORTING_INDEX_SECOND_SPITZEN_STUNDE_FUSS;
    }

    public static int getSortingIndexSpitzenStundeCompleteDayKfz() {
        return SORTING_INDEX_SPITZEN_STUNDE_DAY_KFZ;
    }

    public static int getSortingIndexSpitzenStundeCompleteDayRad() {
        return SORTING_INDEX_SPITZEN_STUNDE_DAY_RAD;
    }

    public static int getSortingIndexSpitzenStundeCompleteDayFuss() {
        return SORTING_INDEX_SPITZEN_STUNDE_DAY_FUSS;
    }

    public static int getSortingIndexGesamtCompleteDay() {
        return SORTING_INDEX_GESAMT_DAY;
    }

    public static int getSortingIndexBlockSpezial() {
        return SORTING_INDEX_BLOCK_SPEZIAL;
    }

    /**
     * Mit dieser Methode wird der Sortierindex für die angefangenen Viertelstunden eines Tages
     * ermittelt.
     *
     * @param time für die der Viertelstundenindex ermittelt werden soll.
     * @return Den Index der angefangenen Viertelstunde eines Tages.
     */
    public static Integer getQuarterHourIndexForTime(final LocalTime time) {
        int minutesPerDay = time.getHour() * MINUTES_PER_HOUR + time.getMinute();
        if (LocalTime.MAX.equals(time) || LocalTime.of(23, 59).equals(time)) {
            minutesPerDay++;
        }
        return minutesPerDay / MINUTES_PER_QUARTER_HOUR;
    }

}
