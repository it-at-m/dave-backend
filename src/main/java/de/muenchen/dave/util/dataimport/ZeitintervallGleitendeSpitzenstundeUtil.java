package de.muenchen.dave.util.dataimport;

import de.muenchen.dave.domain.Fahrbeziehung;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.enums.Zeitblock;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.ObjectUtils;

/**
 * Diese Klasse ermittelt die gleitende Spitzenstunde je mögliche Ausprägung der Verkehrsbeziehung.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ZeitintervallGleitendeSpitzenstundeUtil {

    /**
     * Diese Methode ermittelt die gleitende Spitzenstunden je möglicher Ausprägung der Verkehrsbeziehung
     * jeweils für KFZ-, Rad- und Fussverkehr. Je möglicher
     * Ausprägung der Verkehrsbeziehung wird die gleitende Spitzenstunde für folgende {@link Zeitblock}e
     * ermittelt:
     * - {@link Zeitblock#ZB_00_06}
     * - {@link Zeitblock#ZB_06_10}
     * - {@link Zeitblock#ZB_10_15}
     * - {@link Zeitblock#ZB_15_19}
     * - {@link Zeitblock#ZB_19_24}
     * - {@link Zeitblock#ZB_00_24}
     *
     * @param zeitintervalle Die Zeitintervalle für welche die gleitende Spitzenstunde je mögliche
     *            Ausprägung der Verkehrsbeziehung ermittelt werden soll.
     * @return die gleitenden Spitzenstunden als List von {@link Zeitintervall}en jeweils für KFZ-, Rad-
     *         und Fussverkehr.
     */
    public static List<Zeitintervall> getGleitendeSpitzenstunden(final List<Zeitintervall> zeitintervalle) {
        final Map<ZeitintervallBaseUtil.Intervall, List<Zeitintervall>> zeitintervalleGroupedByIntervall = ZeitintervallBaseUtil
                .createByIntervallGroupedZeitintervalle(zeitintervalle);
        final Set<Fahrbeziehung> possibleFahrbeziehungen = ZeitintervallBaseUtil.getAllPossibleFahrbeziehungen(zeitintervalle);
        final List<Zeitintervall> gleitendeSpitzenstunden = new ArrayList<>();
        possibleFahrbeziehungen.forEach(
                fahrbeziehung -> gleitendeSpitzenstunden.addAll(getGleitendeSpitzenstundenForFahrbeziehung(fahrbeziehung, zeitintervalleGroupedByIntervall)));
        return gleitendeSpitzenstunden;
    }

    /**
     * Diese Methode ermittelt die gleitende Spitzenstunden je möglicher Ausprägung der Verkehrsbeziehung
     * jeweils für KFZ-, Rad- und Fussverkehr. Je möglicher
     * Ausprägung der Verkehrsbeziehung wird die gleitende Spitzenstunde für folgende {@link Zeitblock}e
     * ermittelt:
     * - {@link Zeitblock#ZB_00_06}
     * - {@link Zeitblock#ZB_06_10}
     * - {@link Zeitblock#ZB_10_15}
     * - {@link Zeitblock#ZB_15_19}
     * - {@link Zeitblock#ZB_19_24}
     * - {@link Zeitblock#ZB_00_24}
     *
     * @param fahrbeziehung Die {@link Fahrbeziehung} für welche die gleitende Spitzenstunde je
     *            {@link Zeitblock} ermittelt werden soll.
     * @param zeitintervalleGroupedByIntervall Die Zeitintervalle gruppiert nach den einzelnen
     *            Intervallen.
     * @return die gleitenden Spitzenstunde je {@link Zeitblock} als List von {@link Zeitintervall}en
     *         jeweils für KFZ-, Rad- und Fussverkehr.
     */
    private static List<Zeitintervall> getGleitendeSpitzenstundenForFahrbeziehung(final Fahrbeziehung fahrbeziehung,
            final Map<ZeitintervallBaseUtil.Intervall, List<Zeitintervall>> zeitintervalleGroupedByIntervall) {
        final List<Zeitintervall> zeitintervalleForFahrbeziehung = ZeitintervallBaseUtil.getZeitintervalleForFahrbeziehung(fahrbeziehung,
                zeitintervalleGroupedByIntervall);
        final Optional<UUID> zaehlungId = zeitintervalleForFahrbeziehung.stream()
                .map(Zeitintervall::getZaehlungId)
                .findFirst();
        List<Zeitintervall> gleitendeSpitzenstunden = new ArrayList<>();
        if (zaehlungId.isPresent()) {
            berechneGleitendeSpitzenstunde(zaehlungId.get(), Zeitblock.ZB_00_06, fahrbeziehung, zeitintervalleForFahrbeziehung)
                    .setGleitendeSpstdKfzRadFussToSpitzenstundeList(gleitendeSpitzenstunden);
            berechneGleitendeSpitzenstunde(zaehlungId.get(), Zeitblock.ZB_06_10, fahrbeziehung, zeitintervalleForFahrbeziehung)
                    .setGleitendeSpstdKfzRadFussToSpitzenstundeList(gleitendeSpitzenstunden);
            berechneGleitendeSpitzenstunde(zaehlungId.get(), Zeitblock.ZB_10_15, fahrbeziehung, zeitintervalleForFahrbeziehung)
                    .setGleitendeSpstdKfzRadFussToSpitzenstundeList(gleitendeSpitzenstunden);
            berechneGleitendeSpitzenstunde(zaehlungId.get(), Zeitblock.ZB_15_19, fahrbeziehung, zeitintervalleForFahrbeziehung)
                    .setGleitendeSpstdKfzRadFussToSpitzenstundeList(gleitendeSpitzenstunden);
            berechneGleitendeSpitzenstunde(zaehlungId.get(), Zeitblock.ZB_19_24, fahrbeziehung, zeitintervalleForFahrbeziehung)
                    .setGleitendeSpstdKfzRadFussToSpitzenstundeList(gleitendeSpitzenstunden);
            berechneGleitendeSpitzenstunde(zaehlungId.get(), Zeitblock.ZB_00_24, fahrbeziehung, zeitintervalleForFahrbeziehung)
                    .setGleitendeSpstdKfzRadFussToSpitzenstundeList(gleitendeSpitzenstunden);
        }
        return gleitendeSpitzenstunden;
    }

    /**
     * Diese Methode ermittelt die gleitend Spitzenstunde.
     *
     * @param zaehlungId Die ID der Zaehlung.
     * @param zeitblock Der {@link Zeitblock} für welchen die gleitende Spitzenstunde ermittelt werden
     *            soll.
     * @param fahrbeziehung Die im Rückgabewert der Methode gesetzte Verkehrsbeziehung.
     * @param sortedZeitintervalle Die aufsteigend sortierten {@link Zeitintervall}e einer
     *            {@link Fahrbeziehung}.
     * @return Die gleitende Spitzenstunde als Zeitintervall jeweils für den KFZ-, Rad- und Fussverkehr.
     */
    private static GleitendeSpstdZeitintervallKfzRadFuss berechneGleitendeSpitzenstunde(final UUID zaehlungId,
            final Zeitblock zeitblock,
            final Fahrbeziehung fahrbeziehung,
            final List<Zeitintervall> sortedZeitintervalle) {
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
                Integer sum = ObjectUtils.defaultIfNull(gleitenderZeitintervall.getSumKfz(), 0);
                if (valueGleitendeSpitzenstundeKfz < sum) {
                    valueGleitendeSpitzenstundeKfz = sum;
                    gleitendeSpitzenstundeKfz = Optional.of(gleitenderZeitintervall.getSummedZeitintervallKfz());
                }
                // Ermittlung Rad
                sum = ObjectUtils.defaultIfNull(gleitenderZeitintervall.getSumFahrradfahrer(), 0);
                if (valueGleitendeSpitzenstundeRad < sum) {
                    valueGleitendeSpitzenstundeRad = sum;
                    gleitendeSpitzenstundeRad = Optional.of(gleitenderZeitintervall.getSummedZeitintervallRad());
                }
                // Ermittlung Fuss
                sum = ObjectUtils.defaultIfNull(gleitenderZeitintervall.getSumFussgaenger(), 0);
                if (valueGleitendeSpitzenstundeFuss < sum) {
                    valueGleitendeSpitzenstundeFuss = sum;
                    gleitendeSpitzenstundeFuss = Optional.of(gleitenderZeitintervall.getSummedZeitintervallFuss());
                }
            }
        }
        // Finalisierung Kfz
        gleitendeSpitzenstundeKfz.ifPresent(zeitintervall -> {
            zeitintervall.setZaehlungId(zaehlungId);
            zeitintervall.setFahrbeziehung(fahrbeziehung);
            zeitintervall.setSortingIndex(getSortingIndexKfz(zeitintervall, zeitblock));
        });
        // Finalisierung Rad
        gleitendeSpitzenstundeRad.ifPresent(zeitintervall -> {
            zeitintervall.setZaehlungId(zaehlungId);
            zeitintervall.setFahrbeziehung(fahrbeziehung);
            zeitintervall.setSortingIndex(getSortingIndexRad(zeitintervall, zeitblock));
        });
        // Finalisierung Fuss
        gleitendeSpitzenstundeFuss.ifPresent(zeitintervall -> {
            zeitintervall.setZaehlungId(zaehlungId);
            zeitintervall.setFahrbeziehung(fahrbeziehung);
            zeitintervall.setSortingIndex(getSortingIndexFuss(zeitintervall, zeitblock));
        });
        return new GleitendeSpstdZeitintervallKfzRadFuss(
                gleitendeSpitzenstundeKfz,
                gleitendeSpitzenstundeRad,
                gleitendeSpitzenstundeFuss);
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

    @AllArgsConstructor
    @Getter
    @Setter
    @EqualsAndHashCode
    @ToString
    public static class GleitendeSpstdZeitintervallKfzRadFuss {

        private final Optional<Zeitintervall> gleitendeSpitzenstundeKfz;

        private final Optional<Zeitintervall> gleitendeSpitzenstundeRad;

        private final Optional<Zeitintervall> gleitendeSpitzenstundeFuss;

        public void setGleitendeSpstdKfzRadFussToSpitzenstundeList(final List<Zeitintervall> gleitendeSpitzenstunden) {
            gleitendeSpitzenstundeKfz.ifPresent(gleitendeSpitzenstunden::add);
            gleitendeSpitzenstundeRad.ifPresent(gleitendeSpitzenstunden::add);
            gleitendeSpitzenstundeFuss.ifPresent(gleitendeSpitzenstunden::add);
        }

    }

}
