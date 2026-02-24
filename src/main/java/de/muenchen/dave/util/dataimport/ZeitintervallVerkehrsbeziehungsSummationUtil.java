package de.muenchen.dave.util.dataimport;

import de.muenchen.dave.domain.Verkehrsbeziehung;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.enums.TypeZeitintervall;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;

/**
 * Diese Klasse summiert die {@link Zeitintervall}e einer Zählung entsprechend den
 * Verkehrsbeziehungen.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ZeitintervallVerkehrsbeziehungsSummationUtil {

    /**
     * Diese Methode erstellt die Grundlegende Datenstruktur um Summierung über Zeitintervalle zu
     * ermöglichen.
     *
     * @param zeitintervalle Die Zeitintervalle aus denen die Datenstruktur erstellt werden soll.
     * @return Datenstruktur als Basis für Summierung.
     */
    private static Map<ZeitintervallBaseUtil.Intervall, List<Zeitintervall>> createDataStructureForSummation(final List<Zeitintervall> zeitintervalle) {
        final List<Zeitintervall> filteredZeitintervalle = zeitintervalle.stream()
                .filter(ZeitintervallVerkehrsbeziehungsSummationUtil::filterValidVerkehrsbeziehung)
                .collect(Collectors.toList());
        return ZeitintervallBaseUtil.createByIntervallGroupedZeitintervalle(filteredZeitintervalle);
    }

    /**
     * Methode prüft auf Basis der im {@link Zeitintervall} hinterlegten Verkehrsbeziehung, ob in
     * Methode
     * {@link ZeitintervallVerkehrsbeziehungsSummationUtil#createDataStructureForSummation(List)} eine
     * Filterung durchgeführt werden soll.
     *
     * @param zeitintervall Ein {@link Zeitintervall} der geprüft werden soll.
     * @return true wenn die Verkehrsbeziehung "von" sowie Verkehrsbeziehung "nach" bzw. die
     *         "fahrbewegungKreisverkehr" nicht "null" ist.
     */
    private static boolean filterValidVerkehrsbeziehung(final Zeitintervall zeitintervall) {
        final Verkehrsbeziehung verkehrsbeziehung = zeitintervall.getVerkehrsbeziehung();
        return ObjectUtils.isNotEmpty(verkehrsbeziehung.getVon())
                && (ObjectUtils.isNotEmpty(verkehrsbeziehung.getNach())
                        || ObjectUtils.isNotEmpty(verkehrsbeziehung.getFahrbewegungKreisverkehr()));
    }

    /**
     * Summiert die Verkehrsbeziehungen im Parameter über die {@link Zeitintervall}e.
     * <p>
     * Beispiel:
     * <p>
     * Zeitintervalle je Verkehrsbeziehung im Parameter:
     * - 00:15-00:30 von 1 nach 2
     * - 00:15-00:30 von 1 nach 3
     * - 00:15-00:30 von 2 nach 1
     * - 00:15-00:30 von 2 nach 3
     * <p>
     * Ergebnis der Methode ist Summe über Verkehrsbeziehungen:
     * - 00:15-00:30 von 1 nach alle = Summe über 1 nach 2 und 1 nach 3
     * - 00:15-00:30 von 2 nach alle = Summe über 2 nach 1 und 2 nach 3
     * - 00:15-00:30 von alle nach alle = Summe über 1 nach 2, 1 nach 3, 2 nach 1 sowie 2 nach 3
     *
     * @param zeitintervalle Die Zeitintervalle für bestimmte Verkehrsbeziehungen
     * @return Die summierten Zeitintervalle.
     */
    public static List<Zeitintervall> getUeberVerkehrsbeziehungSummierteZeitintervalle(final List<Zeitintervall> zeitintervalle) {
        final Map<ZeitintervallBaseUtil.Intervall, List<Zeitintervall>> zeitintervalleGroupedByIntervall = createDataStructureForSummation(zeitintervalle);
        final List<Zeitintervall> ueberVerkehrsbeziehungSummierteZeitintervalle = getUeberVerkehrsbeziehungSummierteZeitintervalle(
                zeitintervalleGroupedByIntervall);
        return ueberVerkehrsbeziehungSummierteZeitintervalle;
    }

    private static List<Zeitintervall> getUeberVerkehrsbeziehungSummierteZeitintervalle(
            final Map<ZeitintervallBaseUtil.Intervall, List<Zeitintervall>> zeitintervalleGroupedByIntervall) {
        final List<Zeitintervall> summierteZeitintervalle = new ArrayList<>();
        zeitintervalleGroupedByIntervall.keySet().forEach(intervall -> {
            final List<Zeitintervall> zeitintervallePerIntervall = zeitintervalleGroupedByIntervall.get(intervall);
            // Alle nach alle
            summierteZeitintervalle.add(
                    getSummedZeitintervallForAllVerkehrsbeziehungen(intervall, zeitintervallePerIntervall));
            // X nach alle
            final Set<Integer> allVonVerkehrsbeziehungen = getAllVonVerkehrsbeziehungen(zeitintervallePerIntervall);
            allVonVerkehrsbeziehungen.forEach(vonVerkehrsbeziehung -> {
                summierteZeitintervalle.add(
                        getSummedZeitintervallForCertainVonVerkehrsbeziehungen(vonVerkehrsbeziehung, intervall, zeitintervallePerIntervall));
            });
            // Alle nach X
            final Set<Integer> allNachVerkehrsbeziehungen = getAllNachVerkehrsbeziehungen(zeitintervallePerIntervall);
            allNachVerkehrsbeziehungen.forEach(nachVerkehrsbeziehung -> {
                summierteZeitintervalle.add(
                        getSummedZeitintervallForCertainNachVerkehrsbeziehungen(nachVerkehrsbeziehung, intervall, zeitintervallePerIntervall));
            });
        });
        summierteZeitintervalle
                .forEach(zeitintervall -> zeitintervall.setSortingIndex(ZeitintervallSortingIndexUtil.getSortingIndexWithinBlock(zeitintervall)));
        return summierteZeitintervalle;
    }

    /**
     * Diese Methode summiert die im Parameter übergebene Liste an Zeitintervallen.
     *
     * @param intervall Der Intervall mit Anfangs und Endzeitpunkt für den Ergebniszeitintervall.
     * @param zeitintervalle Die Liste der Zeitintervalle zum summiern.
     * @return Der Zeitintervall mit den Summen je Fahrzeugklasse.
     */
    private static Zeitintervall getSummedZeitintervallForAllVerkehrsbeziehungen(
            final ZeitintervallBaseUtil.Intervall intervall,
            final List<Zeitintervall> zeitintervalle) {
        final Zeitintervall zeitintervall = getSummedZeitintervallOverAllGivenZeitintervalle(intervall, zeitintervalle);
        zeitintervall.getVerkehrsbeziehung().setVon(null);
        zeitintervall.getVerkehrsbeziehung().setNach(null);
        zeitintervall.getVerkehrsbeziehung().setFahrbewegungKreisverkehr(null);
        return zeitintervall;
    }

    /**
     * Diese Methode bildet aus der im Parameter zeitintervalle übergebenen Liste die Summe je
     * Fahrzeugkategorie. Die Summe wird nur für {@link}
     * {@link Zeitintervall}e gebildet, welche die entsprechende "von"-Verkehrsbeziehung aus dem
     * Parameter vonVerkehrsbeziehung gesetzt haben.
     *
     * @param vonVerkehrsbeziehung Der Verkehrsbeziehungsparameter für welche die Summierung getätigt
     *            werden
     *            soll.
     * @param intervall Der Intervall mit Anfangs und Endzeitpunkt für den Ergebniszeitintervall.
     * @param zeitintervalle Die Liste der Zeitintervalle zum summieren.
     * @return Der Zeitintervall mit den Summen je Fahrzeugklasse.
     */
    private static Zeitintervall getSummedZeitintervallForCertainVonVerkehrsbeziehungen(
            final Integer vonVerkehrsbeziehung,
            final ZeitintervallBaseUtil.Intervall intervall,
            final List<Zeitintervall> zeitintervalle) {
        final List<Zeitintervall> zeitintervalleCertainVerkehrsbeziehung = zeitintervalle.stream()
                .filter(zeitintervall -> zeitintervall.getVerkehrsbeziehung().getVon().equals(vonVerkehrsbeziehung))
                .collect(Collectors.toList());
        final Zeitintervall zeitintervall = getSummedZeitintervallOverAllGivenZeitintervalle(intervall, zeitintervalleCertainVerkehrsbeziehung);
        zeitintervall.getVerkehrsbeziehung().setVon(vonVerkehrsbeziehung);
        zeitintervall.getVerkehrsbeziehung().setNach(null);
        zeitintervall.getVerkehrsbeziehung().setFahrbewegungKreisverkehr(null);
        return zeitintervall;
    }

    /**
     * Diese Methode bildet aus der im Parameter zeitintervalle übergebenen Liste die Summe je
     * Fahrzeugkategorie. Die Summe wird nur für {@link}
     * {@link Zeitintervall}e gebildet, welche die entsprechende "nach"-Verkehrsbeziehung aus dem
     * Parameter
     * nachVerkehrsbeziehung gesetzt haben.
     *
     * @param nachVerkehrsbeziehung Der Verkehrsbeziehungsparameter für welche die Summierung getätigt
     *            werden
     *            soll.
     * @param intervall Der Intervall mit Anfangs und Endzeitpunkt für den Ergebniszeitintervall.
     * @param zeitintervalle Die Liste der Zeitintervalle zum summieren.
     * @return Der Zeitintervall mit den Summen je Fahrzeugklasse.
     */
    private static Zeitintervall getSummedZeitintervallForCertainNachVerkehrsbeziehungen(
            final Integer nachVerkehrsbeziehung,
            final ZeitintervallBaseUtil.Intervall intervall,
            final List<Zeitintervall> zeitintervalle) {
        final List<Zeitintervall> zeitintervalleCertainVerkehrsbeziehung = zeitintervalle.stream()
                .filter(zeitintervall -> zeitintervall.getVerkehrsbeziehung().getNach().equals(nachVerkehrsbeziehung)
                        && ObjectUtils.isEmpty(zeitintervall.getVerkehrsbeziehung().getFahrbewegungKreisverkehr()))
                .collect(Collectors.toList());
        final Zeitintervall zeitintervall = getSummedZeitintervallOverAllGivenZeitintervalle(intervall, zeitintervalleCertainVerkehrsbeziehung);
        zeitintervall.getVerkehrsbeziehung().setVon(null);
        zeitintervall.getVerkehrsbeziehung().setNach(nachVerkehrsbeziehung);
        zeitintervall.getVerkehrsbeziehung().setFahrbewegungKreisverkehr(null);
        return zeitintervall;
    }

    /**
     * Diese Methode summiert die im Parameter übergebene Liste an Zeitintervallen.
     *
     * @param intervall Der Intervall mit Anfangs und Endzeitpunkt für den Ergebniszeitintervall.
     * @param zeitintervalle Die Liste der Zeitintervalle zum summiern.
     * @return Der Zeitintervall mit den Summen je Fahrzeugklasse.
     */
    private static Zeitintervall getSummedZeitintervallOverAllGivenZeitintervalle(final ZeitintervallBaseUtil.Intervall intervall,
            final List<Zeitintervall> zeitintervalle) {
        final Zeitintervall zeitintervallAllVerkehrsbeziehungen = ZeitintervallBaseUtil.createZeitintervallWithoutCountingValues(
                zeitintervalle.get(0).getZaehlungId(),
                intervall.getStartUhrzeit(),
                intervall.getEndeUhrzeit(),
                TypeZeitintervall.STUNDE_VIERTEL);
        return zeitintervalle.stream()
                .reduce(
                        zeitintervallAllVerkehrsbeziehungen,
                        ZeitintervallBaseUtil::summation);
    }

    /**
     * Hier werden aus der Liste der übergebenen {@link Zeitintervall}e alle "von"-Verkehrsbeziehungen
     * extrahiert.
     *
     * @param zeitintervalle Die Zeitintervalle zur Verkehrsbeziehungsextraktion.
     * @return Alle "von"-Verkehrsbeziehungen aus den Zeitintervallen.
     */
    private static Set<Integer> getAllVonVerkehrsbeziehungen(final List<Zeitintervall> zeitintervalle) {
        final Set<Integer> allVonVerkehrsbeziehungen = new HashSet<>();
        zeitintervalle.stream()
                .filter(zeitintervall -> ObjectUtils.isNotEmpty(zeitintervall.getVerkehrsbeziehung().getVon()))
                .forEach(zeitintervall -> allVonVerkehrsbeziehungen.add(zeitintervall.getVerkehrsbeziehung().getVon()));
        return allVonVerkehrsbeziehungen;
    }

    /**
     * Hier werden aus der Liste der übergebenen {@link Zeitintervall}e alle "nach"-Verkehrsbeziehungen
     * extrahiert.
     *
     * @param zeitintervalle Die Zeitintervalle zur Verkehrsbeziehungsextraktion.
     * @return Alle "nach"-Verkehrsbeziehungen aus den Zeitintervallen.
     */
    private static Set<Integer> getAllNachVerkehrsbeziehungen(final List<Zeitintervall> zeitintervalle) {
        final Set<Integer> allNachVerkehrsbeziehungen = new HashSet<>();
        zeitintervalle.stream()
                .filter(zeitintervall -> ObjectUtils.isNotEmpty(zeitintervall.getVerkehrsbeziehung().getNach())
                        && ObjectUtils.isEmpty(zeitintervall.getVerkehrsbeziehung().getFahrbewegungKreisverkehr()))
                .forEach(zeitintervall -> allNachVerkehrsbeziehungen.add(zeitintervall.getVerkehrsbeziehung().getNach()));
        return allNachVerkehrsbeziehungen;
    }

}
