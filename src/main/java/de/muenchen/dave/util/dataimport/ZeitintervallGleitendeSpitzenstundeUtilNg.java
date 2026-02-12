package de.muenchen.dave.util.dataimport;

import de.muenchen.dave.domain.Verkehrsbeziehung;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.enums.TypeZeitintervall;
import de.muenchen.dave.domain.enums.Zeitblock;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;

/**
 * Diese Klasse ermittelt die gleitende Spitzenstunde je mögliche Ausprägung der Verkehrsbeziehung.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ZeitintervallGleitendeSpitzenstundeUtilNg {

    /**
     * Diese Methode ermittelt die gleitende Spitzenstunden je möglicher Ausprägung der
     * Verkehrsbeziehung jeweils für KFZ-, Rad- oder auch Fussverkehr.
     *
     * Je möglicher Ausprägung der Verkehrsbeziehung wird die gleitende Spitzenstunde für folgende
     * {@link Zeitblock}e ermittelt:
     * - {@link Zeitblock#ZB_00_06}
     * - {@link Zeitblock#ZB_06_10}
     * - {@link Zeitblock#ZB_10_15}
     * - {@link Zeitblock#ZB_15_19}
     * - {@link Zeitblock#ZB_19_24}
     * - {@link Zeitblock#ZB_00_24}
     *
     * @param zaehlungId die Id der Zählung zu welchem die Zeitintervalle gehören
     * @param zeitintervalle Die Zeitintervalle auf Basis derer die Spitzenstunden ermittelt werden
     *            sollen.
     * @param types als Zeitintervalltypen welche angefragt wurden.
     * @return die gleitenden Spitzenstunde je {@link Zeitblock} als List von {@link Zeitintervall}en
     *         jeweils für KFZ-, Rad- oder Fussverkehr.
     */
    public static List<Zeitintervall> getGleitendeSpitzenstunden(
            final UUID zaehlungId,
            final List<Zeitintervall> zeitintervalle,
            final Set<TypeZeitintervall> types) {
        List<Zeitintervall> gleitendeSpitzenstunden = new ArrayList<>();
        if (Objects.nonNull(zaehlungId)) {
            var calculatedSpitzenstunden = berechneGleitendeSpitzenstunden(zaehlungId, Zeitblock.ZB_00_06, zeitintervalle, types);
            gleitendeSpitzenstunden.addAll(calculatedSpitzenstunden);
            calculatedSpitzenstunden = berechneGleitendeSpitzenstunden(zaehlungId, Zeitblock.ZB_06_10, zeitintervalle, types);
            gleitendeSpitzenstunden.addAll(calculatedSpitzenstunden);
            calculatedSpitzenstunden = berechneGleitendeSpitzenstunden(zaehlungId, Zeitblock.ZB_10_15, zeitintervalle, types);
            gleitendeSpitzenstunden.addAll(calculatedSpitzenstunden);
            calculatedSpitzenstunden = berechneGleitendeSpitzenstunden(zaehlungId, Zeitblock.ZB_15_19, zeitintervalle, types);
            gleitendeSpitzenstunden.addAll(calculatedSpitzenstunden);
            calculatedSpitzenstunden = berechneGleitendeSpitzenstunden(zaehlungId, Zeitblock.ZB_19_24, zeitintervalle, types);
            gleitendeSpitzenstunden.addAll(calculatedSpitzenstunden);
            calculatedSpitzenstunden = berechneGleitendeSpitzenstunden(zaehlungId, Zeitblock.ZB_00_24, zeitintervalle, types);
            gleitendeSpitzenstunden.addAll(calculatedSpitzenstunden);
        }
        return gleitendeSpitzenstunden;
    }

    /**
     * Diese Methode ermittelt die gleitende Spitzenstunde für den gegebenen {@link Zeitblock}.
     *
     * @param zaehlungId Die ID der Zaehlung.
     * @param zeitblock Der {@link Zeitblock} für welchen die gleitende Spitzenstunde ermittelt werden
     *            soll.
     * @param sortedZeitintervalle Die aufsteigend sortierten {@link Zeitintervall}e einer
     *            {@link Verkehrsbeziehung}.
     * @param types als Zeitintervalltypen welche angefragt wurden.
     * @return Die gleitende Spitzenstunde als Zeitintervall jeweils für den KFZ-, Rad- und Fussverkehr
     *         falls diese im Parameter types vorhanden sind.
     */
    private static List<Zeitintervall> berechneGleitendeSpitzenstunden(
            final UUID zaehlungId,
            final Zeitblock zeitblock,
            final List<Zeitintervall> sortedZeitintervalle,
            final Set<TypeZeitintervall> types) {
        Integer valueGleitendeSpitzenstundeKfz = 0;
        Integer valueGleitendeSpitzenstundeRad = 0;
        Integer valueGleitendeSpitzenstundeFuss = 0;
        Optional<Zeitintervall> gleitendeSpitzenstundeKfz = Optional.empty();
        Optional<Zeitintervall> gleitendeSpitzenstundeRad = Optional.empty();
        Optional<Zeitintervall> gleitendeSpitzenstundeFuss = Optional.empty();
        GleitenderZeitintervall gleitenderZeitintervall;
        for (int index = 0; index < sortedZeitintervalle.size(); index++) {
            if (ZeitintervallBaseUtil.isZeitintervallWithinZeitblock(sortedZeitintervalle.get(index), zeitblock)) {
                gleitenderZeitintervall = GleitenderZeitintervall.createInstanceWithIndexParameterAsNewestIndex(sortedZeitintervalle, index, zeitblock);
                // Ermittlung Kfz
                Integer sum = ObjectUtils.getIfNull(gleitenderZeitintervall.getSumKfz(), 0);
                if (valueGleitendeSpitzenstundeKfz < sum) {
                    valueGleitendeSpitzenstundeKfz = sum;
                    gleitendeSpitzenstundeKfz = Optional.of(gleitenderZeitintervall.getSummedZeitintervallKfz());
                }
                // Ermittlung Rad
                sum = ObjectUtils.getIfNull(gleitenderZeitintervall.getSumFahrradfahrer(), 0);
                if (valueGleitendeSpitzenstundeRad < sum) {
                    valueGleitendeSpitzenstundeRad = sum;
                    gleitendeSpitzenstundeRad = Optional.of(gleitenderZeitintervall.getSummedZeitintervallRad());
                }
                // Ermittlung Fuss
                sum = ObjectUtils.getIfNull(gleitenderZeitintervall.getSumFussgaenger(), 0);
                if (valueGleitendeSpitzenstundeFuss < sum) {
                    valueGleitendeSpitzenstundeFuss = sum;
                    gleitendeSpitzenstundeFuss = Optional.of(gleitenderZeitintervall.getSummedZeitintervallFuss());
                }
            }
        }

        final var calculatedSpitzenstunden = new ArrayList<Zeitintervall>();

        if (types.contains(TypeZeitintervall.SPITZENSTUNDE_KFZ)) {
            // Finalisierung Kfz
            gleitendeSpitzenstundeKfz.ifPresent(zeitintervall -> {
                zeitintervall.setZaehlungId(zaehlungId);
                zeitintervall.setSortingIndex(getSortingIndexKfz(zeitintervall, zeitblock));
                calculatedSpitzenstunden.add(zeitintervall);
            });
        }

        if (types.contains(TypeZeitintervall.SPITZENSTUNDE_RAD)) {
            // Finalisierung Rad
            gleitendeSpitzenstundeRad.ifPresent(zeitintervall -> {
                zeitintervall.setZaehlungId(zaehlungId);
                zeitintervall.setSortingIndex(getSortingIndexRad(zeitintervall, zeitblock));
                calculatedSpitzenstunden.add(zeitintervall);
            });
        }

        if (types.contains(TypeZeitintervall.SPITZENSTUNDE_RAD)) {
            // Finalisierung Fuss
            gleitendeSpitzenstundeFuss.ifPresent(zeitintervall -> {
                zeitintervall.setZaehlungId(zaehlungId);
                zeitintervall.setSortingIndex(getSortingIndexFuss(zeitintervall, zeitblock));
                calculatedSpitzenstunden.add(zeitintervall);
            });
        }

        return calculatedSpitzenstunden;
    }

    /**
     * Ermittlung des {@link Zeitintervall}#getSortingIndex() für KFZ-Verkehr.
     *
     * @param zeitintervall Der {@link Zeitintervall} für welchen der Index ermittelt werden soll.
     * @param zeitblock Der für die Indexermittlung relevante Zeitblock.
     * @return Der Index für den {@link Zeitintervall} der gleitenden Spitzenstunde für KFZ-Verkehr.
     */
    public static int getSortingIndexKfz(final Zeitintervall zeitintervall, final Zeitblock zeitblock) {
        int sortingIndex;
        if (zeitblock.equals(Zeitblock.ZB_00_24)) {
            sortingIndex = ZeitintervallSortingIndexUtil.getSortingIndexSpitzenStundeCompleteDayKfz();
        } else {
            sortingIndex = ZeitintervallSortingIndexUtil.getFirstStepSortingIndex(zeitintervall);
            sortingIndex += ZeitintervallSortingIndexUtil.getSortingIndexSpitzenStundeWithinBlockKfz();
        }
        return sortingIndex;
    }

    /**
     * Ermittlung des {@link Zeitintervall}#getSortingIndex() für Radverkehr.
     *
     * @param zeitintervall Der {@link Zeitintervall} für welchen der Index ermittelt werden soll.
     * @param zeitblock Der für die Indexermittlung relevante Zeitblock.
     * @return Der Index für den {@link Zeitintervall} der gleitenden Spitzenstunde für Radverkehr.
     */
    public static int getSortingIndexRad(final Zeitintervall zeitintervall, final Zeitblock zeitblock) {
        int sortingIndex;
        if (zeitblock.equals(Zeitblock.ZB_00_24)) {
            sortingIndex = ZeitintervallSortingIndexUtil.getSortingIndexSpitzenStundeCompleteDayRad();
        } else {
            sortingIndex = ZeitintervallSortingIndexUtil.getFirstStepSortingIndex(zeitintervall);
            sortingIndex += ZeitintervallSortingIndexUtil.getSortingIndexSpitzenStundeWithinBlockRad();
        }
        return sortingIndex;
    }

    /**
     * Ermittlung des {@link Zeitintervall}#getSortingIndex() für Fussverkehr.
     *
     * @param zeitintervall Der {@link Zeitintervall} für welchen der Index ermittelt werden soll.
     * @param zeitblock Der für die Indexermittlung relevante Zeitblock.
     * @return Der Index für den {@link Zeitintervall} der gleitenden Spitzenstunde für Fussverkehr.
     */
    public static int getSortingIndexFuss(final Zeitintervall zeitintervall, final Zeitblock zeitblock) {
        int sortingIndex;
        if (zeitblock.equals(Zeitblock.ZB_00_24)) {
            sortingIndex = ZeitintervallSortingIndexUtil.getSortingIndexSpitzenStundeCompleteDayFuss();
        } else {
            sortingIndex = ZeitintervallSortingIndexUtil.getFirstStepSortingIndex(zeitintervall);
            sortingIndex += ZeitintervallSortingIndexUtil.getSortingIndexSpitzenStundeWithinBlockFuss();
        }
        return sortingIndex;
    }

}
