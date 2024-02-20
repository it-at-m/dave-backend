/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.controller;

import de.muenchen.dave.domain.dtos.laden.LadeProcessedZaehldatenDTO;
import de.muenchen.dave.services.messstelle.MesswerteService;
import javax.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@AllArgsConstructor
public class MesswerteMessquerschnittController {

    private static final String REQUEST_PARAMETER_MESSSTELLE_ID = "messstelle_id";

    private final MesswerteService messwerteService;

    /**
     * Rest-Endpunkt zur Bereitstellung der Daten einer Messstelle für das Gangliniendiagramm,
     * die Heatmap und die Listenausgabe.
     *
     * @param messstelleId Die Id der Messstelle.
     * @return Die aufbereiteten Daten einer Messstelle für die Gangline im Frontend.
     */
    @GetMapping(value = "/lade-messwerte", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public ResponseEntity<LadeProcessedZaehldatenDTO> ladeMesswerte(
            @RequestParam(value = REQUEST_PARAMETER_MESSSTELLE_ID) @NotEmpty final String messstelleId) {
        log.info("ladeMEsswerte für Messstelle {} aufgerufen", messstelleId);
        final LadeProcessedZaehldatenDTO processedZaehldaten = messwerteService.ladeMesswerte(messstelleId);
        log.info("Laden der Daten abgeschlossen.");
        log.debug("Messdaten: {}", processedZaehldaten.toString());
        return ResponseEntity.ok(processedZaehldaten);
    }
}
