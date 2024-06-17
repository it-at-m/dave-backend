/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.services.messstelle.auswertung;

import de.muenchen.dave.domain.dtos.messstelle.auswertung.MessstelleAuswertungDTO;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.MessstelleAuswertungOptionsDTO;
import de.muenchen.dave.geodateneai.gen.api.MesswerteApi;
import de.muenchen.dave.services.messstelle.MessstelleService;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
@Slf4j
public class AuswertungService {

    private final MessstelleService messstelleService;
    private final MesswerteApi messwerteApi;

    public List<MessstelleAuswertungDTO> getAllVisibleMessstellen() {
        return messstelleService.getAllVisibleMessstellenForAuswertungOrderByMstIdAsc();
    }

    public void generateAuswertung(final MessstelleAuswertungOptionsDTO options) {
        log.info("#generateAuswertung for {}", options);
        // TODO load Data via Geodateneai
    }
}
