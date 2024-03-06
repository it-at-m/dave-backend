package de.muenchen.dave.services.messstelle;

import de.muenchen.dave.domain.dtos.laden.messwerte.LadeProcessedMesswerteDTO;
import de.muenchen.dave.domain.dtos.messstelle.MessstelleOptionsDTO;
import de.muenchen.dave.exceptions.ResourceNotFoundException;
import de.muenchen.dave.geodateneai.gen.api.MesswerteApi;
import de.muenchen.dave.geodateneai.gen.model.AverageMeasurementValuesPerIntervalResponse;
import de.muenchen.dave.geodateneai.gen.model.GetMeasurementValuesRequest;
import de.muenchen.dave.geodateneai.gen.model.MeasurementValuesPerInterval;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

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

    public LadeProcessedMesswerteDTO ladeMesswerte(final String messstelleId, final MessstelleOptionsDTO options) {
        log.debug("#ladeMesswerte {}", messstelleId);
        final AverageMeasurementValuesPerIntervalResponse response = this.ladeMesswerteIntervall(options);
        final List<MeasurementValuesPerInterval> intervalle = response.getIntervals();

        final LadeProcessedMesswerteDTO processedZaehldaten = new LadeProcessedMesswerteDTO();
        processedZaehldaten.setZaehldatenStepline(ganglinieService.ladeGanglinie(intervalle));
        processedZaehldaten.setZaehldatenHeatmap(heatmapService.ladeHeatmap(intervalle));
        processedZaehldaten.setZaehldatenTable(listenausgabeService.ladeListenausgabe(intervalle, messstelleService.isKfzMessstelle(messstelleId)));
        return processedZaehldaten;
    }

    protected AverageMeasurementValuesPerIntervalResponse ladeMesswerteIntervall(final MessstelleOptionsDTO options) {
        final GetMeasurementValuesRequest request = new GetMeasurementValuesRequest();
        // Anhand der MesstellenId die entsprechenden MessquerschnittIds ermitteln
        request.setMessquerschnittIds(options.getMessquerschnitte());
        request.setTagesTyp(options.getTagesTyp());
        request.setZeitpunktStart(options.getZeitraum().get(0));
        if (options.getZeitraum().size() == 2) {
            request.setZeitpunktEnde(options.getZeitraum().get(1));
        } else {
            request.setZeitpunktEnde(options.getZeitraum().get(0));
        }
        // TODO: Für Auswahl der Intervalle wird auch für die Uhrzeit ein Start und Ende benötigt
//        options.getZeitblock();

        // TODO: Für die Zeitauswahl Spitzenstunde wird auch noch ein boolean Feld benötigt
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
