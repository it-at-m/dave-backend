/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.services.messstelle.auswertung;

import de.muenchen.dave.domain.dtos.laden.LadeZaehldatenSteplineDTO;
import de.muenchen.dave.domain.dtos.laden.messwerte.LadeMesswerteListenausgabeDTO;
import de.muenchen.dave.domain.dtos.laden.messwerte.LadeProcessedMesswerteDTO;
import de.muenchen.dave.domain.dtos.messstelle.MessstelleOptionsDTO;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.MessstelleAuswertungDTO;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.MessstelleAuswertungOptionsDTO;
import de.muenchen.dave.domain.enums.AuswertungsZeitraum;
import de.muenchen.dave.domain.enums.ZaehldatenIntervall;
import de.muenchen.dave.domain.enums.Zeitauswahl;
import de.muenchen.dave.domain.enums.Zeitblock;
import de.muenchen.dave.services.messstelle.MessstelleService;
import de.muenchen.dave.services.messstelle.MesswerteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
@Service
@Slf4j
public class AuswertungService {

    private final MessstelleService messstelleService;
    private final MesswerteService messwerteService;

    private final ExcelService excelService;

    public List<MessstelleAuswertungDTO> getAllVisibleMessstellen() {
        return messstelleService.getAllVisibleMessstellenForAuswertungOrderByMstIdAsc();
    }

    public byte[] createAuswertungsfile(final MessstelleAuswertungOptionsDTO options) throws IOException {
        log.info("#createAuswertungsfile {}", options);
        LadeZaehldatenSteplineDTO zaehldatenStepline = null;
        if (options.getMstIds().size() == 1) {
            final String messstelleId = messstelleService.getMessstelleByMstId(options.getMstIds().stream().findFirst().get()).getId();
            final MessstelleOptionsDTO messstelleOptionsDTO = new MessstelleOptionsDTO();
            messstelleOptionsDTO.setFahrzeuge(options.getFahrzeuge());
            messstelleOptionsDTO.setTagesTyp(options.getTagesTyp());
            messstelleOptionsDTO.setMessquerschnittIds(options.getMqIds());
            messstelleOptionsDTO.setZeitauswahl(Zeitauswahl.TAGESWERT.getCapitalizedName());
            messstelleOptionsDTO.setZeitblock(Zeitblock.ZB_00_24);
            messstelleOptionsDTO.setIntervall(ZaehldatenIntervall.STUNDE_KOMPLETT);
            final var zeitraum = new ArrayList<LocalDate>();
            zeitraum.add(LocalDate.of(2017, 1, 1));
            zeitraum.add(LocalDate.of(2017, 12, 31));
            messstelleOptionsDTO.setZeitraum(zeitraum);
            final LadeProcessedMesswerteDTO ladeProcessedMesswerteDTO = messwerteService.ladeMesswerte(messstelleId, messstelleOptionsDTO);
            zaehldatenStepline = ladeProcessedMesswerteDTO.getZaehldatenStepline();

        }
        return excelService.createFile(zaehldatenStepline, options);
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
