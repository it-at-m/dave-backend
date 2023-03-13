/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.util.dataimport;


import de.muenchen.dave.domain.Fahrbeziehung;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.enums.TypeZeitintervall;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Diese Klasse summiert die {@link Zeitintervall}e einer Zählung
 * entsprechend den Fahrbeziehungen.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ZeitintervallFahrbeziehungsSummationUtil {

    /**
     * Diese Methode erstellt die Grundlegende Datenstruktur um Summierung über Zeitintervalle
     * zu ermöglichen.
     *
     * @param zeitintervalle Die Zeitintervalle aus denen die Datenstruktur erstellt werden soll.
     * @return Datenstruktur als Basis für Summierung.
     */
    private static Map<ZeitintervallBaseUtil.Intervall, List<Zeitintervall>> createDataStructureForSummation(final List<Zeitintervall> zeitintervalle) {
        final List<Zeitintervall> filteredZeitintervalle = zeitintervalle.stream()
                .filter(ZeitintervallFahrbeziehungsSummationUtil::filterValidFahrbeziehung)
                .collect(Collectors.toList());
        return ZeitintervallBaseUtil.createByIntervallGroupedZeitintervalle(filteredZeitintervalle);
    }

    /**
     * Methode prüft auf Basis der im {@link Zeitintervall} hinterlegten Fahrbeziehungen, ob in Methode
     * {@link ZeitintervallFahrbeziehungsSummationUtil#createDataStructureForSummation(List)} eine Filterung
     * durchgeführt werden soll.
     *
     * @param zeitintervall Ein {@link Zeitintervall} der geprüft werden soll.
     * @return true wenn die Fahrbeziehung "von" sowie Fahrbeziehung "nach"
     * bzw. die "fahrbewegungKreisverkehr" nicht "null" ist.
     */
    private static boolean filterValidFahrbeziehung(final Zeitintervall zeitintervall) {
        final Fahrbeziehung fahrbeziehung = zeitintervall.getFahrbeziehung();
        return ObjectUtils.isNotEmpty(fahrbeziehung.getVon())
                && (ObjectUtils.isNotEmpty(fahrbeziehung.getNach())
                || ObjectUtils.isNotEmpty(fahrbeziehung.getFahrbewegungKreisverkehr()));
    }

    /**
     * Summiert die Fahrbeziehungen im Parameter über die {@link Zeitintervall}e.
     * <p>
     * Beispiel:
     * <p>
     * Zeitintervalle je Fahrbeziehung im Parameter:
     * - 00:15-00:30 von 1 nach 2
     * - 00:15-00:30 von 1 nach 3
     * - 00:15-00:30 von 2 nach 1
     * - 00:15-00:30 von 2 nach 3
     * <p>
     * Ergebnis der Methode ist Summe über Fahrbeziehungen:
     * - 00:15-00:30 von 1 nach alle = Summe über 1 nach 2 und 1 nach 3
     * - 00:15-00:30 von 2 nach alle = Summe über  2 nach 1 und 2 nach 3
     * - 00:15-00:30 von alle nach alle  = Summe über 1 nach 2, 1 nach 3, 2 nach 1 sowie 2 nach 3
     *
     * @param zeitintervalle Die Zeitintervalle für bestimmte Fahrbeziehungen
     * @return Die summierten Zeitintervalle.
     */
    public static List<Zeitintervall> getUeberFahrbeziehungSummierteZeitintervalle(final List<Zeitintervall> zeitintervalle) {
        final Map<ZeitintervallBaseUtil.Intervall, List<Zeitintervall>> zeitintervalleGroupedByIntervall =
                createDataStructureForSummation(zeitintervalle);
        final List<Zeitintervall> ueberFahrbeziehungSummierteZeitintervalle =
                getUeberFahrbeziehungSummierteZeitintervalle(zeitintervalleGroupedByIntervall);
        return ueberFahrbeziehungSummierteZeitintervalle;
    }

    private static List<Zeitintervall> getUeberFahrbeziehungSummierteZeitintervalle(final Map<ZeitintervallBaseUtil.Intervall, List<Zeitintervall>> zeitintervalleGroupedByIntervall) {
        final List<Zeitintervall> summierteZeitintervalle = new ArrayList<>();
        zeitintervalleGroupedByIntervall.keySet().forEach(intervall -> {
            final List<Zeitintervall> zeitintervallePerIntervall = zeitintervalleGroupedByIntervall.get(intervall);
            // Alle nach alle
            summierteZeitintervalle.add(
                    getSummedZeitintervallForAllFahrbeziehungen(intervall, zeitintervallePerIntervall)
            );
            // X nach alle
            final Set<Integer> allVonFahrbeziehungen = getAllVonFahrbeziehungen(zeitintervallePerIntervall);
            allVonFahrbeziehungen.forEach(vonFahrbeziehung -> {
                summierteZeitintervalle.add(
                        getSummedZeitintervallForCertainVonFahrbeziehungen(vonFahrbeziehung, intervall, zeitintervallePerIntervall)
                );
            });
            // Alle nach X
            final Set<Integer> allNachFahrbeziehungen = getAllNachFahrbeziehungen(zeitintervallePerIntervall);
            allNachFahrbeziehungen.forEach(nachFahrbeziehung -> {
                summierteZeitintervalle.add(
                        getSummedZeitintervallForCertainNachFahrbeziehungen(nachFahrbeziehung, intervall, zeitintervallePerIntervall)
                );
            });
        });
        summierteZeitintervalle.forEach(zeitintervall ->
                zeitintervall.setSortingIndex(ZeitintervallSortingIndexUtil.getSortingIndexWithinBlock(zeitintervall))
        );
        return summierteZeitintervalle;
    }

    /**
     * Diese Methode summiert die im Parameter übergebene Liste an Zeitintervallen.
     *
     * @param intervall      Der Intervall mit Anfangs und Endzeitpunkt für den Ergebniszeitintervall.
     * @param zeitintervalle Die Liste der Zeitintervalle zum summiern.
     * @return Der Zeitintervall mit den Summen je Fahrzeugklasse.
     */
    private static Zeitintervall getSummedZeitintervallForAllFahrbeziehungen(final ZeitintervallBaseUtil.Intervall intervall,
                                                                             final List<Zeitintervall> zeitintervalle) {
        final Zeitintervall zeitintervall = getSummedZeitintervallOverAllGivenZeitintervalle(intervall, zeitintervalle);
        zeitintervall.getFahrbeziehung().setVon(null);
        zeitintervall.getFahrbeziehung().setNach(null);
        zeitintervall.getFahrbeziehung().setFahrbewegungKreisverkehr(null);
        return zeitintervall;
    }

    /**
     * Diese Methode bildet aus der im Parameter zeitintervalle übergebenen Liste
     * die Summe je Fahrzeugkategorie.
     * Die Summe wird nur für {@link} {@link Zeitintervall}e gebildet, welche
     * die entsprechende "von"-Fahrbeziehung aus dem Parameter vonFahrbeziehung gesetzt haben.
     *
     * @param vonFahrbeziehung Der Fahrbeziehungsparameter für welche die Summierung getätigt werden soll.
     * @param intervall        Der Intervall mit Anfangs und Endzeitpunkt für den Ergebniszeitintervall.
     * @param zeitintervalle   Die Liste der Zeitintervalle zum summieren.
     * @return Der Zeitintervall mit den Summen je Fahrzeugklasse.
     */
    private static Zeitintervall getSummedZeitintervallForCertainVonFahrbeziehungen(final Integer vonFahrbeziehung,
                                                                                    final ZeitintervallBaseUtil.Intervall intervall,
                                                                                    final List<Zeitintervall> zeitintervalle) {
        final List<Zeitintervall> zeitintervalleCertainFahrbeziehung = zeitintervalle.stream()
                .filter(zeitintervall -> zeitintervall.getFahrbeziehung().getVon().equals(vonFahrbeziehung))
                .collect(Collectors.toList());
        final Zeitintervall zeitintervall = getSummedZeitintervallOverAllGivenZeitintervalle(intervall, zeitintervalleCertainFahrbeziehung);
        zeitintervall.getFahrbeziehung().setVon(vonFahrbeziehung);
        zeitintervall.getFahrbeziehung().setNach(null);
        zeitintervall.getFahrbeziehung().setFahrbewegungKreisverkehr(null);
        return zeitintervall;
    }

    /**
     * Diese Methode bildet aus der im Parameter zeitintervalle übergebenen Liste
     * die Summe je Fahrzeugkategorie.
     * Die Summe wird nur für {@link} {@link Zeitintervall}e gebildet, welche
     * die entsprechende "nach"-Fahrbeziehung aus dem Parameter nachFahrbeziehung gesetzt haben.
     *
     * @param nachFahrbeziehung Der Fahrbeziehungsparameter für welche die Summierung getätigt werden soll.
     * @param intervall         Der Intervall mit Anfangs und Endzeitpunkt für den Ergebniszeitintervall.
     * @param zeitintervalle    Die Liste der Zeitintervalle zum summieren.
     * @return Der Zeitintervall mit den Summen je Fahrzeugklasse.
     */
    private static Zeitintervall getSummedZeitintervallForCertainNachFahrbeziehungen(final Integer nachFahrbeziehung,
                                                                                     final ZeitintervallBaseUtil.Intervall intervall,
                                                                                     final List<Zeitintervall> zeitintervalle) {
        final List<Zeitintervall> zeitintervalleCertainFahrbeziehung = zeitintervalle.stream()
                .filter(zeitintervall ->
                        zeitintervall.getFahrbeziehung().getNach().equals(nachFahrbeziehung)
                                && ObjectUtils.isEmpty(zeitintervall.getFahrbeziehung().getFahrbewegungKreisverkehr())
                )
                .collect(Collectors.toList());
        final Zeitintervall zeitintervall = getSummedZeitintervallOverAllGivenZeitintervalle(intervall, zeitintervalleCertainFahrbeziehung);
        zeitintervall.getFahrbeziehung().setVon(null);
        zeitintervall.getFahrbeziehung().setNach(nachFahrbeziehung);
        zeitintervall.getFahrbeziehung().setFahrbewegungKreisverkehr(null);
        return zeitintervall;
    }

    /**
     * Diese Methode summiert die im Parameter übergebene Liste an Zeitintervallen.
     *
     * @param intervall      Der Intervall mit Anfangs und Endzeitpunkt für den Ergebniszeitintervall.
     * @param zeitintervalle Die Liste der Zeitintervalle zum summiern.
     * @return Der Zeitintervall mit den Summen je Fahrzeugklasse.
     */
    private static Zeitintervall getSummedZeitintervallOverAllGivenZeitintervalle(final ZeitintervallBaseUtil.Intervall intervall,
                                                                                  final List<Zeitintervall> zeitintervalle) {
        final Zeitintervall zeitintervallAllFahrbeziehungen = ZeitintervallBaseUtil.createZeitintervallWithoutCountingValues(
                zeitintervalle.get(0).getZaehlungId(),
                intervall.getStartUhrzeit(),
                intervall.getEndeUhrzeit(),
                TypeZeitintervall.STUNDE_VIERTEL);
        return zeitintervalle.stream()
                .reduce(
                        zeitintervallAllFahrbeziehungen,
                        ZeitintervallBaseUtil::summation
                );
    }

    /**
     * Hier werden aus der Liste der übergebenen {@link Zeitintervall}e
     * alle "von"-Fahrbeziehugen extrahiert.
     *
     * @param zeitintervalle Die Zeitintervalle zur Fahrbeziehungsextraktion.
     * @return Alle "von"-Fahrbeziehungen aus den Zeitintervallen.
     */
    private static Set<Integer> getAllVonFahrbeziehungen(final List<Zeitintervall> zeitintervalle) {
        final Set<Integer> allVonFahrbeziehungen = new HashSet<>();
        zeitintervalle.stream()
                .filter(zeitintervall -> ObjectUtils.isNotEmpty(zeitintervall.getFahrbeziehung().getVon()))
                .forEach(zeitintervall -> allVonFahrbeziehungen.add(zeitintervall.getFahrbeziehung().getVon()));
        return allVonFahrbeziehungen;
    }

    /**
     * Hier werden aus der Liste der übergebenen {@link Zeitintervall}e
     * alle "nach"-Fahrbeziehugen extrahiert.
     *
     * @param zeitintervalle Die Zeitintervalle zur Fahrbeziehungsextraktion.
     * @return Alle "nach"-Fahrbeziehungen aus den Zeitintervallen.
     */
    private static Set<Integer> getAllNachFahrbeziehungen(final List<Zeitintervall> zeitintervalle) {
        final Set<Integer> allNachFahrbeziehungen = new HashSet<>();
        zeitintervalle.stream()
                .filter(zeitintervall ->
                        ObjectUtils.isNotEmpty(zeitintervall.getFahrbeziehung().getNach())
                                && ObjectUtils.isEmpty(zeitintervall.getFahrbeziehung().getFahrbewegungKreisverkehr()))
                .forEach(zeitintervall -> allNachFahrbeziehungen.add(zeitintervall.getFahrbeziehung().getNach()));
        return allNachFahrbeziehungen;
    }

}
