package de.muenchen.dave.services.ladezaehldaten;

import de.muenchen.dave.domain.Bewegungsbeziehung;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.util.dataimport.ZeitintervallBaseUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class ZaehldatenSummationService {

    public List<Zeitintervall> sumOverBewegungsbeziehung(final Map<Bewegungsbeziehung, List<Zeitintervall>> input) {

        //Die Map wird invertiert: neuer Schlüssl: Intervalle
        // Die Datenstruktur wird zur Berechnung der SPitzenstunde gebraucht
        //Die Info welche Bewegungsbeziehung es angehört geht verlohren
        List<Zeitintervall> concatenatedZeitintervall = new ArrayList<>();
        for (Map.Entry<Bewegungsbeziehung, List<Zeitintervall>> entry : input.entrySet()) {
            concatenatedZeitintervall.addAll(entry.getValue());
        }
        final Map<ZeitintervallBaseUtil.Intervall, List<Zeitintervall>> zeitintervalleGroupedByIntervall = ZeitintervallBaseUtil
                .createByIntervallGroupedZeitintervalle(concatenatedZeitintervall);

        //Die Einträge der neuen Map werden summiert und in summedZeitintervals gespeichert

        List<Zeitintervall> summedZeitintervalls = new ArrayList<>();

        for (Map.Entry<ZeitintervallBaseUtil.Intervall, List<Zeitintervall>> entry : zeitintervalleGroupedByIntervall.entrySet()) {
            Zeitintervall addedZeitintervall = new Zeitintervall();
            for (Zeitintervall zeitintervall : entry.getValue()) {
                addedZeitintervall = ZeitintervallBaseUtil.summation(addedZeitintervall, zeitintervall);
            }
            summedZeitintervalls.add(addedZeitintervall);
        }

        return summedZeitintervalls;
    }

}
