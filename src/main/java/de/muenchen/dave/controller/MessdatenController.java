/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.controller;

import de.muenchen.dave.geodateneai.gen.model.MessdatenDto;
import de.muenchen.dave.services.MessdatenService;
import java.util.List;
import javax.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/messdaten")
@AllArgsConstructor
public class MessdatenController {

    private static final String REQUEST_PARAMETER_MESSSTELLE_ID = "messstelle_id";
    private static final String REQUEST_PARAMETER_VON = "von";
    private static final String REQUEST_PARAMETER_BIS = "bis";
    private static final String REQUEST_PARAMETER_TAGTYP = "tagesTyp";

    private final MessdatenService messdatenService;

    /**
     * Rest-Endpunkt zur Bereitstellung der Daten einer Messstelle
     * für das Gangliniendiagramm, die Listenausgabe und für die Heatmap.
     *
     * @param messstelleId Die Id der angefragten Messstelle.
     * @param von required Zeitpunkt der Daten.
     * @param bis optional Ende eines Zeitraums.
     * @param tagtyp Typ des Tages
     * @return Die aufbereiteten Daten einer Messstelle für die Diagramme im Frontend.
     */
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public ResponseEntity<List<MessdatenDto>> ladeMessdaten(
            @RequestParam(value = REQUEST_PARAMETER_MESSSTELLE_ID) @NotEmpty final long messstelleId,
            @RequestParam(value = REQUEST_PARAMETER_VON) @NotEmpty final String von,
            @RequestParam(value = REQUEST_PARAMETER_BIS) @NotEmpty final String bis,
            @RequestParam(value = REQUEST_PARAMETER_TAGTYP) final String tagtyp) {
        log.info("ladeMessdaten für Messstelle {} aufgerufen", messstelleId);
        final List<MessdatenDto> messdaten = messdatenService.ladeMessdaten(messstelleId, von, bis, tagtyp);
        log.info("laden der Daten abgeschlossen.");
        log.debug("Messdaten: {}", messdaten.toString());
        return ResponseEntity.ok(messdaten);
    }
}
