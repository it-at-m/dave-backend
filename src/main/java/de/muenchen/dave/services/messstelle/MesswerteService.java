package de.muenchen.dave.services.messstelle;

import de.muenchen.dave.domain.dtos.laden.messwerte.LadeProcessedMesswerteDTO;
import de.muenchen.dave.domain.dtos.laden.messwerte.ListBelastungsplanMessquerschnitteDTO;
import de.muenchen.dave.domain.dtos.messstelle.MessstelleOptionsDTO;
import de.muenchen.dave.exceptions.BadRequestException;
import de.muenchen.dave.exceptions.ResourceNotFoundException;
import de.muenchen.dave.geodateneai.gen.api.MesswerteApi;
import de.muenchen.dave.geodateneai.gen.model.AverageMeasurementValuesPerIntervalResponse;
import de.muenchen.dave.geodateneai.gen.model.GetMeasurementValuesRequest;
import de.muenchen.dave.geodateneai.gen.model.MeasurementValuesPerInterval;
import de.muenchen.dave.geodateneai.gen.model.MeasurementValuesResponse;
import de.muenchen.dave.geodateneai.gen.model.TotalSumPerMessquerschnitt;
import java.time.LocalDate;
import java.util.ArrayList;
import de.muenchen.dave.util.OptionsUtil;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
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
        final MeasurementValuesResponse response = this.ladeMesswerteIntervall(options);
        final List<MeasurementValuesPerInterval> intervals;
        if (OptionsUtil.isZeitauswahlSpitzenstunde(options.getZeitauswahl())) {
            intervals = spitzenstundeService.getIntervalsOfSpitzenstunde(response.getAverageMeasurementValuesPerIntervalResponse().getIntervals(),
                    messstelleService.isKfzMessstelle(messstelleId));
        } else {
            intervals = response.getAverageMeasurementValuesPerIntervalResponse().getIntervals();
        }
        final List<TotalSumPerMessquerschnitt> totalSumPerMessquerschnittList = response.getTotalSumOfAllMessquerschnitte().getTotalSumPerMessquerschnittList();

        final LadeProcessedMesswerteDTO processedZaehldaten = new LadeProcessedMesswerteDTO();
        processedZaehldaten.setZaehldatenStepline(ganglinieService.ladeGanglinie(intervals, options));
        processedZaehldaten.setZaehldatenHeatmap(heatmapService.ladeHeatmap(intervals, options));
        processedZaehldaten.setZaehldatenTable(listenausgabeService.ladeListenausgabe(intervals, messstelleService.isKfzMessstelle(messstelleId), options));
        processedZaehldaten.setListBelastungsplanMessquerschnitteDTO(new ListBelastungsplanMessquerschnitteDTO());
        processedZaehldaten.setListBelastungsplanMessquerschnitteDTO(belastungsplanService.ladeBelastungsplan(totalSumPerMessquerschnittList, messstelleId));
        return processedZaehldaten;
    }

    protected void validateOptions(final MessstelleOptionsDTO options) {
        if (options.getZeitraum().size() == 2 && StringUtils.isEmpty(options.getTagesTyp())) {
            throw new BadRequestException("Bei einem Zeitraum muss der Wochentag angegeben sein.");
        }
    }

    protected MeasurementValuesResponse ladeMesswerteIntervall(final MessstelleOptionsDTO options) {
        final GetMeasurementValuesRequest request = new GetMeasurementValuesRequest();
        // Anhand der MesstellenId die entsprechenden MessquerschnittIds ermitteln
        request.setMessquerschnittIds(options.getMessquerschnittIds());
        if (StringUtils.isNotEmpty(options.getTagesTyp())) {
            request.setTagesTyp(GetMeasurementValuesRequest.TagesTypEnum.valueOf(options.getTagesTyp()));
        }
        request.setZeitpunktStart(options.getZeitraum().get(0));
        if (options.getZeitraum().size() == 2) {
            request.setZeitpunktEnde(options.getZeitraum().get(1));
        } else {
            request.setZeitpunktEnde(options.getZeitraum().get(0));
        }
        request.setUhrzeitStart(options.getZeitblock().getStart().toLocalTime());
        request.setUhrzeitEnde(options.getZeitblock().getEnd().toLocalTime());
        request.setMinutesPerZeitintervall(options.getIntervall().getMinutesPerIntervall());

        final Mono<ResponseEntity<MeasurementValuesResponse>> response = messwerteApi
                .getAverageMeasurementValuesPerIntervalWithHttpInfo(
                        request);
        final ResponseEntity<MeasurementValuesResponse> block = response.block();
        if (ObjectUtils.isEmpty(block)) {
            log.error("ResponseEntity der Anfrage <getAverageMeasurementValuesPerIntervalWithHttpInfo> ist leer.");
            throw new ResourceNotFoundException(ERROR_MESSAGE);
        }
        final MeasurementValuesResponse body = block.getBody();
        if (ObjectUtils.isEmpty(body)) {
            log.error("Body der Anfrage <MeasurementValuesResponse> ist leer.");
            throw new ResourceNotFoundException(ERROR_MESSAGE);
        }
        if (ObjectUtils.isEmpty(body.getAverageMeasurementValuesPerIntervalResponse())) {
            log.error("Body der Anfrage <MeasurementValuesResponse> enthält keine AverageMeasurementValuesPerInterval.");
            throw new ResourceNotFoundException(ERROR_MESSAGE);
        }
        if (ObjectUtils.isEmpty(body.getTotalSumOfAllMessquerschnitte())) {
            log.error("Body der Anfrage <MeasurementValuesResponse> enthält keine TotalSumOfAllMessquerschnitte.");
            throw new ResourceNotFoundException(ERROR_MESSAGE);
        }
        if (CollectionUtils.isEmpty(body.getAverageMeasurementValuesPerIntervalResponse().getIntervals())) {
            log.error("Body der Anfrage <getAverageMeasurementValuesPerIntervalWithHttpInfo> enthält keine Messwerte.");
            throw new ResourceNotFoundException(ERROR_MESSAGE);
        }
        return body;
    }
}
