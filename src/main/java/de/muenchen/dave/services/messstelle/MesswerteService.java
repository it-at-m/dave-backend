package de.muenchen.dave.services.messstelle;

import de.muenchen.dave.domain.dtos.laden.messwerte.LadeProcessedMesswerteDTO;
import de.muenchen.dave.domain.dtos.messstelle.MessstelleOptionsDTO;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.AuswertungResponse;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.MessstelleAuswertungOptionsDTO;
import de.muenchen.dave.domain.elasticsearch.detektor.Messstelle;
import de.muenchen.dave.domain.enums.AuswertungsZeitraum;
import de.muenchen.dave.domain.enums.TagesTyp;
import de.muenchen.dave.domain.mapper.detektor.AuswertungMapper;
import de.muenchen.dave.exceptions.BadRequestException;
import de.muenchen.dave.exceptions.ResourceNotFoundException;
import de.muenchen.dave.geodateneai.gen.api.MesswerteApi;
import de.muenchen.dave.geodateneai.gen.model.IntervalDto;
import de.muenchen.dave.geodateneai.gen.model.IntervalResponseDto;
import de.muenchen.dave.geodateneai.gen.model.MesswertRequestDto;
import de.muenchen.dave.geodateneai.gen.model.TagesaggregatDto;
import de.muenchen.dave.geodateneai.gen.model.TagesaggregatRequestDto;
import de.muenchen.dave.geodateneai.gen.model.TagesaggregatResponseDto;
import de.muenchen.dave.util.OptionsUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalField;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collector;
import java.util.stream.Collectors;

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
    private final AuswertungMapper auswertungMapper;

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

    public Map<Integer, List<AuswertungResponse>> ladeAuswertung(final MessstelleAuswertungOptionsDTO options) {

        final List<Zeitraum> zeitraums = calculateZeitraeume(options.getZeitraum(), options.getJahre());

        // TagesaggregatResponseDto Januar
        // Liste für MQ
        // Wert Messstelle 1

        // TagesaggregatResponseDto Januar
        // Liste für MQ
        // Wert Messstelle 2

        // TagesaggregatResponseDto Februar
        // Liste für MQ
        // Wert Messstelle 1

        // TagesaggregatResponseDto Februar
        // Liste für MQ
        // Wert Messstelle 2

        // TagesaggregatResponseDto Januar und Februar
        // Liste für MQ
        // Wert Messstelle

        ConcurrentMap<Integer, List<AuswertungResponse>> collect = zeitraums.parallelStream().flatMap(zeitraum -> {
            return options.getMstIds().parallelStream().map(mstId -> {
                final Messstelle messstelle = messstelleService.getMessstelleByMstId(mstId);
                options.setMqIds(new HashSet<>());
                messstelle.getMessquerschnitte().forEach(messquerschnitt -> options.getMqIds().add(messquerschnitt.getMqId()));
                final TagesaggregatRequestDto requestDto = createRequestDto(options, zeitraum);
                final TagesaggregatResponseDto tagesaggregatResponseDto = sendRequest(requestDto);
                final AuswertungResponse auswertungResponse = auswertungMapper.tagesaggregatDto2AuswertungResponse(tagesaggregatResponseDto);
                auswertungResponse.setZeitraum(zeitraum);
                return auswertungResponse;
            });
            // TODO Pro Messstelle und deren MQ's einzeln Anfragen
            //            final TagesaggregatRequestDto requestDto = createRequestDto(options, zeitraum);
            //            final List<TagesaggregatDto> meanOfAggregatesForEachMqId = sendRequest(requestDto).getMeanOfAggregatesForEachMqId();
            //            final List<AuswertungResponse> auswertungResponses = auswertungMapper.tagesaggregatDto2AuswertungResponse(meanOfAggregatesForEachMqId);
            //            auswertungResponses.parallelStream().forEach(auswertungResponse -> {
            //                auswertungResponse.setZeitraum(zeitraum);
        }).collect(Collectors.groupingByConcurrent(tagesaggregatResponseDto -> tagesaggregatResponseDto.getMeanOfAggregatesForAllMqId().getMqId()));
        return collect;
        //            return auswertungResponses.stream();
        //        })
        //            .sorted(Comparator.comparing((AuswertungResponse o) -> o.getZeitraum().start).thenComparingInt(AuswertungResponse::getMqId))
        //                .toList();
    }

    protected TagesaggregatRequestDto createRequestDto(final MessstelleAuswertungOptionsDTO options, final Zeitraum zeitraum) {
        final TagesaggregatRequestDto requestDto = new TagesaggregatRequestDto();
        requestDto.setMessquerschnittIds(options.getMqIds().stream().map(Integer::valueOf).toList());
        requestDto.setStartDate(LocalDate.of(zeitraum.start.getYear(), zeitraum.start.getMonthValue(), 1));
        requestDto.setEndDate(LocalDate.of(zeitraum.end.getYear(), zeitraum.end.getMonthValue(), zeitraum.end.atEndOfMonth().getDayOfMonth()));
        requestDto.setTagesTyp(options.getTagesTyp().getTagesaggregatTyp());

        return requestDto;
    }

    protected TagesaggregatResponseDto sendRequest(final TagesaggregatRequestDto requestDto) {
        final ResponseEntity<TagesaggregatResponseDto> response = messwerteApi.getMeanOfDailyAggregatesPerMQWithHttpInfo(requestDto).block();

        if (ObjectUtils.isEmpty(response) || ObjectUtils.isEmpty(response.getBody())) {
            log.error("Die Response beinhaltet keine Daten");
            throw new ResourceNotFoundException(ERROR_MESSAGE);
        }
        return response.getBody();
    }

    protected List<Zeitraum> calculateZeitraeume(final List<AuswertungsZeitraum> auswertungszeitraeume, final List<Integer> jahre) {
        final List<Zeitraum> result = new ArrayList<>();

        for (AuswertungsZeitraum auswertungsZeitraum : auswertungszeitraeume) {
            for (int jahr : jahre) {
                result.add(new Zeitraum(
                        YearMonth.of(jahr, auswertungsZeitraum.getZeitraumStart().getMonth()),
                        YearMonth.of(jahr, auswertungsZeitraum.getZeitraumEnd().getMonth()),
                        auswertungsZeitraum));
            }
        }
        return result;
    }

}
