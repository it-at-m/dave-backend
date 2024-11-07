/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.services.messstelle.auswertung;

import de.muenchen.dave.configuration.LogExecutionTime;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.AuswertungMessquerschnitte;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.MessstelleAuswertungDTO;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.MessstelleAuswertungOptionsDTO;
import de.muenchen.dave.domain.elasticsearch.detektor.Messquerschnitt;
import de.muenchen.dave.domain.enums.AuswertungsZeitraum;
import de.muenchen.dave.domain.mapper.detektor.AuswertungMapper;
import de.muenchen.dave.services.messstelle.MessstelleService;
import de.muenchen.dave.services.messstelle.MesswerteService;
import de.muenchen.dave.services.messstelle.Zeitraum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
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

    @LogExecutionTime
    public byte[] createAuswertungsfile(final MessstelleAuswertungOptionsDTO options) throws IOException {
        log.info("#createAuswertungsfile {}", options);
        if (CollectionUtils.isEmpty(options.getMstIds())) {
            throw new IllegalArgumentException("MstIds is empty");
        }
        final var auswertungenMqByMstId = this.ladeAuswertungGroupedByMstId(options);
        return spreadsheetService.createFile(auswertungenMqByMstId, options);
    }

    protected Map<Integer, List<AuswertungMessquerschnitte>> ladeAuswertungGroupedByMstId(final MessstelleAuswertungOptionsDTO options) {

        final List<Zeitraum> zeitraeume = this.createZeitraeume(options.getZeitraum(), options.getJahre());

        // AuswertungResponse extends TagesaggregatResponseDto für Januar
        // Liste für MQ
        // Wert Messstelle 1

        // AuswertungResponse extends TagesaggregatResponseDto für Januar
        // Liste für MQ
        // Wert Messstelle 2

        // AuswertungResponse extends TagesaggregatResponseDto für Februar
        // Liste für MQ
        // Wert Messstelle 1

        // AuswertungResponse extends TagesaggregatResponseDto für Februar
        // Liste für MQ
        // Wert Messstelle 2

        // AuswertungResponse extends TagesaggregatResponseDto für Januar und Februar
        // Liste für MQ
        // Wert Messstelle

        return CollectionUtils.emptyIfNull(options.getMstIds())
                .parallelStream()
                .flatMap(mstId -> CollectionUtils.emptyIfNull(zeitraeume)
                        .parallelStream()
                        .map(zeitraum -> {
                            // Holen der Messquerschnitte aus Messstelle für Options.
                            final var messstelle = messstelleService.getMessstelleByMstId(mstId);
                            final var mqIds = ListUtils.emptyIfNull(messstelle.getMessquerschnitte())
                                    .stream()
                                    .filter(ObjectUtils::isNotEmpty)
                                    .map(Messquerschnitt::getMqId)
                                    .filter(ObjectUtils::isNotEmpty)
                                    .collect(Collectors.toSet());
                            options.setMqIds(mqIds);

                            // Extrahieren der Tagesaggregate für die Messquerschnitte
                            final var tagesaggregate = messwerteService.ladeTagesaggregate(options, zeitraum);
                            return auswertungMapper.tagesaggregatDto2AuswertungResponse(tagesaggregate, zeitraum, mstId);
                        }))
                .collect(Collectors.groupingByConcurrent(AuswertungMessquerschnitte::getMstId));
    }

    protected List<Zeitraum> createZeitraeume(final List<AuswertungsZeitraum> auswertungszeitraeume, final List<Integer> jahre) {
        return ListUtils.emptyIfNull(auswertungszeitraeume)
                .stream()
                .flatMap(auswertungsZeitraum -> ListUtils.emptyIfNull(jahre)
                        .stream()
                        .map(jahr -> new Zeitraum(
                                YearMonth.of(jahr, auswertungsZeitraum.getZeitraumStart().getMonth()),
                                YearMonth.of(jahr, auswertungsZeitraum.getZeitraumEnd().getMonth()),
                                auswertungsZeitraum)))
                .toList();
    }
}
