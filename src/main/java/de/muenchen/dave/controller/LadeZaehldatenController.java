/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.controller;

import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.dtos.laden.LadeBelastungsplanDTO;
import de.muenchen.dave.domain.dtos.laden.LadeProcessedZaehldatenDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatenZeitreiheDTO;
import de.muenchen.dave.exceptions.DataNotFoundException;
import de.muenchen.dave.services.processzaehldaten.ProcessZaehldatenBelastungsplanService;
import de.muenchen.dave.services.processzaehldaten.ProcessZaehldatenService;
import de.muenchen.dave.services.processzaehldaten.ProcessZaehldatenZeitreiheService;
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

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;


@RestController
@Slf4j
@Validated
@PreAuthorize("hasAnyRole(T(de.muenchen.dave.security.AuthoritiesEnum).ANWENDER.name(), " +
        "T(de.muenchen.dave.security.AuthoritiesEnum).POWERUSER.name())")
public class LadeZaehldatenController {

    private static final String REQUEST_PARAMETER_ZAEHLUNG_ID = "zaehlung_id";

    private static final String REQUEST_PARAMETER_ZAEHLSTELLE_ID = "zaehlstelle_id";

    private final ProcessZaehldatenBelastungsplanService processZaehldatenBelastungsplanService;

    private final ProcessZaehldatenService processZaehldatenService;

    private final ProcessZaehldatenZeitreiheService processZaehldatenZeitreiheService;

    public LadeZaehldatenController(final ProcessZaehldatenBelastungsplanService processZaehldatenBelastungsplanService,
                                    final ProcessZaehldatenService processZaehldatenService,
                                    final ProcessZaehldatenZeitreiheService processZaehldatenZeitreiheService) {
        this.processZaehldatenBelastungsplanService = processZaehldatenBelastungsplanService;
        this.processZaehldatenService = processZaehldatenService;
        this.processZaehldatenZeitreiheService = processZaehldatenZeitreiheService;
    }

    /**
     * Rest-Endpunkt zur Bereitstellung der Daten einer Zaehlung
     * für das Gangliniendiagramm, die Listenausgabe und für die Heatmap.
     *
     * @param zaehlungId Die Id der Zaehlung.
     * @param options    Die im Frontend gewählten Optionen.
     * @return Die aufbereiteten Daten einer Zaehlung für die Diagramme im Frontend.
     */
    @PostMapping(value = "/lade-zaehldaten-processed", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public ResponseEntity<LadeProcessedZaehldatenDTO> ladeZaehldatenProcessed(@RequestParam(value = REQUEST_PARAMETER_ZAEHLUNG_ID) @NotEmpty final String zaehlungId,
                                                                              @Valid @RequestBody @NotNull final OptionsDTO options) {
        try {
            log.info("ladeZaehldatenProcessed für Zaehlung {} aufgerufen", zaehlungId);
            final LadeProcessedZaehldatenDTO processedZaehldaten =
                    processZaehldatenService.ladeProcessedZaehldaten(
                            zaehlungId,
                            options);
            log.info("laden der Daten abgeschlossen.");
            log.debug("Zähldaten: {}", processedZaehldaten.toString());
            return ResponseEntity.ok(processedZaehldaten);
        } catch (final DataNotFoundException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        }
    }

    /**
     * Rest-Endpunkt zur Bereitstellung der Daten einer Zaehlung
     * für den Belastungsplan.
     *
     * @param zaehlungId Die Id der Zaehlung.
     * @param options    Die im Frontend gewählten Optionen.
     * @return Die aufbereiteten Daten einer Zaehlung für den Belastungsplan im Frontend.
     */
    @PostMapping(value = "/lade-belastungsplan", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public ResponseEntity<LadeBelastungsplanDTO> ladeBelastungsplan(@RequestParam(value = REQUEST_PARAMETER_ZAEHLUNG_ID) @NotEmpty final String zaehlungId,
                                                                    @Valid @RequestBody @NotNull final OptionsDTO options) {
        try {
            log.info("ladeBelastungsplan für Zaehlung {} aufgerufen", zaehlungId);
            final LadeBelastungsplanDTO ladeBelastungsplanDTO =
                    processZaehldatenBelastungsplanService.getBelastungsplanDTO(
                            zaehlungId,
                            options);
            log.info("laden der Daten abgeschlossen.");
            log.debug("Belastungsplandaten: {}", ladeBelastungsplanDTO);
            return ResponseEntity.ok(ladeBelastungsplanDTO);
        } catch (final DataNotFoundException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        }
    }

    /**
     * Rest-Endpunkt zur Bereitstellung der Daten einer Zählstelle für die Zeitreihe
     *
     * @param zaehlstelleId Die Id der Zaehlstelle.
     * @param zaehlungId    Die Id der im Frontend ausgewaehlten Zaehlung.
     * @param options       Die im Frontend gewählten Optionen.
     * @return Die aufbereiteten Daten aller Zählungen einer Zählstelle innerhalb eines bestimmten Zeitintervalls.
     */
    @PostMapping(value = "/lade-zeitreihe", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public ResponseEntity<LadeZaehldatenZeitreiheDTO> ladeZeitreihe(@RequestParam(value = REQUEST_PARAMETER_ZAEHLSTELLE_ID) @NotEmpty final String zaehlstelleId,
                                                                    @RequestParam(value = REQUEST_PARAMETER_ZAEHLUNG_ID) @NotEmpty final String zaehlungId,
                                                                    @Valid @RequestBody @NotNull final OptionsDTO options) {
        log.info("ladeZeitreihe für Zaehlstelle {} aufgerufen", zaehlstelleId);
        try {
            final LadeZaehldatenZeitreiheDTO ladeZaehldatenZeitreiheDTO =
                    processZaehldatenZeitreiheService.getZeitreiheDTO(
                            zaehlstelleId,
                            zaehlungId,
                            options
                    );
            log.info("laden der Zeitreihedaten abgeschlossen.");
            log.debug("Zeitreihedaten: {}", ladeZaehldatenZeitreiheDTO);
            return ResponseEntity.ok(ladeZaehldatenZeitreiheDTO);
        } catch (final DataNotFoundException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        }
    }
}
