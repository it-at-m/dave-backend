package de.muenchen.dave.services.persist;

import de.muenchen.dave.domain.KIPredictionResult;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.enums.TypeZeitintervall;
import de.muenchen.dave.domain.enums.Zaehldauer;
import de.muenchen.dave.domain.enums.Zeitblock;
import de.muenchen.dave.exceptions.PlausibilityException;
import de.muenchen.dave.exceptions.PredictionFailedException;
import de.muenchen.dave.repositories.relationaldb.ZeitintervallRepository;
import de.muenchen.dave.services.KIService;
import de.muenchen.dave.util.dataimport.ZeitintervallBaseUtil;
import de.muenchen.dave.util.dataimport.ZeitintervallFahrbeziehungsSummationUtil;
import de.muenchen.dave.util.dataimport.ZeitintervallGleitendeSpitzenstundeUtil;
import de.muenchen.dave.util.dataimport.ZeitintervallKIUtil;
import de.muenchen.dave.util.dataimport.ZeitintervallSortingIndexUtil;
import de.muenchen.dave.util.dataimport.ZeitintervallZeitblockSummationUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class ZeitintervallPersistierungsService {

    private final ZeitintervallRepository zeitintervallRepository;

    private final KIService kiService;

    public ZeitintervallPersistierungsService(final ZeitintervallRepository zeitintervallRepository, final KIService kiService) {
        this.zeitintervallRepository = zeitintervallRepository;
        this.kiService = kiService;
    }

    public boolean deleteZaehlung(final String zaehlungId) {
        final UUID zaehlungIdAsUUID = UUID.fromString(zaehlungId);
        zeitintervallRepository.deleteAllByZaehlungId(zaehlungIdAsUUID);
        zeitintervallRepository.flush();
        return !zeitintervallRepository.existsByZaehlungId(zaehlungIdAsUUID);
    }

    /**
     * Die Methode führt vor der Persistierung der Zeitintervalle in der Datenbank eine Datenaufbreitung
     * durch. D.h.:
     * <p>
     * - Die im Parameter übergebenen Zeitintervalle werden daraufhin überprüft, ob der letzte
     * Zeitintervall des Tages die korrekte Endeuhrzeit von 23:59
     * aufweist.
     * - Die im Parameter übergebenen Zeitintervalle werden mit einem Index für die Sortierung bei der
     * Datenextraktion versehen.
     * - Die im Parameter übergebenen Zeitintervalle werden mit dem Merkmal
     * {@link TypeZeitintervall#STUNDE_VIERTEL} versehen.
     * - Die im Parameter übergebenen Zeitintervalle werden je Intervall über alle möglichen
     * Fahrbeziehungspermutationen summiert.
     * - Für die über Fahrbeziehungspermutationen summierten und auch im Parameter übergebene
     * Zeitintervalle werden die gleitenden Spitzenstunden ermittelt.
     * - Für die über Fahrbeziehungspermutationen summierten und auch im Parameter übergebene
     * Zeitintervalle werden die Summen für die einzelnen
     * {@link Zeitblock}e gebildet.
     *
     * @param zeitintervalle Die {@link Zeitintervall}e zur vorherigen Aufbereitung vor der eigentlichen
     *            Persistierung.
     * @param kiAufbereitung KI Aufbereitung ausführen (Nur für 2x4h Zählungen)
     */
    public void aufbereitenUndPersistieren(final List<Zeitintervall> zeitintervalle, final boolean kiAufbereitung) {

        /*
         * - Die im Parameter übergebenen Zeitintervalle werden überprüft,
         * ob der letzte Zeitintervall des Tages die korrekte Endeuhrzeit von 23:59 aufweist.
         * - Die im Parameter übergebenen Zeitintervalle werden mit einem Index für
         * die Sortierung bei der Datenextraktion versehen.
         * - Die im Parameter übergebenen Zeitintervalle werden mit
         * dem Merkmal {@link TypeZeitintervall#STUNDE_VIERTEL} versehen.
         */
        zeitintervalle.forEach(zeitintervall -> {
            ZeitintervallBaseUtil.checkAndCorrectEndeuhrzeitForLastZeitintervallOfDayIfNecessary(zeitintervall);
            zeitintervall.setType(TypeZeitintervall.STUNDE_VIERTEL);
            zeitintervall.setSortingIndex(ZeitintervallSortingIndexUtil.getSortingIndexWithinBlock(zeitintervall));
        });

        /**
         * @TODO
         *
         * Entfernen der Funktionen zum summieren der Fahrbeziehungen.
         *
         * Auslagern in die Datenextraktion und Anpassung der Summation.
         */
        /*
         * - Die im Parameter übergebenen Zeitintervalle werden je Intervall über
         * alle möglichen Fahrbeziehungspermutationen summiert.
         */
        final List<Zeitintervall> summierteFahrbeziehungen = ZeitintervallFahrbeziehungsSummationUtil
                .getUeberFahrbeziehungSummierteZeitintervalle(zeitintervalle);

        List<Zeitintervall> allPossibleFahrbeziehungen = new ArrayList<>();
        allPossibleFahrbeziehungen.addAll(zeitintervalle);
        allPossibleFahrbeziehungen.addAll(summierteFahrbeziehungen);

        /**
         * @TODO
         *
         * Entfernen der Funktionen zur Ermittlung der gleitenden Spitzenstunden
         *
         * Auslagern der Ermittlung der gleitenden Spitzenstunde in die Datenextraktion.
         */
        /*
         * - Für die über Fahrbeziehungspermutationen summierten und auch im Parameter übergebene
         * Zeitintervalle
         * werden die gleitenden Spitzenstunden ermittelt.
         */
        final List<Zeitintervall> gleitendeSpitzenstunden = ZeitintervallGleitendeSpitzenstundeUtil.getGleitendeSpitzenstunden(allPossibleFahrbeziehungen);

        /**
         * @TODO
         *
         * Anpassung in der nachfolgende aufgerufenen Summierungsmethode sind erforderlich.
         */
        /*
         * - Für die über Fahrbeziehungspermutationen summierten und auch im Parameter übergebene
         * Zeitintervalle
         * werden die Summen für die einzelnen {@link Zeitblock}e gebildet.
         */
        final List<Zeitintervall> summierteZeitbloecke = ZeitintervallZeitblockSummationUtil.getSummen(allPossibleFahrbeziehungen);

        /**
         * @TODO
         *
         * Code bleibt bestehen.
         */
        /*
         * Für die im Parameter übergebenen Zeitintervalle werden die KI-Tagessummen ermittelt, wenn der
         * boolean-Parameter true ist
         */
        final List<Zeitintervall> kiZeitintervalle = new ArrayList<>();
        if (kiAufbereitung) {
            final List<List<Zeitintervall>> groupedZeitintervalleByFahrbeziehung = ZeitintervallKIUtil.groupZeitintervalleByFahrbeziehung(zeitintervalle);
            try {
                final KIPredictionResult[] predictionResults = kiService
                        .predictHochrechnungTageswerteForZeitIntervalleOfZaehlung(groupedZeitintervalleByFahrbeziehung);
                final List<Zeitintervall> zeitintervallForEachFahrbeziehung = ZeitintervallKIUtil
                        .extractZeitintervallForEachFahrbeziehung(groupedZeitintervalleByFahrbeziehung);
                kiZeitintervalle.addAll(
                        ZeitintervallKIUtil.createKIZeitintervalleFromKIPredictionResults(Arrays.asList(predictionResults), zeitintervallForEachFahrbeziehung));
                ZeitintervallKIUtil.expandKiHochrechnungen(kiZeitintervalle);
                ZeitintervallKIUtil.mergeKiHochrechnungInGesamt(summierteZeitbloecke, kiZeitintervalle);
            } catch (PredictionFailedException exception) {
                log.error("Error predicting Tagessummen with KIService\n" + exception);
            }
        }

        List<Zeitintervall> allZeitintervalle = new ArrayList<>();
        allZeitintervalle.addAll(allPossibleFahrbeziehungen);
        allZeitintervalle.addAll(gleitendeSpitzenstunden);
        allZeitintervalle.addAll(summierteZeitbloecke);
        allZeitintervalle.addAll(kiZeitintervalle);

        persistZeitintervalle(allZeitintervalle);
    }

    /**
     * Persistierung der Aufbereiteten {@link Zeitintervall}e in der relationalen Datenbank.
     *
     * @param toPersist zu speichernde Zeitintervalle
     */
    public void persistZeitintervalle(final List<Zeitintervall> toPersist) {
        log.debug("persistZeitintervalle");
        zeitintervallRepository.saveAllAndFlush(toPersist);
    }

    @Transactional
    public void checkZeitintervalleIfPlausible(final Zaehlung zaehlung, final int numberOfIntervalle) throws PlausibilityException {
        final List<Zeitintervall> zeitintervalle = zeitintervallRepository.findByZaehlungId(UUID.fromString(zaehlung.getId()),
                Sort.by(Sort.Direction.ASC, "startUhrzeit"));
        // überprüfen, ob alle Zeitintervalle vorhanden sind
        if (numberOfIntervalle == 0 || numberOfIntervalle == zeitintervalle.size()) {
            aufbereitenUndPersistieren(zeitintervalle, List.of(Zaehldauer.DAUER_2_X_4_STUNDEN, Zaehldauer.DAUER_13_STUNDEN, Zaehldauer.DAUER_16_STUNDEN)
                    .contains(Zaehldauer.valueOf(zaehlung.getZaehldauer())));
        } else {
            throw new PlausibilityException("Die Anzahl der übermittelten Zeitintervalle stimmt nicht mit den erwarteten überein");
        }
    }

    @Transactional
    public void deleteZeitintervalleByFahrbeziehungId(final List<String> fahrbeziehungIds) {
        final var uuidsOfFahrbeziehungen = CollectionUtils
                .emptyIfNull(fahrbeziehungIds)
                .stream()
                .map(UUID::fromString)
                .toList();
        zeitintervallRepository.deleteByFahrbeziehungIdIn(uuidsOfFahrbeziehungen);
    }

    @Transactional
    public void deleteZeitintervalleForCorrection(final String zaehlungId) {
        zeitintervallRepository.deleteAllByZaehlungId(UUID.fromString(zaehlungId));
        zeitintervallRepository.flush();
    }

}
