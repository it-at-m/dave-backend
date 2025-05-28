package de.muenchen.dave.controller;

import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.dtos.laden.LadeProcessedZaehldatenDTO;
import de.muenchen.dave.exceptions.DataNotFoundException;
import de.muenchen.dave.services.processzaehldaten.ProcessZaehldatenService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@Slf4j
@RequiredArgsConstructor
@Validated
@PreAuthorize(
    "hasAnyRole(T(de.muenchen.dave.security.AuthoritiesEnum).ANWENDER.name(), " +
            "T(de.muenchen.dave.security.AuthoritiesEnum).POWERUSER.name())"
)
public class LadeZaehldatenController {

    private static final String REQUEST_PARAMETER_ZAEHLUNG_ID = "zaehlung_id";

    private final ProcessZaehldatenService processZaehldatenService;

    /**
     * Rest-Endpunkt zur Bereitstellung der Daten einer Zaehlung für alle Grafiken.
     *
     * @param zaehlungId Die Id der Zaehlung.
     * @param options Die im Frontend gewählten Optionen.
     * @return Die aufbereiteten Daten einer Zaehlung für die Diagramme im Frontend.
     */
    @PostMapping(value = "/lade-zaehldaten-processed", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public ResponseEntity<LadeProcessedZaehldatenDTO> ladeZaehldatenProcessed(
            @RequestParam(value = REQUEST_PARAMETER_ZAEHLUNG_ID) @NotEmpty final String zaehlungId,
            @Valid @RequestBody @NotNull final OptionsDTO options) {
        try {
            log.info("ladeZaehldatenProcessed für Zaehlung {} aufgerufen", zaehlungId);
            final LadeProcessedZaehldatenDTO processedZaehldaten = processZaehldatenService.ladeProcessedZaehldaten(
                    zaehlungId,
                    options);
            log.info("laden der Daten abgeschlossen.");
            log.debug("Zähldaten: {}", processedZaehldaten.toString());
            return ResponseEntity.ok(processedZaehldaten);
        } catch (final DataNotFoundException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        }
    }
}
