package de.muenchen.dave.util.dataimport;

import com.google.common.collect.Streams;
import de.muenchen.dave.domain.Hochrechnung;
import de.muenchen.dave.domain.KIPredictionResult;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatumDTO;
import de.muenchen.dave.domain.enums.TypeZeitintervall;
import de.muenchen.dave.util.DaveConstants;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Diese Klasse generiert die {@link Zeitintervall}e für eine KI-Vorhersage entsprechend den
 * Verkehrsbeziehungen.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ZeitintervallKIUtil {

    public static final String LIST_LENGTH_MISMATCH = "Mismatch in list size of predictionResults and firstZeitintervalleOfVerkehrsbeziehungen";

    /**
     * Diese Methode erzeugt die zu persistierenden Zeitintervalle (je Bewegungsbeziehung) für die
     * KI-Tagessummen.
     *
     * @param predictionResults Liste von KIPredictionResults, wobei für jede Bewegungsbeziehung ein
     *            KIPredictionResult enthalten ist.
     * @param firstZeitintervallOfBewegungsbeziehungen List mit je einem importierten Zeitintervall pro
     *            Bewegungsbeziehung
     * @return Liste der zu perstierenden Zeitintervalle für die KI-Tagessummen
     */
    public static List<Zeitintervall> createKIZeitintervalleForTagessummeFromKIPredictionResults(
            final List<KIPredictionResult> predictionResults,
            final List<Zeitintervall> firstZeitintervallOfBewegungsbeziehungen) {
        if (predictionResults.size() != firstZeitintervallOfBewegungsbeziehungen.size())
            throw new IllegalArgumentException(LIST_LENGTH_MISMATCH);

        return Streams.zip(
                predictionResults.stream(),
                firstZeitintervallOfBewegungsbeziehungen.stream(),
                ZeitintervallKIUtil::createKIZeitintervallFromKIPredictionResult).collect(Collectors.toList());
    }

    /**
     * Diese Methode gruppiert eine Liste von Zeitintervallen nach Bewegungsbeziehungen und gibt diese
     * als
     * zweidimensionale Liste von Zeitintervallen zurück.
     *
     * @param zeitintervalle Zu gruppierende Liste von Zeitintervallen
     * @return Über bewegungsbeziehungId-gruppierte Listen als zweidimensionale Liste
     */
    public static List<List<Zeitintervall>> groupZeitintervalleByBewegungsbeziehung(List<Zeitintervall> zeitintervalle) {
        final Map<UUID, List<Zeitintervall>> groupedByBewegungsbeziehungId = zeitintervalle
                .stream()
                .collect(Collectors.groupingBy(Zeitintervall::getBewegungsbeziehungId));
        return new ArrayList<>(groupedByBewegungsbeziehungId.values());
    }

    /**
     * Diese Method extrahiert das erste Zeitintervall aus einer nach bewegungsbeziehungId-gruppierten
     * Liste
     *
     * @param zeitintervalle nach bewegungsbeziehungId-gruppierte Listen als zweidimensionale Liste
     * @return Liste von je dem ersten Zeitintervall pro Bewegungsbeziehung
     */
    public static List<Zeitintervall> extractFirstZeitintervallForEachBewegungsbeziehung(List<List<Zeitintervall>> zeitintervalle) {
        return zeitintervalle.stream().map(List::getFirst).collect(Collectors.toList());
    }

    /**
     * Erstellt ein zu persistierendes Zeitintervall aus einem KIPredictionResult und einem
     * Zeitintervall.
     *
     * @param predictionResult KIPredictionResult, dass die Tagessummen der Fahrzeugtypen enthält
     * @param zeitintervall Zeitintervall zur Übertragung der ZaehlungID, BewegungsbeziehungID und
     *            Verkehrsbeziehung
     * @return Zeitintervall vom Typ TypeZeitintervall.GESAMT_KI, dass die Tageshochrechnungen für
     *         einzelne Fahrzeugtypen sowie -klassen (innerhalb der
     *         Hochrechnung) enthält.
     */
    private static Zeitintervall createKIZeitintervallFromKIPredictionResult(
            final KIPredictionResult predictionResult,
            final Zeitintervall zeitintervall) {
        final Zeitintervall kiZeitintervall = ZeitintervallBaseUtil.createZeitintervallWithoutCountingValues(
                zeitintervall.getZaehlungId(),
                LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.MIDNIGHT),
                LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.MAX),
                TypeZeitintervall.GESAMT_KI,
                zeitintervall.getVerkehrsbeziehung(),
                zeitintervall.getLaengsverkehr(),
                zeitintervall.getQuerungsverkehr());
        kiZeitintervall.setBewegungsbeziehungId(zeitintervall.getBewegungsbeziehungId());

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
     * @param kiIntervalle Durch die KI hochgerechnete Gesamtsummen für die Verkehrsbeziehungen
     */
    public static void mergeKiHochrechnungInGesamt(List<Zeitintervall> allZeitintervalle, List<Zeitintervall> kiIntervalle) {
        allZeitintervalle.stream()
                .filter(intervall -> TypeZeitintervall.GESAMT.equals(intervall.getType()))
                .forEach(intervall -> kiIntervalle.stream()
                        .filter(prediction -> ZeitintervallBaseUtil.haveBothZeitintervallSameBewegungsbeziehung(prediction, intervall))
                        .findFirst()
                        .ifPresent(prediction -> intervall.getHochrechnung().setHochrechnungRad(prediction.getFahrradfahrer())));
    }

}
