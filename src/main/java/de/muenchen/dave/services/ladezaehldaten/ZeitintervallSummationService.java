package de.muenchen.dave.services.ladezaehldaten;

import de.muenchen.dave.domain.Bewegungsbeziehung;
import de.muenchen.dave.domain.Hochrechnung;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.util.dataimport.ZeitintervallBaseUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
        //final Map<ZeitintervallBaseUtil.Intervall, List<Zeitintervall>> zeitintervalleGroupedByIntervall = ZeitintervallBaseUtil
        //        .createByIntervallGroupedZeitintervalle(concatenatedZeitintervall);

        final Map<Integer, List<Zeitintervall>> zeitintervalleGroupedBySortingIndex = concatenatedZeitintervall
                .stream()
                .collect(Collectors.groupingByConcurrent(Zeitintervall::getSortingIndex));

        //Die Einträge der neuen Map werden summiert und in summedZeitintervals gespeichert

        List<Zeitintervall> summedZeitintervalls = new ArrayList<>();

        for (Map.Entry<Integer, List<Zeitintervall>> entry : zeitintervalleGroupedBySortingIndex.entrySet()) {
            Zeitintervall addedZeitintervall = new Zeitintervall();
            for (Zeitintervall zeitintervall : entry.getValue()) {
                if (Objects.isNull(addedZeitintervall.getType())){
                    addedZeitintervall.setType(zeitintervall.getType());
                }
                if (Objects.isNull(addedZeitintervall.getStartUhrzeit())){
                    addedZeitintervall.setStartUhrzeit(zeitintervall.getStartUhrzeit());
                }
                if (Objects.isNull(addedZeitintervall.getEndeUhrzeit())){
                    addedZeitintervall.setEndeUhrzeit(zeitintervall.getEndeUhrzeit());
                }
                addedZeitintervall = nullSafeSummationForHochrechnung(addedZeitintervall, zeitintervall);
            }
            addedZeitintervall.setSortingIndex(entry.getKey());
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
