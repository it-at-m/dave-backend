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
import de.muenchen.dave.util.dataimport.ZeitintervallGleitendeSpitzenstundeUtil;
import de.muenchen.dave.util.dataimport.ZeitintervallKIUtil;
import de.muenchen.dave.util.dataimport.ZeitintervallSortingIndexUtil;
import de.muenchen.dave.util.dataimport.ZeitintervallVerkehrsbeziehungsSummationUtil;
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
     * Verkehrsbeziehungspermutationen summiert.
     * - Für die über Verkehrsbeziehungspermutationen summierten und auch im Parameter übergebene
     * Zeitintervalle werden die gleitenden Spitzenstunden ermittelt.
     * - Für die über Verkehrsbeziehungspermutationen summierten und auch im Parameter übergebene
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

        /*
         * - Die im Parameter übergebenen Zeitintervalle werden je Intervall über
         * alle möglichen Verkehrsbeziehungspermutationen summiert.
         */
        final List<Zeitintervall> summierteVerkehrsbeziehungen = ZeitintervallVerkehrsbeziehungsSummationUtil
                .getUeberVerkehrsbeziehungSummierteZeitintervalle(zeitintervalle);

        List<Zeitintervall> allPossibleVerkehrsbeziehungen = new ArrayList<>();
        allPossibleVerkehrsbeziehungen.addAll(zeitintervalle);
        allPossibleVerkehrsbeziehungen.addAll(summierteVerkehrsbeziehungen);

        /*
         * - Für die über Verkehrsbeziehungspermutationen summierten und auch im Parameter übergebene
         * Zeitintervalle
         * werden die gleitenden Spitzenstunden ermittelt.
         */
        final List<Zeitintervall> gleitendeSpitzenstunden = ZeitintervallGleitendeSpitzenstundeUtil.getGleitendeSpitzenstunden(allPossibleVerkehrsbeziehungen);

        /*
         * - Für die über Verkehrsbeziehungspermutationen summierten und auch im Parameter übergebene
         * Zeitintervalle
         * werden die Summen für die einzelnen {@link Zeitblock}e gebildet.
         */
        final List<Zeitintervall> summierteZeitbloecke = ZeitintervallZeitblockSummationUtil.getSummen(allPossibleVerkehrsbeziehungen);

        /*
         * Für die im Parameter übergebenen Zeitintervalle werden die KI-Tagessummen ermittelt, wenn der
         * boolean-Parameter true ist
         */
        final List<Zeitintervall> kiZeitintervalle = new ArrayList<>();
        if (kiAufbereitung) {
            final List<List<Zeitintervall>> groupedZeitintervalleByVerkehrsbeziehung = ZeitintervallKIUtil
                    .groupZeitintervalleByVerkehrsbeziehung(zeitintervalle);
            try {
                final KIPredictionResult[] predictionResults = kiService
                        .predictHochrechnungTageswerteForZeitIntervalleOfZaehlung(groupedZeitintervalleByVerkehrsbeziehung);
                final List<Zeitintervall> zeitintervallForEachVerkehrsbeziehung = ZeitintervallKIUtil
                        .extractZeitintervallForEachVerkehrsbeziehung(groupedZeitintervalleByVerkehrsbeziehung);
                kiZeitintervalle.addAll(
                        ZeitintervallKIUtil.createKIZeitintervalleFromKIPredictionResults(Arrays.asList(predictionResults),
                                zeitintervallForEachVerkehrsbeziehung));
                ZeitintervallKIUtil.expandKiHochrechnungen(kiZeitintervalle);
                ZeitintervallKIUtil.mergeKiHochrechnungInGesamt(summierteZeitbloecke, kiZeitintervalle);
            } catch (PredictionFailedException exception) {
                log.error("Error predicting Tagessummen with KIService\n" + exception);
            }
        }

        List<Zeitintervall> allZeitintervalle = new ArrayList<>();
        allZeitintervalle.addAll(allPossibleVerkehrsbeziehungen);
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
    public void deleteZeitintervalleByIdOfVerkehrsbeziehungQuerverkehrOrLaengsverkehr(final List<String> verkehrsbeziehungIds) {
        final var uuidsOfVerkehrsbeziehungen = CollectionUtils
                .emptyIfNull(verkehrsbeziehungIds)
                .stream()
                .map(UUID::fromString)
                .toList();
        zeitintervallRepository.deleteByBewegungsbeziehungIdIn(uuidsOfVerkehrsbeziehungen);
    }

    @Transactional
    public void deleteZeitintervalleForCorrection(final String zaehlungId) {
        zeitintervallRepository.deleteAllByZaehlungId(UUID.fromString(zaehlungId));
        zeitintervallRepository.flush();
    }

}
