/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.services.messstelle.auswertung;

import de.muenchen.dave.domain.dtos.messstelle.auswertung.AuswertungResponse;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.MessstelleAuswertungDTO;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.MessstelleAuswertungOptionsDTO;
import de.muenchen.dave.geodateneai.gen.model.TagesaggregatResponseDto;
import de.muenchen.dave.services.messstelle.MessstelleService;
import de.muenchen.dave.services.messstelle.MesswerteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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
        if (CollectionUtils.isEmpty(options.getMstIds())) {
            throw new IllegalArgumentException("MstIds is empty");
        }
        final Map<Integer, List<AuswertungResponse>> auswertungen = messwerteService.ladeAuswertung(options);
        return excelService.createFile(auswertungen, options);
    }

    //    protected List<Messstelle> getMessstellenByMstids(final Set<String> mstIds) {
    //        final var messstellen = new ArrayList<Messstelle>();
    //        mstIds.forEach(mstId -> messstellen.add(messstelleService.getMessstelleByMstId(mstId)));
    //        return messstellen;
    //    }

    //    protected List<List<LocalDate>> calculateZeitraeume(final List<AuswertungsZeitraum> auswertungszeitraeume, final List<Integer> jahre) {
    //        final List<List<LocalDate>> result = new ArrayList<>();
    //        final List<Zeitraum> zeitraume = new ArrayList<>();
    //        auswertungszeitraeume.forEach(auswertungszeitraum -> jahre.forEach(jahr -> {
    //
    //            LocalDate end = auswertungszeitraum.getZeitraumEnd().withYear(jahr);
    //            if (AuswertungsZeitraum.FEBRUAR == auswertungszeitraum && jahr % 4 == 0) {
    //                end = end.withDayOfMonth(29);
    //            }
    //            result.add(List.of(auswertungszeitraum.getZeitraumStart().withYear(jahr), end));
    //        }));
    //
    //
    //        return result;
    //    }
    //
    //    protected List<Zeitraum> calculateZeitraeume2(final List<AuswertungsZeitraum> auswertungszeitraeume, final List<Integer> jahre) {
    //        final List<Zeitraum> result = new ArrayList<>();
    //
    //        for (AuswertungsZeitraum auswertungsZeitraum : auswertungszeitraeume) {
    //            for (int jahr: jahre) {
    //                result.add(new Zeitraum(
    //                        YearMonth.of(jahr, auswertungsZeitraum.getZeitraumStart().getMonth()),
    //                        YearMonth.of(jahr, auswertungsZeitraum.getZeitraumEnd().getMonth())));
    //            }
    //        }
    //        return result;
    //    }
    //
    //    @Data
    //    @AllArgsConstructor
    //    protected static class Zeitraum {
    //        YearMonth start;
    //        YearMonth end;
    //    }
}
