package de.muenchen.dave.services.messstelle;

import de.muenchen.dave.domain.dtos.laden.LadeProcessedZaehldatenDTO;
import de.muenchen.dave.exceptions.ResourceNotFoundException;
import de.muenchen.dave.geodateneai.gen.api.MesswerteApi;
import de.muenchen.dave.geodateneai.gen.model.AverageMeasurementValuesPerIntervalResponse;
import de.muenchen.dave.geodateneai.gen.model.GetMeasurementValuesRequest;
import de.muenchen.dave.geodateneai.gen.model.MeasurementValuesPerInterval;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    public LadeProcessedZaehldatenDTO ladeMesswerte(final String messstelleId) {
        log.debug("#ladeMesswerte {}", messstelleId);
        final Set<String> messquerschnittNummern = messstelleService.getMessquerschnittNummern(messstelleId);

        final AverageMeasurementValuesPerIntervalResponse response = this.ladeMesswerteIntervall(messquerschnittNummern);
        final List<MeasurementValuesPerInterval> intervalle = response.getIntervals();

        final LadeProcessedZaehldatenDTO processedZaehldaten = new LadeProcessedZaehldatenDTO();
        processedZaehldaten.setZaehldatenStepline(ganglinieService.ladeGanglinie(intervalle));
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
        final AverageMeasurementValuesPerIntervalResponse body = Objects.requireNonNull(response.block()).getBody();
        if (ObjectUtils.isEmpty(body) || ObjectUtils.isEmpty(body.getIntervals())) {
            throw new ResourceNotFoundException("Die Intervalle konnten nicht geladen werden");
        }
        return body;

    }

}
