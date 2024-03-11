package de.muenchen.dave.services.messstelle;

import de.muenchen.dave.domain.dtos.laden.messwerte.LadeProcessedMesswerteDTO;
import de.muenchen.dave.domain.dtos.laden.messwerte.ListBelastungsplanMessquerschnitteDTO;
import de.muenchen.dave.exceptions.ResourceNotFoundException;
import de.muenchen.dave.geodateneai.gen.api.MesswerteApi;
import de.muenchen.dave.geodateneai.gen.model.AverageMeasurementValuesPerIntervalResponse;
import de.muenchen.dave.geodateneai.gen.model.GetMeasurementValuesRequest;
import de.muenchen.dave.geodateneai.gen.model.MeasurementValuesPerInterval;
import de.muenchen.dave.geodateneai.gen.model.MeasurementValuesResponse;
import de.muenchen.dave.geodateneai.gen.model.TotalSumPerMessquerschnitt;
import java.time.LocalDate;
import java.util.ArrayList;
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
    private final BelastungsplanService belastungsplanService;

    private static final String ERROR_MESSAGE = "Beim Laden der AverageMeasurementValuesPerIntervalResponse ist ein Fehler aufgetreten";

    public LadeProcessedMesswerteDTO ladeMesswerte(final String messstelleId) {
        log.debug("#ladeMesswerte {}", messstelleId);
        final Set<String> messquerschnittNummern = messstelleService.getMessquerschnittNummern(messstelleId);

        final MeasurementValuesResponse response = this.ladeMesswerteIntervall(messquerschnittNummern);
        final List<MeasurementValuesPerInterval> intervalle = response.getAverageMeasurementValuesPerIntervalResponse().getIntervals();
        final List<TotalSumPerMessquerschnitt> totalSumPerMessquerschnittList = response.getTotalSumOfAllMessquerschnitte().getTotalSumPerMessquerschnittList();

        final LadeProcessedMesswerteDTO processedZaehldaten = new LadeProcessedMesswerteDTO();
        processedZaehldaten.setZaehldatenStepline(ganglinieService.ladeGanglinie(intervalle));
        processedZaehldaten.setZaehldatenHeatmap(heatmapService.ladeHeatmap(intervalle));
        processedZaehldaten.setZaehldatenTable(listenausgabeService.ladeListenausgabe(intervalle, messstelleService.isKfzMessstelle(messstelleId)));
        processedZaehldaten.setListBelastungsplanMessquerschnitteDTO(new ListBelastungsplanMessquerschnitteDTO());
        processedZaehldaten.setListBelastungsplanMessquerschnitteDTO(belastungsplanService.ladeBelastungsplan(totalSumPerMessquerschnittList, messstelleId));
        return processedZaehldaten;
    }

    protected MeasurementValuesResponse ladeMesswerteIntervall(final Set<String> messquerschnittIds) {
        final GetMeasurementValuesRequest request = new GetMeasurementValuesRequest();
        // Anhand der MesstellenId die entsprechenden MessquerschnittIds ermitteln
        request.setMessquerschnittIds(messquerschnittIds);
        request.setTagesTyp(GetMeasurementValuesRequest.TagesTypEnum.WERKTAG_DI_MI_DO);
        request.setZeitpunktStart(LocalDate.of(2024, 1, 1));
        request.setZeitpunktEnde(LocalDate.of(2024, 1, 1));
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
