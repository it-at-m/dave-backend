package de.muenchen.dave.util.dataimport;

import de.muenchen.dave.domain.Bewegungsbeziehung;
import de.muenchen.dave.domain.Laengsverkehr;
import de.muenchen.dave.domain.Querungsverkehr;
import de.muenchen.dave.domain.Verkehrsbeziehung;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.enums.TypeZeitintervall;
import de.muenchen.dave.domain.enums.Zaehlart;
import de.muenchen.dave.domain.enums.Zeitblock;
import de.muenchen.dave.exceptions.IncorrectZeitauswahlException;
import de.muenchen.dave.services.ladezaehldaten.LadeZaehldatenService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;

/**
 * Diese Klasse ermittelt die gleitende Spitzenstunde je mögliche Ausprägung der Verkehrsbeziehung.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ZeitintervallGleitendeSpitzenstundeUtil {

    /**
     * Mapping: für eine gegebene Zeitblock-Auswahl die Ziel-Zeitblöcke, die berechnet werden sollen
     */
    private static final Map<Zeitblock, List<Zeitblock>> ZEITBLOCK_PROCESS_MAP = createProcessMap();

    /**
     * Diese Methode ermittelt die gleitende Spitzenstunden je möglicher Ausprägung
     * der Bewegungsbeziehung jeweils für KFZ-, Rad- und Fussverkehr.
     * Je möglicher Ausprägung der Verkehrsbeziehung wird die gleitende
     * Spitzenstunde des gegebenen {@link Zeitblock}s ermittelt:
     *
     * @param zaehlungId die Id der Zählung zu welchem die Zeitintervalle gehören
     * @param zeitblock für den die Auswertung vorgenommen werden soll.
     * @param zaehlart die Zählart der Zählung.
     * @param zeitintervalle Die Zeitintervalle auf Basis derer die Spitzenstunden ermittelt werden
     *            sollen.
     * @param types als Zeitintervalltypen der Spitzenstunde welche angefragt wurden.
     * @param zeitintervalle Die Zeitintervalle für welche die gleitende Spitzenstunde je mögliche
     *            Ausprägung der Verkehrsbeziehung ermittelt werden soll.
     * @return die gleitenden Spitzenstunden als List von {@link Zeitintervall}en jeweils für
     *         die angefragten Spitzenstundentypen.
     */
    public static List<Zeitintervall> getGleitendeSpitzenstundenForEachBewegungsbeziehungForZeitblock(
            final UUID zaehlungId,
            final Zeitblock zeitblock,
            final Zaehlart zaehlart,
            final List<Zeitintervall> zeitintervalle,
            final Set<TypeZeitintervall> types) {
        return zeitintervalle
                .stream()
                .collect(Collectors.groupingBy(ZeitintervallBaseUtil::getBewegungbeziehung))
                .entrySet()
                .stream()
                .flatMap(zeitintervalleOfBewegungsbeziehung -> ZeitintervallGleitendeSpitzenstundeUtil
                        .calculateGleitendeSpitzenstunden(
                                zaehlungId,
                                zeitblock,
                                zeitintervalleOfBewegungsbeziehung.getValue(),
                                types)
                        .stream()
                        .peek(zeitintervall -> setBewegungsbeziehungOnZeitintervallAccordingZaehlart(
                                zeitintervall,
                                zeitintervalleOfBewegungsbeziehung.getKey(),
                                zaehlart)))
                .toList();
    }

    /**
     * Diese Methode ermittelt die gleitende Spitzenstunden je möglicher Ausprägung
     * der Bewegungsbeziehung jeweils für KFZ-, Rad- und Fussverkehr.
     * Je möglicher Ausprägung der Verkehrsbeziehung wird die gleitende
     * Spitzenstunde für folgende {@link Zeitblock}e ermittelt:
     * - {@link Zeitblock#ZB_00_06}
     * - {@link Zeitblock#ZB_06_10}
     * - {@link Zeitblock#ZB_10_15}
     * - {@link Zeitblock#ZB_15_19}
     * - {@link Zeitblock#ZB_19_24}
     * - {@link Zeitblock#ZB_00_24}
     *
     * @param zaehlungId die Id der Zählung zu welchem die Zeitintervalle gehören
     * @param zeitblock für den die Auswertung vorgenommen werden soll.
     * @param zaehlart die Zählart der Zählung.
     * @param zeitintervalle Die Zeitintervalle auf Basis derer die Spitzenstunden ermittelt werden
     *            sollen.
     * @param types als Zeitintervalltypen der Spitzenstunde welche angefragt wurden.
     * @return die gleitenden Spitzenstunden als List von {@link Zeitintervall}en jeweils für
     *         die angefragten Spitzenstundentypen.
     */
    public static List<Zeitintervall> getGleitendeSpitzenstundenForEachBewegungsbeziehung(
            final UUID zaehlungId,
            final Zeitblock zeitblock,
            final Zaehlart zaehlart,
            final List<Zeitintervall> zeitintervalle,
            final Set<TypeZeitintervall> types) {
        return zeitintervalle
                .stream()
                .collect(Collectors.groupingBy(ZeitintervallBaseUtil::getBewegungbeziehung))
                .entrySet()
                .stream()
                .flatMap(zeitintervalleOfBewegungsbeziehung -> ZeitintervallGleitendeSpitzenstundeUtil
                        .getGleitendeSpitzenstunden(
                                zaehlungId,
                                zeitblock,
                                zeitintervalleOfBewegungsbeziehung.getValue(),
                                types)
                        .stream()
                        .peek(zeitintervall -> setBewegungsbeziehungOnZeitintervallAccordingZaehlart(
                                zeitintervall,
                                zeitintervalleOfBewegungsbeziehung.getKey(),
                                zaehlart)))
                .toList();
    }

    /**
     * Diese Methode ermittelt die gleitende Spitzenstunden je möglicher Ausprägung der
     * Verkehrsbeziehung jeweils für KFZ-, Rad- oder auch Fussverkehr.
     * <p>
     * Je möglicher Ausprägung der Verkehrsbeziehung wird die gleitende Spitzenstunde für die
     * angegebenen
     * {@link Zeitblock}e ermittelt.
     *
     * @param zaehlungId die Id der Zählung zu welchem die Zeitintervalle gehören
     * @param zeitblock für den die Auswertung vorgenommen werden soll.
     * @param zeitintervalle Die Zeitintervalle auf Basis derer die Spitzenstunden ermittelt werden
     *            sollen.
     * @param types als Zeitintervalltypen der Spitzenstunde welche angefragt wurden.
     * @return die gleitenden Spitzenstunde je {@link Zeitblock} als List von {@link Zeitintervall}en
     *         jeweils für KFZ-, Rad- oder Fussverkehr.
     */
    public static List<Zeitintervall> getGleitendeSpitzenstunden(
            final UUID zaehlungId,
            final Zeitblock zeitblock,
            final List<Zeitintervall> zeitintervalle,
            final Set<TypeZeitintervall> types) {
        if (Objects.isNull(zaehlungId)) {
            return List.of();
        }

        // Ermittlung aller Unter-Zeitblöcke, deren Intervalle für die Berechnung
        // der Spitzenstunde herangezogen werden sollen.
        final List<Zeitblock> zeitbloeckeToProcess = ZEITBLOCK_PROCESS_MAP.getOrDefault(zeitblock, List.of(zeitblock));

        final List<Zeitintervall> gleitendeSpitzenstunden = zeitbloeckeToProcess
                .stream()
                .flatMap(block -> calculateGleitendeSpitzenstunden(zaehlungId, block, zeitintervalle, types).stream())
                .collect(Collectors.toList());
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
    private static List<Zeitintervall> calculateGleitendeSpitzenstunden(
            final UUID zaehlungId,
            final Zeitblock zeitblock,
            final List<Zeitintervall> sortedZeitintervalle,
            final Set<TypeZeitintervall> types) {
        int valueGleitendeSpitzenstundeKfz = 0;
        int valueGleitendeSpitzenstundeRad = 0;
        int valueGleitendeSpitzenstundeFuss = 0;
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

        if (types.contains(TypeZeitintervall.SPITZENSTUNDE_FUSS)) {
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
     * Setzt die passende Bewegungsbeziehung (Querungsverkehr, Längsverkehr oder Verkehrsbeziehung)
     * auf das übergebene {@link Zeitintervall} abhängig von der angegebenen {@link Zaehlart}.
     *
     * @param zeitintervall Das Ziel-{@link Zeitintervall}, auf das die Bewegungsbeziehung gesetzt wird
     *            (muss nicht null sein).
     * @param bewegungsbeziehung Die zu setzende {@link Bewegungsbeziehung} (z. B. Querungsverkehr,
     *            Laengsverkehr oder Verkehrsbeziehung).
     * @param zaehlart Die {@link Zaehlart} welche bestimmt, welche spezifische Bewegungsbeziehung auf
     *            dem Zeitintervall gesetzt wird.
     */
    public static void setBewegungsbeziehungOnZeitintervallAccordingZaehlart(
            final Zeitintervall zeitintervall,
            final Bewegungsbeziehung bewegungsbeziehung,
            final Zaehlart zaehlart) {
        if (Zaehlart.QU.equals(zaehlart)) {
            zeitintervall.setQuerungsverkehr((Querungsverkehr) bewegungsbeziehung);
        } else if (Zaehlart.FJS.equals(zaehlart)) {
            zeitintervall.setLaengsverkehr((Laengsverkehr) bewegungsbeziehung);
        } else {
            zeitintervall.setVerkehrsbeziehung((Verkehrsbeziehung) bewegungsbeziehung);
        }
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
        final var zeitbloeckeForDayIndex = Set.of(
                Zeitblock.ZB_00_24,
                Zeitblock.ZB_06_22,
                Zeitblock.ZB_06_19);
        if (zeitbloeckeForDayIndex.contains(zeitblock)) {
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

    /**
     * @param zeitauswahl welche die Ausprägung
     *            {@link LadeZaehldatenService#ZEITAUSWAHL_SPITZENSTUNDE_KFZ},
     *            {@link LadeZaehldatenService#ZEITAUSWAHL_SPITZENSTUNDE_RAD} oder
     *            {@link LadeZaehldatenService#ZEITAUSWAHL_SPITZENSTUNDE_FUSS} haben
     *            darf.
     * @return den {@link TypeZeitintervall} welcher der Zeitauswahl entspricht.
     * @throws IncorrectZeitauswahlException sobald die Zeitauswahl nicht vom Typ
     *             {@link LadeZaehldatenService#ZEITAUSWAHL_SPITZENSTUNDE_KFZ},
     *             {@link LadeZaehldatenService#ZEITAUSWAHL_SPITZENSTUNDE_RAD} oder
     *             {@link LadeZaehldatenService#ZEITAUSWAHL_SPITZENSTUNDE_FUSS} ist.
     */
    public static TypeZeitintervall getRelevantTypeZeitintervallFromZeitauswahl(final String zeitauswahl) throws IncorrectZeitauswahlException {
        final TypeZeitintervall typeSpitzenstunde;
        if (LadeZaehldatenService.ZEITAUSWAHL_SPITZENSTUNDE_KFZ.equals(zeitauswahl)) {
            typeSpitzenstunde = TypeZeitintervall.SPITZENSTUNDE_KFZ;
        } else if (LadeZaehldatenService.ZEITAUSWAHL_SPITZENSTUNDE_RAD.equals(zeitauswahl)) {
            typeSpitzenstunde = TypeZeitintervall.SPITZENSTUNDE_RAD;
        } else if (LadeZaehldatenService.ZEITAUSWAHL_SPITZENSTUNDE_FUSS.equals(zeitauswahl)) {
            typeSpitzenstunde = TypeZeitintervall.SPITZENSTUNDE_FUSS;
        } else {
            throw new IncorrectZeitauswahlException();
        }
        return typeSpitzenstunde;
    }

    /**
     * Erstellt für alle Zeitblöcke eine Liste mit darin enthaltenen Zeitblöcken
     * (z.B. ZB_06_19 beinhaltet ZB_06_10, ZB_10_15, ZB_15_19 und ZB_06_19).
     */
    private static Map<Zeitblock, List<Zeitblock>> createProcessMap() {
        return Map.of(
                // Einzelne Blöcke -> beinhalten nur sich selbst
                Zeitblock.ZB_00_06, List.of(Zeitblock.ZB_00_06),
                Zeitblock.ZB_06_10, List.of(Zeitblock.ZB_06_10),
                Zeitblock.ZB_10_15, List.of(Zeitblock.ZB_10_15),
                Zeitblock.ZB_15_19, List.of(Zeitblock.ZB_15_19),
                Zeitblock.ZB_19_22, List.of(Zeitblock.ZB_19_22),
                Zeitblock.ZB_19_24, List.of(Zeitblock.ZB_19_24),

                // Zeitblöcke für 13h
                Zeitblock.ZB_06_19, List.of(
                        Zeitblock.ZB_06_10,
                        Zeitblock.ZB_10_15,
                        Zeitblock.ZB_15_19,
                        Zeitblock.ZB_06_19),

                // Zeitblöcke für 16h
                Zeitblock.ZB_06_22, List.of(
                        Zeitblock.ZB_06_10,
                        Zeitblock.ZB_10_15,
                        Zeitblock.ZB_15_19,
                        Zeitblock.ZB_19_22,
                        Zeitblock.ZB_06_22),

                // Zeitblöcke für 24h
                Zeitblock.ZB_00_24, List.of(
                        Zeitblock.ZB_00_06,
                        Zeitblock.ZB_06_10,
                        Zeitblock.ZB_10_15,
                        Zeitblock.ZB_15_19,
                        Zeitblock.ZB_19_24,
                        Zeitblock.ZB_00_24));
    }
}
