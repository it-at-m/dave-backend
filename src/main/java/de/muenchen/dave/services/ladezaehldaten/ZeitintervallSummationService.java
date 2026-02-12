package de.muenchen.dave.services.ladezaehldaten;

import de.muenchen.dave.domain.Bewegungsbeziehung;
import de.muenchen.dave.domain.Hochrechnung;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.util.dataimport.ZeitintervallBaseUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;

@Service
public class ZeitintervallSummationService {

    public List<Zeitintervall> sumZeitintervelleOverBewegungsbeziehung(final Map<Bewegungsbeziehung, List<Zeitintervall>> zeitintervalleByBewegungsbeziehung) {

        //Die Map wird invertiert: neuer Schlüssl: Intervalle
        // Die Datenstruktur wird zur Berechnung der SPitzenstunde gebraucht
        //Die Info welche Bewegungsbeziehung es angehört geht verlohren
        List<Zeitintervall> concatenatedZeitintervall = zeitintervalleByBewegungsbeziehung
                .values()
                .stream()
                .flatMap(zeitintervalleOfBewegungsbeziehung -> zeitintervalleOfBewegungsbeziehung.stream())
                .toList();
        final Map<ZeitintervallBaseUtil.Intervall, List<Zeitintervall>> zeitintervalleGroupedByIntervall = ZeitintervallBaseUtil
                .createByIntervallGroupedZeitintervalle(concatenatedZeitintervall);

        //Die Einträge der neuen Map werden summiert und in summedZeitintervals gespeichert

        List<Zeitintervall> summedZeitintervalls = new ArrayList<>();

        for (Map.Entry<ZeitintervallBaseUtil.Intervall, List<Zeitintervall>> entry : zeitintervalleGroupedByIntervall.entrySet()) {
            Zeitintervall addedZeitintervall = new Zeitintervall();
            for (Zeitintervall zeitintervall : entry.getValue()) {
                addedZeitintervall = nullSafeSummationForHochrechnung(addedZeitintervall, zeitintervall);
            }
            summedZeitintervalls.add(addedZeitintervall);
        }

        return summedZeitintervalls;
    }

    protected Zeitintervall nullSafeSummationForHochrechnung(final Zeitintervall zeitintervall1, final Zeitintervall zeitintervall2) {
        if (Objects.isNull(zeitintervall1.getHochrechnung())) {
            zeitintervall1.setHochrechnung(new Hochrechnung());
        }
        if (Objects.isNull(zeitintervall2.getHochrechnung())) {
            zeitintervall2.setHochrechnung(new Hochrechnung());
        }
        return ZeitintervallBaseUtil.summation(zeitintervall1, zeitintervall2);
    }

}
