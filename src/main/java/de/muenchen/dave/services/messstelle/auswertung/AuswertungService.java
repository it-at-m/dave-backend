/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.services.messstelle.auswertung;

import de.muenchen.dave.domain.dtos.messstelle.auswertung.MessstelleAuswertungDTO;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.MessstelleAuswertungOptionsDTO;
import de.muenchen.dave.domain.enums.AuswertungsZeitraum;
import de.muenchen.dave.domain.mapper.GeodatenEaiMapper;
import de.muenchen.dave.exceptions.ResourceNotFoundException;
import de.muenchen.dave.geodateneai.gen.api.MesswerteApi;
import de.muenchen.dave.geodateneai.gen.model.GetMeasurementValuesAggregateRequest;
import de.muenchen.dave.geodateneai.gen.model.MeasurementValuesAggregateDto;
import de.muenchen.dave.geodateneai.gen.model.MeasurementValuesAggregateResponse;
import de.muenchen.dave.services.messstelle.MessstelleService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@Service
@Slf4j
public class AuswertungService {

    private final MessstelleService messstelleService;
    private final MesswerteApi messwerteApi;
    private final GeodatenEaiMapper geodatenEaiMapper;

    private static final String ERROR_MESSAGE = "Beim Laden der MesswerteTagesaggregatMessquerschnittResponse ist ein Fehler aufgetreten";

    public List<MessstelleAuswertungDTO> getAllVisibleMessstellen() {
        return messstelleService.getAllVisibleMessstellenForAuswertungOrderByMstIdAsc();
    }

    public void loadDataForEvaluation(final MessstelleAuswertungOptionsDTO options) {
        log.info("#loadDataForEvaluation {}", options);
        final GetMeasurementValuesAggregateRequest request = new GetMeasurementValuesAggregateRequest();
        request.setMessquerschnittIdsPerMessstelle(calculateMessquerschnittIdsPerMessstelle(options.getMstIds(), options.getMqIds()));
        request.setTagesTyp(geodatenEaiMapper.backendToEai(options.getTagesTyp()));
        request.setZeitraeume(calculateZeitraeume(options.getZeitraum(), options.getJahre()));

        Map<String, MeasurementValuesAggregateDto> response = loadData(request);
        log.info(response.toString());

    }

    protected Map<String, MeasurementValuesAggregateDto> loadData(final GetMeasurementValuesAggregateRequest request) {
        final Mono<ResponseEntity<MeasurementValuesAggregateResponse>> response;
        if (request.getMessquerschnittIdsPerMessstelle().keySet().size() == 1) {
            response = messwerteApi.getMesswerteTagesaggregatPerMessquerschnittWithHttpInfo(
                    request);
        } else {
            response = messwerteApi.getMesswerteTagesaggregatPerMessstelleWithHttpInfo(
                    request);
        }
        final ResponseEntity<MeasurementValuesAggregateResponse> block = response.block();
        if (ObjectUtils.isEmpty(block)) {
            log.error("ResponseEntity der Anfrage <getAverageMeasurementValuesPerIntervalWithHttpInfo> ist leer.");
            throw new ResourceNotFoundException(ERROR_MESSAGE);
        }
        final MeasurementValuesAggregateResponse body = block.getBody();
        if (ObjectUtils.isEmpty(body)) {
            log.error("Body der Anfrage <MeasurementValuesAggregateResponse> ist leer.");
            throw new ResourceNotFoundException(ERROR_MESSAGE);
        }
        final Map<String, MeasurementValuesAggregateDto> measurementValues = body.getMeasurementValues();
        if (ObjectUtils.isEmpty(measurementValues)) {
            log.error("Body der Anfrage <MeasurementValuesAggregateResponse> enthält keine Messwerte.");
            throw new ResourceNotFoundException(ERROR_MESSAGE);
        }

        return measurementValues;
    }

    protected Map<String, Set<String>> calculateMessquerschnittIdsPerMessstelle(final List<String> mstIds, final List<String> mqIds) {
        final Map<String, Set<String>> result = new HashMap<>();
        if (mstIds.size() == 1) {
            // per MQ
            result.put(mstIds.get(0), Set.copyOf(mqIds));
        } else {
            // per Mst
            mstIds.forEach(mstId -> result.put(mstId, messstelleService.getMessquerschnittNummern(mstId)));
        }
        return result;
    }

    protected List<List<LocalDate>> calculateZeitraeume(final List<AuswertungsZeitraum> auswertungszeitraeume, final List<Integer> jahre) {
        final List<List<LocalDate>> result = new ArrayList<>();
        auswertungszeitraeume.forEach(auswertungszeitraum -> jahre.forEach(jahr -> {
            LocalDate end = auswertungszeitraum.getZeitraumEnd().withYear(jahr);
            if (AuswertungsZeitraum.FEBRUAR == auswertungszeitraum && jahr % 4 == 0) {
                end = end.withDayOfMonth(29);
            }
            result.add(List.of(auswertungszeitraum.getZeitraumStart().withYear(jahr), end));
        }));
        return result;
    }
}
