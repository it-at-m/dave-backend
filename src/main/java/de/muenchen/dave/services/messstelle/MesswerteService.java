package de.muenchen.dave.services.messstelle;

import de.muenchen.dave.domain.dtos.laden.messwerte.LadeProcessedMesswerteDTO;
import de.muenchen.dave.domain.dtos.messstelle.MessstelleOptionsDTO;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.MessstelleAuswertungOptionsDTO;
import de.muenchen.dave.domain.enums.TagesTyp;
import de.muenchen.dave.exceptions.BadRequestException;
import de.muenchen.dave.exceptions.ResourceNotFoundException;
import de.muenchen.dave.geodateneai.gen.api.MesswerteApi;
import de.muenchen.dave.geodateneai.gen.model.IntervalDto;
import de.muenchen.dave.geodateneai.gen.model.IntervalResponseDto;
import de.muenchen.dave.geodateneai.gen.model.MesswertRequestDto;
import de.muenchen.dave.geodateneai.gen.model.TagesaggregatRequestDto;
import de.muenchen.dave.geodateneai.gen.model.TagesaggregatResponseDto;
import de.muenchen.dave.util.OptionsUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;

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

    public LadeProcessedMesswerteDTO ladeMesswerte(final String messstelleId, final MessstelleOptionsDTO options) {
        validateOptions(options);
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
                .flatMap(intervalsForMqId -> intervalsForMqId.getMeanOfIntervalsByMesstag().stream())
                .toList();

        final var processedZaehldaten = new LadeProcessedMesswerteDTO();
        processedZaehldaten.setZaehldatenStepline(ganglinieService.ladeGanglinie(intervals, options));
        processedZaehldaten.setZaehldatenHeatmap(heatmapService.ladeHeatmap(intervals, options));
        processedZaehldaten.setZaehldatenTable(listenausgabeService.ladeListenausgabe(intervals, isKfzMessstelle, options));
        processedZaehldaten
                .setBelastungsplanMessquerschnitte(belastungsplanService.ladeBelastungsplan(intervals, meanPerMessquerschnitt, messstelleId, options));
        if (CollectionUtils.isNotEmpty(intervals)) {
            processedZaehldaten.setTagesTyp(TagesTyp.getByIntervallTyp(intervals.getFirst().getTagesTyp()));
        }
        processedZaehldaten.setRequestedMeasuringDays(options.getZeitraum().getFirst().until(options.getZeitraum().getLast()).getDays() + 1);
        processedZaehldaten.setIncludedMeasuringDays(response.getIncludedMeasuringDays());
        return processedZaehldaten;
    }

    protected void validateOptions(final MessstelleOptionsDTO options) {
        if (options.getZeitraum().size() == 2 && ObjectUtils.isEmpty(options.getTagesTyp())) {
            throw new BadRequestException("Bei einem Zeitraum muss der Wochentag angegeben sein.");
        }
    }

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
     *
     * @param options
     * @param zeitraum
     * @return
     */
    public TagesaggregatResponseDto ladeTagesaggregate(final MessstelleAuswertungOptionsDTO options, final Zeitraum zeitraum) {
        final var request = this.createTagesaggregatRequest(options, zeitraum);
        final ResponseEntity<TagesaggregatResponseDto> response = messwerteApi.getMeanOfDailyAggregatesPerMQWithHttpInfo(request).block();

        if (ObjectUtils.isEmpty(response) || ObjectUtils.isEmpty(response.getBody())) {
            log.error("Die Response beinhaltet keine Daten");
            throw new ResourceNotFoundException(ERROR_MESSAGE);
        }
        return response.getBody();
    }

    protected TagesaggregatRequestDto createTagesaggregatRequest(final MessstelleAuswertungOptionsDTO options, final Zeitraum zeitraum) {
        final var request = new TagesaggregatRequestDto();
        request.setMessquerschnittIds(options.getMqIds().stream().map(Integer::valueOf).toList());
        request.setStartDate(LocalDate.of(zeitraum.start.getYear(), zeitraum.start.getMonthValue(), 1));
        request.setEndDate(LocalDate.of(zeitraum.end.getYear(), zeitraum.end.getMonthValue(), zeitraum.end.atEndOfMonth().getDayOfMonth()));
        request.setTagesTyp(options.getTagesTyp().getTagesaggregatTyp());
        return request;
    }

}
