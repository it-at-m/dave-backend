/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.services.messstelle.auswertung;

import de.muenchen.dave.domain.dtos.messstelle.auswertung.AuswertungResponse;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.MessstelleAuswertungDTO;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.MessstelleAuswertungOptionsDTO;
import de.muenchen.dave.domain.elasticsearch.detektor.Messstelle;
import de.muenchen.dave.domain.enums.AuswertungsZeitraum;
import de.muenchen.dave.domain.mapper.detektor.AuswertungMapper;
import de.muenchen.dave.services.messstelle.MessstelleService;
import de.muenchen.dave.services.messstelle.MesswerteService;
import de.muenchen.dave.services.messstelle.Zeitraum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class AuswertungService {

    private final MessstelleService messstelleService;

    private final MesswerteService messwerteService;

    private final AuswertungMapper auswertungMapper;

    private final SpreadsheetService spreadsheetService;

    public List<MessstelleAuswertungDTO> getAllVisibleMessstellen() {
        return messstelleService.getAllVisibleMessstellenForAuswertungOrderByMstIdAsc();
    }

    public byte[] createAuswertungsfile(final MessstelleAuswertungOptionsDTO options) throws IOException {
        log.info("#createAuswertungsfile {}", options);
        if (CollectionUtils.isEmpty(options.getMstIds())) {
            throw new IllegalArgumentException("MstIds is empty");
        }
        final Map<Integer, List<AuswertungResponse>> auswertungen = this.ladeAuswertung(options);
        return spreadsheetService.createFile(auswertungen, options);
    }

    public Map<Integer, List<AuswertungResponse>> ladeAuswertung(final MessstelleAuswertungOptionsDTO options) {

        final List<Zeitraum> zeitraums = this.calculateZeitraeume(options.getZeitraum(), options.getJahre());

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
                final var tagesaggregate = messwerteService.ladeTagesaggregate(options, zeitraum);
                final AuswertungResponse auswertungResponse = auswertungMapper.tagesaggregatDto2AuswertungResponse(tagesaggregate);
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
