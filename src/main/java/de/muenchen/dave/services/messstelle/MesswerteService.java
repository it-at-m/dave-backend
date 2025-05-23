package de.muenchen.dave.services.messstelle;

import de.muenchen.dave.domain.dtos.laden.messwerte.LadeProcessedMesswerteDTO;
import de.muenchen.dave.domain.dtos.messstelle.MessstelleOptionsDTO;
import de.muenchen.dave.domain.enums.TagesTyp;
import de.muenchen.dave.exceptions.ResourceNotFoundException;
import de.muenchen.dave.geodateneai.gen.api.MesswerteApi;
import de.muenchen.dave.geodateneai.gen.model.IntervalDto;
import de.muenchen.dave.geodateneai.gen.model.IntervalResponseDto;
import de.muenchen.dave.geodateneai.gen.model.MesswertRequestDto;
import de.muenchen.dave.geodateneai.gen.model.TagesaggregatRequestDto;
import de.muenchen.dave.geodateneai.gen.model.TagesaggregatResponseDto;
import de.muenchen.dave.services.KalendertagService;
import de.muenchen.dave.util.OptionsUtil;
import de.muenchen.dave.util.messstelle.MesswerteBaseUtil;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class MesswerteService {

    private static final String ERROR_MESSAGE = "Beim Laden der AverageMeasurementValuesPerIntervalResponse ist ein Fehler aufgetreten";
    private final MessstelleService messstelleService;
    private final MesswerteApi messwerteApi;
    private final GanglinieService ganglinieService;
    private final HeatmapService heatmapService;
    private final ListenausgabeService listenausgabeService;
    private final BelastungsplanService belastungsplanService;
    private final SpitzenstundeService spitzenstundeService;
    private final KalendertagService kalendertagService;

    /**
     * Bereitet die geladenen Messwerte der gewünschten Messstelle für die GUI auf.
     *
     * @param messstelleId Zu ladende Messstelle
     * @param options in der GUI definierte Optionen zum Laden der Daten
     * @return aufbereitete Daten
     */
    public LadeProcessedMesswerteDTO ladeMesswerte(final String messstelleId, final MessstelleOptionsDTO options) {
        log.debug("#ladeMesswerte {}", messstelleId);

        final IntervalResponseDto response = this.ladeMesswerteIntervalle(options, messstelleService.getMessquerschnittIdsByMessstelleId(messstelleId));
        final var isKfzMessstelle = messstelleService.isKfzMessstelle(messstelleId);
        final List<IntervalDto> intervals;

        if (OptionsUtil.isZeitauswahlSpitzenstunde(options.getZeitauswahl())) {
            // Extrahieren der Intervalle welche die Spitzenstunde ausmachen.
            intervals = spitzenstundeService.getIntervalsOfSpitzenstunde(
                    ListUtils.emptyIfNull(response.getMeanOfMqIdForEachIntervalByMesstag()),
                    isKfzMessstelle,
                    options.getIntervall());
        } else {
            intervals = ListUtils.emptyIfNull(response.getMeanOfMqIdForEachIntervalByMesstag());
        }

        final var meanPerMessquerschnitt = ListUtils.emptyIfNull(response.getMeanOfIntervalsForEachMqIdByMesstag())
                .stream()
                .flatMap(intervalsForMqId -> ListUtils.emptyIfNull(intervalsForMqId.getMeanOfIntervalsByMesstag()).stream())
                .toList();

        final var processedZaehldaten = new LadeProcessedMesswerteDTO();
        processedZaehldaten.setZaehldatenStepline(ganglinieService.ladeGanglinie(intervals, options.getFahrzeuge()));
        processedZaehldaten.setZaehldatenHeatmap(heatmapService.ladeHeatmap(intervals, options));
        processedZaehldaten.setZaehldatenTable(listenausgabeService.ladeListenausgabe(intervals, isKfzMessstelle, options));
        processedZaehldaten
                .setBelastungsplanMessquerschnitte(belastungsplanService.ladeBelastungsplan(intervals, meanPerMessquerschnitt, messstelleId, options));
        if (CollectionUtils.isNotEmpty(intervals)) {
            processedZaehldaten.setTagesTyp(TagesTyp.getByIntervallTyp(intervals.getFirst().getTagesTyp()));
        }

        // Da für die Auswertung nicht alle Tage innerhalb des Zeitraums relevant sind,
        // werden anhand des ausgewählten Tagestyps die relevanten Kalendertage ermittelt
        if (MesswerteBaseUtil.isDateRange(options.getZeitraum())) {
            final var tagestypen = TagesTyp.getIncludedTagestypen(options.getTagesTyp());
            final long numberOfRelevantKalendertage = kalendertagService.countAllKalendertageByDatumAndTagestypen(
                    options.getZeitraum().getFirst(),
                    options.getZeitraum().getLast(), tagestypen);
            processedZaehldaten.setRequestedMeasuringDays(numberOfRelevantKalendertage);
            processedZaehldaten.setIncludedMeasuringDays(response.getIncludedMeasuringDays());
        }
        return processedZaehldaten;
    }

    /**
     * Lädt die Messwerte als Intervalle anhand der definierten Messquerschnitt-IDs aus der Geodaten-EAI
     *
     * @param options definierte Optionen zum Laden der Daten
     * @param messquerschnittIds zu ladende Messquerschnitte
     * @return geladene Intervall-Daten als DTO
     */
    protected IntervalResponseDto ladeMesswerteIntervalle(final MessstelleOptionsDTO options, final Set<String> messquerschnittIds) {
        final var request = new MesswertRequestDto();
        // Anhand der MesstellenId die entsprechenden MessquerschnittIds ermitteln
        request.setSelectedMessquerschnittIds(options.getMessquerschnittIds().stream().map(Integer::valueOf).toList());
        request.setAllMessquerschnittIds(messquerschnittIds.stream().map(Integer::valueOf).toList());
        if (ObjectUtils.isNotEmpty(options.getTagesTyp())) {
            request.setTagesTyp(options.getTagesTyp().getMesswertTyp());
        } else {
            request.setTagesTyp(MesswertRequestDto.TagesTypEnum.DTV);
        }
        if (options.getZeitraum().size() == 2) {
            Collections.sort(options.getZeitraum());
            request.setStartDate(options.getZeitraum().getFirst());
            request.setEndDate(options.getZeitraum().getLast());
        } else {
            request.setStartDate(options.getZeitraum().getFirst());
            request.setEndDate(options.getZeitraum().getFirst());
        }
        request.setStartTime(options.getZeitblock().getStart().toLocalTime());
        request.setEndTime(options.getZeitblock().getEnd().toLocalTime());
        request.setIntervalInMinutes(options.getIntervall().getMesswertIntervalInMinutes());

        final ResponseEntity<IntervalResponseDto> response = messwerteApi.getIntervalleWithHttpInfo(request).block();

        if (ObjectUtils.isEmpty(response) || ObjectUtils.isEmpty(response.getBody())) {
            log.error("Die Response beinhaltet keine Daten");
            throw new ResourceNotFoundException(ERROR_MESSAGE);
        }
        return response.getBody();
    }

    /**
     * Lädt die Messwerte als Tagesaggregat anhand der definierten Messquerschnitt-IDs aus der
     * Geodaten-EAI
     *
     * @param tagesTyp Tagestyp der zu ladenden Daten
     * @param mqIds zu ladende Messquerschnitte
     * @param zeitraum Zeitraum der zu ladenden Daten
     * @return die geladenen Tagesaggregate als DTO
     */
    public TagesaggregatResponseDto ladeTagesaggregate(final TagesTyp tagesTyp, final Set<String> mqIds, final Zeitraum zeitraum) {
        final var request = new TagesaggregatRequestDto();
        request.setMessquerschnittIds(mqIds.stream().map(Integer::valueOf).toList());
        request.setStartDate(LocalDate.of(zeitraum.getStart().getYear(), zeitraum.getStart().getMonthValue(), 1));
        request.setEndDate(LocalDate.of(zeitraum.getEnd().getYear(), zeitraum.getEnd().getMonthValue(), zeitraum.getEnd().atEndOfMonth().getDayOfMonth()));
        request.setTagesTyp(tagesTyp.getTagesaggregatTyp());

        final ResponseEntity<TagesaggregatResponseDto> response = messwerteApi.getMeanOfDailyAggregatesPerMQWithHttpInfo(request).block();

        if (ObjectUtils.isEmpty(response) || ObjectUtils.isEmpty(response.getBody())) {
            log.error("Die Response beinhaltet keine Daten");
            throw new ResourceNotFoundException(ERROR_MESSAGE);
        }
        return response.getBody();
    }

}
