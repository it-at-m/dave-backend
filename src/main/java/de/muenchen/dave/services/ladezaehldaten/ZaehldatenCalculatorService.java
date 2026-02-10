package de.muenchen.dave.services.ladezaehldaten;

import de.muenchen.dave.domain.Bewegungsbeziehung;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.util.dataimport.ZeitintervallBaseUtil;
import de.muenchen.dave.util.dataimport.ZeitintervallGleitendeSpitzenstundeUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ZaehldatenCalculatorService {
    private List<Zeitintervall> sumOverBewegungsbeziehung(final Map<Bewegungsbeziehung, List<Zeitintervall>> input, final OptionsDTO options) {
        //Ídee: SortingIndex als Key für die Zeitintervalle

        //Die Map wird invertiert: neuer Schlüssl: Sortierungsindex
        // Die Info welche Bewegungsbeziehung sie angehört geht verlohren
        Map<Integer, List<Zeitintervall>> datesPerInterval = new HashMap<>();
        for (Map.Entry<Bewegungsbeziehung, List<Zeitintervall>> entry : input.entrySet()) {
            List<Zeitintervall> timeIntervals = entry.getValue();
            for (Zeitintervall interval : timeIntervals) {
                List<Zeitintervall> datesForGivenInterval = datesPerInterval.get(interval.getSortingIndex());
                datesForGivenInterval.add(interval);
                datesPerInterval.put(interval.getSortingIndex(), datesForGivenInterval);
            }
        }

        //Die Einträge der neuen Map werden summiert und in summedZeitintervals gespeichert

        List<Zeitintervall> summedZeitintervalls = new ArrayList<>();

        for (Map.Entry<Integer, List<Zeitintervall>> entry : datesPerInterval.entrySet()) {
            Zeitintervall addedZeitintervall = new Zeitintervall();
            for (Zeitintervall zeitintervall : entry.getValue()) {
                addedZeitintervall = ZeitintervallBaseUtil.summation(addedZeitintervall, zeitintervall);
            }
            summedZeitintervalls.add(addedZeitintervall);
        }

        //Berechnung der Spitzenstunde abhängig von Zeitervall und Art
        // Zeitblock auswahl fehlt noch

        if (options.getSpitzenstunde()) {

        }

        if (options.getSpitzenstundeRad()) {

        }

        if (options.getSpitzenstundeFuss()) {

        }

        if (options.getSpitzenstundeKfz()) { //Standart

        }

        return summedZeitintervalls;
    }

    private List<Zeitintervall> addSpitzenstundeToSummedAndExtractedZeitintervalle(final List<Zeitintervall> zeitinervalleSummed) {
        //TODO: individualisieren
        return ZeitintervallGleitendeSpitzenstundeUtil.getGleitendeSpitzenstunden(zeitinervalleSummed);
    }
}
