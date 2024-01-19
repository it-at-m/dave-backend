/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.controller;

import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.dtos.laden.LadeProcessedZaehldatenDTO;
import de.muenchen.dave.exceptions.DataNotFoundException;
import de.muenchen.dave.services.messstelle.GanglinieService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Slf4j
@RestController
@AllArgsConstructor
public class MesswerteMessquerschnittController {

    private static final String REQUEST_PARAMETER_MESSSTELLE_ID = "messstelleId";

    private final GanglinieService ganglinieService;

    /**
     * Rest-Endpunkt zur Bereitstellung der Daten einer Messstelle für das Gangliniendiagramm.
     *
     * @param messstelleId Die Id der Zaehlung.
     * @return Die aufbereiteten Daten einer Messstelle für die Gangline im Frontend.
     */
    @PostMapping(value = "/lade-ganglinie", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public ResponseEntity<LadeProcessedZaehldatenDTO> ladeGanglinie(
            @RequestParam(value = REQUEST_PARAMETER_MESSSTELLE_ID) @NotEmpty final String messstelleId) {
        try {
            log.info("ladeGanglinie für Messstelle {} aufgerufen", messstelleId);
            final LadeProcessedZaehldatenDTO processedZaehldaten = ganglinieService.ladeGanglinie(messstelleId);
            log.info("laden der Daten abgeschlossen.");
            log.debug("Messdaten: {}", processedZaehldaten.toString());
            return ResponseEntity.ok(processedZaehldaten);
        } catch (final DataNotFoundException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        }
    }
}
