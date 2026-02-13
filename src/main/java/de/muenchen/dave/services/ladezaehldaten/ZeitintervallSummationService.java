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

    /**
     * Die Methode liefert die summierten Zeitintervalle über alle übergebenen Bewegungsbeziehungen
     * z.B. um 1 nach alle zu erhalten werden die Zeitintervalllisten für 1 nach 2, 1 nach 3, 1 nach 4, ... übergeben
     * @param zeitintervalleByBewegungsbeziehung Liste an Zeitintervallen pro Bewegungsbeziehung
     * @return Liste mit addierten Zeitintervallen (z.B. entspricht 1 nach alle)
     */
    public List<Zeitintervall> sumZeitintervelleOverBewegungsbeziehung(final Map<Bewegungsbeziehung, List<Zeitintervall>> zeitintervalleByBewegungsbeziehung) {

        final Map<Integer, List<Zeitintervall>> zeitintervalleGroupedBySortingIndex = invertMap(zeitintervalleByBewegungsbeziehung);

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
                if (Objects.isNull(addedZeitintervall.getZaehlungId())){
                    addedZeitintervall.setZaehlungId(zeitintervall.getZaehlungId());
                }
                addedZeitintervall = nullSafeSummationForHochrechnung(addedZeitintervall, zeitintervall);
            }
            addedZeitintervall.setSortingIndex(entry.getKey());
            summedZeitintervalls.add(addedZeitintervall);
        }

        return summedZeitintervalls;
    }

    /**
     * Falls keine Hochrechnung in den Daten hinterlegt ist, wird eine leere angefügt
     * @param zeitintervall1 zu addierender Zeitintervall
     * @param zeitintervall2 zu addierender Zeitintervall
     * @return Summe der Intervalle
     */
    protected Zeitintervall nullSafeSummationForHochrechnung(final Zeitintervall zeitintervall1, final Zeitintervall zeitintervall2) {
        if (Objects.isNull(zeitintervall1.getHochrechnung())) {
            zeitintervall1.setHochrechnung(new Hochrechnung());
        }
        if (Objects.isNull(zeitintervall2.getHochrechnung())) {
            zeitintervall2.setHochrechnung(new Hochrechnung());
        }
        return ZeitintervallBaseUtil.summation(zeitintervall1, zeitintervall2);
    }

    /**
     * Invertiert die übergebene Map, um eine effektivere Weiterberechnung zu ermöglichen
     * Der neue Schlüssel ist der Sortierungsindex,
     * dieser identifiziert eine Viertelstunde/Blocksumme/Spitzenstunde eindeutig
     * Die Bewegungsbeziehung geht dabei verlohren
     * @param map Liste an Zeitintervallen je Bewegungsbeziehung
     * @return Liste an Zeitintervallen je Viertelstunde/Blocksumme/Spitzenstunde
     */
    protected Map<Integer, List<Zeitintervall>> invertMap (Map<Bewegungsbeziehung, List<Zeitintervall>> map){
        List<Zeitintervall> concatenatedZeitintervall = map
                .values()
                .stream()
                .flatMap(zeitintervalleOfBewegungsbeziehung -> zeitintervalleOfBewegungsbeziehung.stream())
                .toList();

        return concatenatedZeitintervall
                .stream()
                .collect(Collectors.groupingByConcurrent(Zeitintervall::getSortingIndex));
    }

}
