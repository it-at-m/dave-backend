package de.muenchen.dave.services.messstelle;

import de.muenchen.dave.domain.dtos.laden.messwerte.BelastungsplanMessquerschnitteDTO;
import de.muenchen.dave.domain.dtos.laden.messwerte.LadeProcessedMesswerteDTO;
import de.muenchen.dave.domain.dtos.messstelle.MessstelleOptionsDTO;
import de.muenchen.dave.domain.enums.TagesTyp;
import de.muenchen.dave.exceptions.BadRequestException;
import de.muenchen.dave.exceptions.ResourceNotFoundException;
import de.muenchen.dave.geodateneai.gen.api.MesswerteApi;
import de.muenchen.dave.geodateneai.gen.model.IntervalDto;
import de.muenchen.dave.geodateneai.gen.model.IntervalResponseDto;
import de.muenchen.dave.geodateneai.gen.model.MesswertRequestDto;
import de.muenchen.dave.util.OptionsUtil;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@AllArgsConstructor
public class MesswerteService {

    private final MessstelleService messstelleService;
    private final MesswerteApi messwerteApi;

    private final GanglinieService ganglinieService;
    private final HeatmapService heatmapService;
    private final ListenausgabeService listenausgabeService;
    private final BelastungsplanService belastungsplanService;
    private final SpitzenstundeService spitzenstundeService;

    private static final String ERROR_MESSAGE = "Beim Laden der AverageMeasurementValuesPerIntervalResponse ist ein Fehler aufgetreten";

    public LadeProcessedMesswerteDTO ladeMesswerte(final String messstelleId, final MessstelleOptionsDTO options) {
        validateOptions(options);
        log.debug("#ladeMesswerte {}", messstelleId);

        final IntervalResponseDto response = this.ladeMesswerteIntervall(options);
        final List<IntervalDto> intervals;
        if (OptionsUtil.isZeitauswahlSpitzenstunde(options.getZeitauswahl())) {
            intervals = spitzenstundeService.getIntervalsOfSpitzenstunde(
                    response.getMeanOfMqIdForEachIntervalByMesstag(),
                    messstelleService.isKfzMessstelle(messstelleId),
                    options.getIntervall());
        } else {
            intervals = response.getMeanOfMqIdForEachIntervalByMesstag();
        }

        final List<IntervalDto> meanPerMessquerschnitt = response
                .getMeanOfIntervalsForEachMqIdByMesstag()
                .stream()
                .flatMap(intervalsForMqId -> intervalsForMqId.getMeanOfIntervalsByMesstag().stream())
                .toList();

        final var processedZaehldaten = new LadeProcessedMesswerteDTO();
        processedZaehldaten.setZaehldatenStepline(ganglinieService.ladeGanglinie(intervals, options));
        processedZaehldaten.setZaehldatenHeatmap(heatmapService.ladeHeatmap(intervals, options));
        processedZaehldaten.setZaehldatenTable(listenausgabeService.ladeListenausgabe(intervals, messstelleService.isKfzMessstelle(messstelleId), options));
        processedZaehldaten
                .setBelastungsplanMessquerschnitte(belastungsplanService.ladeBelastungsplan(intervals, meanPerMessquerschnitt, messstelleId, options));
        if (CollectionUtils.isNotEmpty(intervals)) {
            processedZaehldaten.setTagesTyp(TagesTyp.valueOf(intervals.getFirst().getTagesTyp().name()));
        }
        return processedZaehldaten;
    }

    protected void validateOptions(final MessstelleOptionsDTO options) {
        if (options.getZeitraum().size() == 2 && ObjectUtils.isEmpty(options.getTagesTyp())) {
            throw new BadRequestException("Bei einem Zeitraum muss der Wochentag angegeben sein.");
        }
    }

    protected IntervalResponseDto ladeMesswerteIntervall(final MessstelleOptionsDTO options) {
        final var request = new MesswertRequestDto();
        // Anhand der MesstellenId die entsprechenden MessquerschnittIds ermitteln
        request.setMessquerschnittIds(options.getMessquerschnittIds().stream().map(Integer::valueOf).toList());
        if (ObjectUtils.isNotEmpty(options.getTagesTyp())) {
            request.setTagesTyp(options.getTagesTyp().getMesswertTyp());
        } else {
            request.setTagesTyp(MesswertRequestDto.TagesTypEnum.DTV);
        }
        if (options.getZeitraum().size() == 2) {
            Collections.sort(options.getZeitraum());
            request.setStartDate(options.getZeitraum().get(0));
            request.setEndDate(options.getZeitraum().get(1));
        } else {
            request.setStartDate(options.getZeitraum().get(0));
            request.setEndDate(options.getZeitraum().get(0));
        }
        request.setStartTime(options.getZeitblock().getStart().toLocalTime());
        request.setEndTime(options.getZeitblock().getEnd().toLocalTime());
        request.setIntervalInMinutes(options.getIntervall().getMesswertIntervalInMinutes());

        final Mono<ResponseEntity<IntervalResponseDto>> response = messwerteApi.getIntervalleWithHttpInfo(request);
        final ResponseEntity<IntervalResponseDto> block = response.block();
        if (ObjectUtils.isEmpty(block)) {
            log.error("ResponseEntity der Anfrage <getAverageMeasurementValuesPerIntervalWithHttpInfo> ist leer.");
            throw new ResourceNotFoundException(ERROR_MESSAGE);
        }
        final IntervalResponseDto body = block.getBody();
        if (ObjectUtils.isEmpty(body)) {
            log.error("Body der Anfrage <MeasurementValuesResponse> ist leer.");
            throw new ResourceNotFoundException(ERROR_MESSAGE);
        }
        if (ObjectUtils.isEmpty(body.getMeanOfMqIdForEachIntervalByMesstag())) {
            log.error("Body der Anfrage <MeasurementValuesResponse> enthält keine AverageMeasurementValuesPerInterval.");
            throw new ResourceNotFoundException(ERROR_MESSAGE);
        }
        if (ObjectUtils.isEmpty(body.getMeanOfIntervalsForEachMqIdByMesstag())) {
            log.error("Body der Anfrage <MeasurementValuesResponse> enthält keine TotalSumOfAllMessquerschnitte.");
            throw new ResourceNotFoundException(ERROR_MESSAGE);
        }
        return body;
    }
}
