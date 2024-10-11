package de.muenchen.dave.util.dataimport;

import com.google.common.collect.Streams;
import de.muenchen.dave.domain.Hochrechnung;
import de.muenchen.dave.domain.KIPredictionResult;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatumDTO;
import de.muenchen.dave.domain.enums.TypeZeitintervall;
import de.muenchen.dave.util.DaveConstants;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Diese Klasse generiert die {@link Zeitintervall}e für eine KI-Vorhersage entsprechend den
 * Fahrbeziehungen.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ZeitintervallKIUtil {

    public static final String LIST_LENGTH_MISMATCH = "Mismatch in list size of predictionResults and firstZeitintervalleOfFahrbeziehungen";

    /**
     * Diese Methode erzeugt die zu persistierenden Zeitintervalle (je Fahrbeziehung) für die
     * KI-Tagessummen.
     *
     * @param predictionResults Liste von KIPredictionResults, wobei für jede Fahrbeziehung ein
     *            KIPredictionResult enthalten ist.
     * @param firstZeitintervalleOfFahrbeziehungen List mit je einem importierten Zeitintervall pro
     *            Fahrbeziehung
     * @return Liste der zu perstierenden Zeitintervalle für die KI-Tagessummen
     */
    public static List<Zeitintervall> createKIZeitintervalleFromKIPredictionResults(List<KIPredictionResult> predictionResults,
            List<Zeitintervall> firstZeitintervalleOfFahrbeziehungen) {
        if (predictionResults.size() != firstZeitintervalleOfFahrbeziehungen.size())
            throw new IllegalArgumentException(LIST_LENGTH_MISMATCH);

        return Streams.zip(
                predictionResults.stream(),
                firstZeitintervalleOfFahrbeziehungen.stream(),
                ZeitintervallKIUtil::createKIZeitintervallFromKIPredictionResult).collect(Collectors.toList());
    }

    /**
     * Diese Methode gruppiert eine Liste von Zeitintervallen nach Fahrbeziehung und gibt diese als
     * zweidimensionale Liste von Zeitintervallen zurück.
     *
     * @param zeitintervalle Zu gruppierende Liste von Zeitintervallen
     * @return Über fahrbeziehungId-gruppierte Listen als zweidimensionale Liste
     */
    public static List<List<Zeitintervall>> groupZeitintervalleByFahrbeziehung(List<Zeitintervall> zeitintervalle) {
        final Map<UUID, List<Zeitintervall>> groupedByFahrbeziehungId = zeitintervalle.stream()
                .collect(Collectors.groupingBy(Zeitintervall::getFahrbeziehungId));
        return new ArrayList<>(groupedByFahrbeziehungId.values());
    }

    /**
     * Diese Method extrahiert das erste Zeitintervall aus einer nach fahrbeziehungId-gruppierten Liste
     *
     * @param zeitintervalle nach fahrbeziehungId-gruppierte Listen als zweidimensionale Liste
     * @return Liste von je dem ersten Zeitintervall pro Fahrbeziehung
     */
    public static List<Zeitintervall> extractZeitintervallForEachFahrbeziehung(List<List<Zeitintervall>> zeitintervalle) {
        return zeitintervalle.stream().map(list -> list.get(0)).collect(Collectors.toList());
    }

    /**
     * Erstellt ein zu persistierendes Zeitintervall aus einem KIPredictionResult und einem
     * Zeitintervall.
     *
     * @param predictionResult KIPredictionResult, dass die Tagessummen der Fahrzeugtypen enthält
     * @param zeitintervall Zeitintervall zur Übertragung der ZaehlungID, FahrbeziehungID und
     *            Fahrbeziehung
     * @return Zeitintervall vom Typ TypeZeitintervall.GESAMT_KI, dass die Tageshochrechnungen für
     *         einzelne Fahrzeugtypen sowie -klassen (innerhalb der
     *         Hochrechnung) enthält.
     */
    private static Zeitintervall createKIZeitintervallFromKIPredictionResult(KIPredictionResult predictionResult, Zeitintervall zeitintervall) {
        final Zeitintervall kiZeitintervall = ZeitintervallBaseUtil.createZeitintervallWithoutCountingValues(
                zeitintervall.getZaehlungId(),
                LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.MIDNIGHT),
                LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.MAX),
                TypeZeitintervall.GESAMT_KI,
                zeitintervall.getFahrbeziehung());
        kiZeitintervall.setFahrbeziehungId(zeitintervall.getFahrbeziehungId());

        // Setze KI-Ergebnisse für einzelne Fahrzeugtypen
        kiZeitintervall.setFahrradfahrer(predictionResult.getRadTagessumme());
        kiZeitintervall.setFussgaenger(0);

        // Erstelle LadeZaehldatumDTO zur Summierung für Fahrzeugklassen
        final LadeZaehldatumDTO ladeZaehldatumDTO = new LadeZaehldatumDTO();
        ladeZaehldatumDTO.setPkw(kiZeitintervall.getPkw());
        ladeZaehldatumDTO.setLkw(kiZeitintervall.getLkw());
        ladeZaehldatumDTO.setLastzuege(kiZeitintervall.getLastzuege());
        ladeZaehldatumDTO.setBusse(kiZeitintervall.getBusse());
        ladeZaehldatumDTO.setKraftraeder(kiZeitintervall.getKraftraeder());
        ladeZaehldatumDTO.setFahrradfahrer(kiZeitintervall.getFahrradfahrer());
        ladeZaehldatumDTO.setFussgaenger(kiZeitintervall.getFussgaenger());

        // Erstelle Hochrechung
        final Hochrechnung hochrechnung = new Hochrechnung();
        hochrechnung.setHochrechnungKfz(ladeZaehldatumDTO.getKfz());
        hochrechnung.setHochrechnungSv(ladeZaehldatumDTO.getSchwerverkehr());
        hochrechnung.setHochrechnungGv(ladeZaehldatumDTO.getGueterverkehr());
        hochrechnung.setHochrechnungRad(ladeZaehldatumDTO.getFahrradfahrer());

        kiZeitintervall.setHochrechnung(hochrechnung);
        return kiZeitintervall;
    }

    /**
     * Setzt in den Gesamt-Zeitintervallen die Spalte Hochrechnung Rad mit den KI-Hochrechnugen.
     *
     * @param allZeitintervalle Zeitintervalle (wird noch nach Typ Gesamt gefiltert)
     * @param kiIntervalle Durch die KI hochgerechnete Gesamtsummen für die Fahrbeziehungen
     */
    public static void mergeKiHochrechnungInGesamt(List<Zeitintervall> allZeitintervalle, List<Zeitintervall> kiIntervalle) {
        allZeitintervalle.stream()
                .filter(intervall -> TypeZeitintervall.GESAMT.equals(intervall.getType()))
                .forEach(intervall -> kiIntervalle.stream()
                        .filter(prediction -> prediction.getFahrbeziehung().equals(intervall.getFahrbeziehung()))
                        .findFirst()
                        .ifPresent(prediction -> intervall.getHochrechnung().setHochrechnungRad(prediction.getFahrradfahrer())));
    }

    /**
     * Die KI-berechnet nur A nach B Fahrbeziehungen. Anreicherung um die Fahrbeziehungen A nach alle
     * und alle nach B
     *
     * @param kiIntervalle Zeitintervalle mit den A nach B Fahrbeziehungen
     */
    public static void expandKiHochrechnungen(List<Zeitintervall> kiIntervalle) {
        var neueIntervalle = ZeitintervallFahrbeziehungsSummationUtil.getUeberFahrbeziehungSummierteZeitintervalle(kiIntervalle);
        neueIntervalle.forEach(intervall -> intervall.setType(TypeZeitintervall.GESAMT_KI));
        kiIntervalle.addAll(neueIntervalle);
    }

}
