/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.services.messstelle.auswertung;

import de.muenchen.dave.domain.dtos.messstelle.auswertung.MessstelleAuswertungDTO;
import de.muenchen.dave.domain.enums.AuswertungsZeitraum;
import de.muenchen.dave.services.messstelle.MessstelleService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@AllArgsConstructor
@Service
@Slf4j
public class AuswertungService {

    private final MessstelleService messstelleService;

    public List<MessstelleAuswertungDTO> getAllVisibleMessstellen() {
        return messstelleService.getAllVisibleMessstellenForAuswertungOrderByMstIdAsc();
    }

    protected Map<String, Set<String>> calculateMessquerschnittIdsPerMessstelle(final List<String> mstIds, final List<String> mqIds) {
        final Map<String, Set<String>> result = new HashMap<>();
        if (mstIds.size() == 1) {
            // per MQ
            result.put(mstIds.get(0), Set.copyOf(mqIds));
        } else {
            // per Mst
            mstIds.forEach(mstId -> result.put(mstId, messstelleService.getMessquerschnittIds(mstId)));
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
