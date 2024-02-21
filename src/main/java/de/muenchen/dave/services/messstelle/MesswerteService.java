package de.muenchen.dave.services.messstelle;

import de.muenchen.dave.domain.dtos.laden.messwerte.LadeProcessedMesswerteDTO;
import de.muenchen.dave.exceptions.ResourceNotFoundException;
import de.muenchen.dave.geodateneai.gen.api.MesswerteApi;
import de.muenchen.dave.geodateneai.gen.model.AverageMeasurementValuesPerIntervalResponse;
import de.muenchen.dave.geodateneai.gen.model.GetMeasurementValuesRequest;
import de.muenchen.dave.geodateneai.gen.model.MeasurementValuesPerInterval;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
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

    private static final String ERROR_MESSAGE = "Beim Laden der AverageMeasurementValuesPerIntervalResponse ist ein Fehler aufgetreten";

    public LadeProcessedMesswerteDTO ladeMesswerte(final String messstelleId) {
        log.debug("#ladeMesswerte {}", messstelleId);
        final Set<String> messquerschnittNummern = messstelleService.getMessquerschnittNummern(messstelleId);

        final AverageMeasurementValuesPerIntervalResponse response = this.ladeMesswerteIntervall(messquerschnittNummern);
        final List<MeasurementValuesPerInterval> intervalle = response.getIntervals();

        final LadeProcessedMesswerteDTO processedZaehldaten = new LadeProcessedMesswerteDTO();
        processedZaehldaten.setZaehldatenStepline(ganglinieService.ladeGanglinie(intervalle));
        processedZaehldaten.setZaehldatenHeatmap(heatmapService.ladeHeatmap(intervalle));
        processedZaehldaten.setZaehldatenTable(listenausgabeService.ladeListenausgabe(intervalle));
        return processedZaehldaten;
    }

    protected AverageMeasurementValuesPerIntervalResponse ladeMesswerteIntervall(final Set<String> messquerschnittIds) {
        final GetMeasurementValuesRequest request = new GetMeasurementValuesRequest();
        // Anhand der MesstellenId die entsprechenden MessquerschnittIds ermitteln
        request.setMessquerschnittIds(messquerschnittIds);
        request.setTagesTyp(GetMeasurementValuesRequest.TagesTypEnum.WERKTAG_DI_MI_DO);
        request.setZeitpunktStart(LocalDate.of(2024, 1, 1));
        request.setZeitpunktEnde(LocalDate.of(2024, 1, 1));
        final Mono<ResponseEntity<AverageMeasurementValuesPerIntervalResponse>> response = messwerteApi
                .getAverageMeasurementValuesPerIntervalWithHttpInfo(
                        request);
        final ResponseEntity<AverageMeasurementValuesPerIntervalResponse> block = response.block();
        if (ObjectUtils.isEmpty(block)) {
            log.error("ResponseEntity der Anfrage <getAverageMeasurementValuesPerIntervalWithHttpInfo> ist leer.");
            throw new ResourceNotFoundException(ERROR_MESSAGE);
        }
        final AverageMeasurementValuesPerIntervalResponse body = block.getBody();
        if (ObjectUtils.isEmpty(body)) {
            log.error("Body der Anfrage <getAverageMeasurementValuesPerIntervalWithHttpInfo> ist leer.");
            throw new ResourceNotFoundException(ERROR_MESSAGE);
        }
        if (CollectionUtils.isEmpty(body.getIntervals())) {
            log.error("Body der Anfrage <getAverageMeasurementValuesPerIntervalWithHttpInfo> enthält keine Messwerte.");
            throw new ResourceNotFoundException(ERROR_MESSAGE);
        }
        return body;
    }
}
