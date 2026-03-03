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
        final var byTimeAndTypeAndSortingIndexAdaptedZeitintervalle = zeitintervalle
                .stream()
                .peek(zeitintervall -> {
                    ZeitintervallBaseUtil.checkAndCorrectEndeuhrzeitForLastZeitintervallOfDayIfNecessary(zeitintervall);
                    zeitintervall.setType(TypeZeitintervall.STUNDE_VIERTEL);
                    zeitintervall.setSortingIndex(ZeitintervallSortingIndexUtil.getSortingIndexWithinBlock(zeitintervall));
                })
                .toList();

        /*
         * - Bildung der Summen für die einzelnen {@link Zeitblock}e für die übergebenen Zeitintervalle.
         */
        // TODO: Anpassen zu Bewegungsbeziehungen in Methode.
        final var summierteZeitbloecke = ZeitintervallZeitblockSummationUtil
                .getSummen(byTimeAndTypeAndSortingIndexAdaptedZeitintervalle);

        /*
         * Für die im Parameter übergebenen Zeitintervalle werden die KI-Tagessummen ermittelt,
         * wenn der boolean-Parameter true ist
         */
        // TODO: Anpassen zu Bewegungsbeziehungen in Methode.
        final var kiZeitintervalle = new ArrayList<Zeitintervall>();
        if (kiAufbereitung) {
            final List<List<Zeitintervall>> groupedZeitintervalleByBewegungsbeziehung = ZeitintervallKIUtil
                    .groupZeitintervalleByBewegungsbeziehung(zeitintervalle);
            try {
                final KIPredictionResult[] predictionResults = kiService
                        .predictHochrechnungTageswerteForZeitIntervalleOfZaehlung(groupedZeitintervalleByBewegungsbeziehung);
                final List<Zeitintervall> firstZeitintervallForEachBewegungsbeziehung = ZeitintervallKIUtil
                        .extractFirstZeitintervallForEachBewegungsbeziehung(groupedZeitintervalleByBewegungsbeziehung);
                final List<Zeitintervall> kiZeitintervalleForTagessumme = ZeitintervallKIUtil
                        .createKIZeitintervalleForTagessummeFromKIPredictionResults(
                                Arrays.asList(predictionResults),
                                firstZeitintervallForEachBewegungsbeziehung);
                kiZeitintervalle.addAll(kiZeitintervalleForTagessumme);
                ZeitintervallKIUtil.expandKiHochrechnungen(kiZeitintervalle);
                ZeitintervallKIUtil.mergeKiHochrechnungInGesamt(summierteZeitbloecke, kiZeitintervalle);
            } catch (final PredictionFailedException exception) {
                log.error("Error predicting Tagessummen with KIService\n" + exception);
            }
        }

        final var allZeitintervalle = new ArrayList<Zeitintervall>();
        allZeitintervalle.addAll(byTimeAndTypeAndSortingIndexAdaptedZeitintervalle);
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
        final List<Zeitintervall> zeitintervalle = zeitintervallRepository.findByZaehlungId(
                UUID.fromString(zaehlung.getId()),
                Sort.by(Sort.Direction.ASC, "startUhrzeit"));
        // überprüfen, ob alle Zeitintervalle vorhanden sind
        if (numberOfIntervalle == 0 || numberOfIntervalle == zeitintervalle.size()) {
            final var kiAufbereitungNecessary = List
                    .of(Zaehldauer.DAUER_2_X_4_STUNDEN, Zaehldauer.DAUER_13_STUNDEN, Zaehldauer.DAUER_16_STUNDEN)
                    .contains(Zaehldauer.valueOf(zaehlung.getZaehldauer()));
            aufbereitenUndPersistieren(zeitintervalle, kiAufbereitungNecessary);
        } else {
            throw new PlausibilityException("Die Anzahl der übermittelten Zeitintervalle stimmt nicht mit den erwarteten überein");
        }
    }

    @Transactional
    public void deleteZeitintervalleByIdOfBewegungsbeziehung(final List<String> bewegungsbeziehungsIds) {
        final var uuidsOfVerkehrsbeziehungen = CollectionUtils
                .emptyIfNull(bewegungsbeziehungsIds)
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
