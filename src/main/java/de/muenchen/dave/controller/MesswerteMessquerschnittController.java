package de.muenchen.dave.controller;

import de.muenchen.dave.domain.dtos.laden.messwerte.LadeProcessedMesswerteDTO;
import de.muenchen.dave.domain.dtos.messstelle.MessstelleOptionsDTO;
import de.muenchen.dave.services.messstelle.MesswerteService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@AllArgsConstructor
public class MesswerteMessquerschnittController {

    private static final String REQUEST_PARAMETER_MESSSTELLE_ID = "messstelle_id";

    private final MesswerteService messwerteService;

    /**
     * Rest-Endpunkt zur Bereitstellung der Daten einer Messstelle für das Gangliniendiagramm, die
     * Heatmap und die Listenausgabe.
     *
     * @param messstelleId Die Id der Messstelle.
     * @return Die aufbereiteten Daten einer Messstelle für die Gangline im Frontend.
     */
    @PostMapping(value = "/lade-messwerte", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public ResponseEntity<LadeProcessedMesswerteDTO> ladeMesswerte(
            @RequestParam(value = REQUEST_PARAMETER_MESSSTELLE_ID) @NotEmpty final String messstelleId,
            @Valid @RequestBody @NotNull final MessstelleOptionsDTO options) {
        log.info("ladeMEsswerte für Messstelle {} aufgerufen", messstelleId);
        final LadeProcessedMesswerteDTO processedZaehldaten = messwerteService.ladeMesswerte(messstelleId, options);
        log.info("Laden der Daten abgeschlossen.");
        log.debug("Messdaten: {}", processedZaehldaten.toString());
        return ResponseEntity.ok(processedZaehldaten);
    }
}
