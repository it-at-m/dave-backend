/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
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
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.extern.slf4j.Slf4j;
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
     * Die Methode führt vor der Persistierung der Zeitintervalle in der Datenbank
     * eine Datenaufbreitung durch. D.h.:
     * <p>
     * - Die im Parameter übergebenen Zeitintervalle werden daraufhin überprüft,
     * ob der letzte Zeitintervall des Tages die korrekte Endeuhrzeit von 23:59 aufweist.
     * - Die im Parameter übergebenen Zeitintervalle werden mit einem Index für
     * die Sortierung bei der Datenextraktion versehen.
     * - Die im Parameter übergebenen Zeitintervalle werden mit
     * dem Merkmal {@link TypeZeitintervall#STUNDE_VIERTEL} versehen.
     * - Die im Parameter übergebenen Zeitintervalle werden je Intervall über
     * alle möglichen Fahrbeziehungspermutationen summiert.
     * - Für die über Fahrbeziehungspermutationen summierten und auch im Parameter übergebene
     * Zeitintervalle
     * werden die gleitenden Spitzenstunden ermittelt.
     * - Für die über Fahrbeziehungspermutationen summierten und auch im Parameter übergebene
     * Zeitintervalle
     * werden die Summen für die einzelnen {@link Zeitblock}e gebildet.
     *
     * @param zeitintervalle Die {@link Zeitintervall}e zur vorherigen Aufbereitung vor
     *            der eigentlichen Persistierung.
     * @param kiAufbereitung KI Aufbereitung ausführen (Nur für 2x4h Zählungen)
     * @return Alle persistierten {@link Zeitintervall}e.
     */
    public List<Zeitintervall> aufbereitenUndPersistieren(final List<Zeitintervall> zeitintervalle, final boolean kiAufbereitung) {

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
         * alle möglichen Fahrbeziehungspermutationen summiert.
         */
        final List<Zeitintervall> summierteFahrbeziehungen = ZeitintervallFahrbeziehungsSummationUtil
                .getUeberFahrbeziehungSummierteZeitintervalle(zeitintervalle);

        List<Zeitintervall> allPossibleFahrbeziehungen = new ArrayList<>();
        allPossibleFahrbeziehungen.addAll(zeitintervalle);
        allPossibleFahrbeziehungen.addAll(summierteFahrbeziehungen);

        /*
         * - Für die über Fahrbeziehungspermutationen summierten und auch im Parameter übergebene
         * Zeitintervalle
         * werden die gleitenden Spitzenstunden ermittelt.
         */
        final List<Zeitintervall> gleitendeSpitzenstunden = ZeitintervallGleitendeSpitzenstundeUtil.getGleitendeSpitzenstunden(allPossibleFahrbeziehungen);

        /*
         * - Für die über Fahrbeziehungspermutationen summierten und auch im Parameter übergebene
         * Zeitintervalle
         * werden die Summen für die einzelnen {@link Zeitblock}e gebildet.
         */
        final List<Zeitintervall> summierteZeitbloecke = ZeitintervallZeitblockSummationUtil.getSummen(allPossibleFahrbeziehungen);

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

        return persistZeitintervalle(allZeitintervalle);
    }

    /**
     * Persistierung der Aufbereiteten {@link Zeitintervall}e in der relationalen Datenbank.
     *
     * @param toPersist zu speichernde Zeitintervalle
     * @return Alle persistierten {@link Zeitintervall}e.
     */
    public List<Zeitintervall> persistZeitintervalle(final List<Zeitintervall> toPersist) {
        Iterable<Zeitintervall> persistedZeitintervalle = zeitintervallRepository.saveAll(toPersist);
        zeitintervallRepository.flush();
        return StreamSupport.stream(persistedZeitintervalle.spliterator(), false)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<Zeitintervall> checkZeitintervalleIfPlausible(final Zaehlung zaehlung, final int numberOfIntervalle) throws PlausibilityException {
        final List<Zeitintervall> zeitintervalle = zeitintervallRepository.findByZaehlungId(UUID.fromString(zaehlung.getId()),
                Sort.by(Sort.Direction.ASC, "startUhrzeit"));
        // überprüfen, ob alle Zeitintervalle vorhanden sind
        if (numberOfIntervalle == 0 || numberOfIntervalle == zeitintervalle.size()) {
            return aufbereitenUndPersistieren(zeitintervalle, List.of(Zaehldauer.DAUER_2_X_4_STUNDEN, Zaehldauer.DAUER_13_STUNDEN, Zaehldauer.DAUER_16_STUNDEN)
                    .contains(Zaehldauer.valueOf(zaehlung.getZaehldauer())));
        } else {
            throw new PlausibilityException("Die Anzahl der übermittelten Zeitintervalle stimmt nicht mit den erwarteten überein");
        }
    }

    @Transactional
    public boolean deleteZeitintervalleByFahrbeziehungId(final String fahrbeziehungId) {
        final UUID fahrbeziehungIdAsUUID = UUID.fromString(fahrbeziehungId);
        zeitintervallRepository.deleteAllByFahrbeziehungId(fahrbeziehungIdAsUUID);
        zeitintervallRepository.flush();
        return !zeitintervallRepository.existsByFahrbeziehungId(fahrbeziehungIdAsUUID);
    }

    @Transactional
    public void deleteZeitintervalleForCorrection(final String zaehlungId) {
        zeitintervallRepository.deleteAllByZaehlungId(UUID.fromString(zaehlungId));
        zeitintervallRepository.flush();
    }

}
