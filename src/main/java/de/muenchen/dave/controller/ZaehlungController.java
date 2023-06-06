/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.controller;

import de.muenchen.dave.domain.dtos.DienstleisterDTO;
import de.muenchen.dave.domain.dtos.OpenZaehlungDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BackendIdDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteZaehlungDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.UpdateStatusDTO;
import de.muenchen.dave.domain.dtos.external.ExternalZaehlungDTO;
import de.muenchen.dave.domain.enums.Status;
import de.muenchen.dave.exceptions.BrokenInfrastructureException;
import de.muenchen.dave.exceptions.DataNotFoundException;
import de.muenchen.dave.exceptions.PlausibilityException;
import de.muenchen.dave.security.SecurityContextInformationExtractor;
import de.muenchen.dave.services.ChatMessageService;
import de.muenchen.dave.services.persist.ExternalZaehlungPersistierungsService;
import de.muenchen.dave.services.persist.InternalZaehlungPersistierungsService;
import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController
@RequestMapping("/zaehlung")
public class ZaehlungController {

    private static final String REQUEST_PARAMETER_ZAEHLSTELLE_ID = "zaehlstelle_id";
    private static final String REQUEST_PARAMETER_ZAEHLUNG_ID = "zaehlung_id";

    private final InternalZaehlungPersistierungsService internalZaehlungPersistierungsService;
    private final ExternalZaehlungPersistierungsService externalZaehlungPersistierungsService;
    private final ChatMessageService chatMessageService;

    public ZaehlungController(final InternalZaehlungPersistierungsService internalZaehlungPersistierungsService,
            final ExternalZaehlungPersistierungsService externalZaehlungPersistierungsService,
            final ChatMessageService chatMessageService) {
        this.internalZaehlungPersistierungsService = internalZaehlungPersistierungsService;
        this.externalZaehlungPersistierungsService = externalZaehlungPersistierungsService;
        this.chatMessageService = chatMessageService;
    }

    @GetMapping(value = "/loadAllOpenZaehlungen", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole(T(de.muenchen.dave.security.AuthoritiesEnum).FACHADMIN.name())")
    public ResponseEntity<List<OpenZaehlungDTO>> loadAllOpenZaehlungen() {
        log.debug("Lade alle offenen Zählungen");
        try {
            final List<OpenZaehlungDTO> openZaehlungDTOS = this.internalZaehlungPersistierungsService.loadAllOpenZaehlungen();
            log.debug("Alle offenene Zählungen wurden geladen");
            return ResponseEntity.ok(openZaehlungDTOS);
        } catch (final BrokenInfrastructureException bie) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(value = "/saveWithZeitintervall", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole(T(de.muenchen.dave.security.AuthoritiesEnum).FACHADMIN.name())")
    public ResponseEntity<BackendIdDTO> saveWithZeitintervall(@RequestBody @NotNull final BearbeiteZaehlungDTO zaehlung,
            @RequestParam(value = REQUEST_PARAMETER_ZAEHLSTELLE_ID) @NotNull final String zaehlstelleId) {
        log.debug("Zaehlung mit Zeitintervalle speichern: {}", zaehlung);
        try {
            final BackendIdDTO backendIdDto = this.internalZaehlungPersistierungsService.saveZaehlungWithZeitintervalle(zaehlung, zaehlstelleId);
            this.chatMessageService.saveUpdateMessageForZaehlungInternal(backendIdDto.getId());
            log.debug("Die Zaehlung wurde erfolgreich gespeichert.");
            return ResponseEntity.ok(backendIdDto);
        } catch (final DataNotFoundException dnfe) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, dnfe.getMessage());
        } catch (final BrokenInfrastructureException bie) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(value = "/save", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole(T(de.muenchen.dave.security.AuthoritiesEnum).FACHADMIN.name())")
    public ResponseEntity<BackendIdDTO> saveZaehlung(@RequestBody @NotNull final BearbeiteZaehlungDTO zaehlung,
            @RequestParam(value = REQUEST_PARAMETER_ZAEHLSTELLE_ID) @NotNull final String zaehlstelleId) {
        log.debug("Zaehlung speichern: {}", zaehlung);
        try {
            final BackendIdDTO backendIdDto = this.internalZaehlungPersistierungsService.saveZaehlung(zaehlung, zaehlstelleId);
            this.chatMessageService.saveUpdateMessageForZaehlungInternal(backendIdDto.getId());
            log.debug("Die Zaehlung wurde erfolgreich gespeichert.");
            return ResponseEntity.ok(backendIdDto);
        } catch (final DataNotFoundException dnfe) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, dnfe.getMessage());
        } catch (final BrokenInfrastructureException bie) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasRole(T(de.muenchen.dave.security.AuthoritiesEnum).FACHADMIN.name())")
    @GetMapping(value = "/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> deleteZaehlung(@RequestParam(value = REQUEST_PARAMETER_ZAEHLUNG_ID) @NotNull final String zaehlungId) {
        log.debug("Zaehlung löschen: {}", zaehlungId);
        try {
            final boolean isDeleted = this.internalZaehlungPersistierungsService.deleteZaehlung(zaehlungId);
            return ResponseEntity.ok(isDeleted);
        } catch (final DataNotFoundException dnfe) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, dnfe.getMessage());
        } catch (final BrokenInfrastructureException bie) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasAnyRole(T(de.muenchen.dave.security.AuthoritiesEnum).FACHADMIN.name()," +
            " T(de.muenchen.dave.security.AuthoritiesEnum).EXTERNAL.name())")
    @GetMapping(value = "/getZaehlungenForExternal", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ExternalZaehlungDTO>> getZaehlungenForExternal() {
        log.debug("Lade Zaehlungen für Dienstleister");
        try {
            return ResponseEntity.ok(this.externalZaehlungPersistierungsService.getZaehlungenForExternal(SecurityContextInformationExtractor.getUserName(),
                    SecurityContextInformationExtractor.isFachadmin()));
        } catch (final BrokenInfrastructureException bie) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasAnyRole(T(de.muenchen.dave.security.AuthoritiesEnum).FACHADMIN.name()," +
            " T(de.muenchen.dave.security.AuthoritiesEnum).EXTERNAL.name())")
    @PostMapping(value = "/saveExternal", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BackendIdDTO> saveExternalZaehlung(@RequestBody @NotNull final ExternalZaehlungDTO zaehlung) {
        log.debug("Externe Zaehlung speichern: {}", zaehlung);
        try {
            final BackendIdDTO backendIdDto = this.externalZaehlungPersistierungsService.saveZaehlung(zaehlung);
            this.chatMessageService.saveUpdateMessageForZaehlungExternal(backendIdDto.getId());
            log.debug("Die externe Zaehlung wurde erfolgreich gespeichert.");
            return ResponseEntity.ok(backendIdDto);
        } catch (final DataNotFoundException dnfe) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, dnfe.getMessage());
        } catch (final BrokenInfrastructureException bie) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Wird sowohl von intern, als auch von extern genutzt
    @PreAuthorize("hasAnyRole(T(de.muenchen.dave.security.AuthoritiesEnum).FACHADMIN.name()," +
            " T(de.muenchen.dave.security.AuthoritiesEnum).EXTERNAL.name())")
    @PostMapping(value = "/updateStatus", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BackendIdDTO> updateStatus(@RequestBody @NotNull final UpdateStatusDTO updateZaehlung) {
        try {
            log.debug("Status aktualisieren: {}", updateZaehlung);
            final BackendIdDTO backendIdDto = this.externalZaehlungPersistierungsService.updateStatus(updateZaehlung);
            this.chatMessageService.saveUpdateMessageForZaehlungStatus(backendIdDto.getId(), updateZaehlung);
            // Wenn eine Zaehlung beauftragt wird, deren Zaehldatum bereits in der Vergangeheit liegt, so wird der Status gleich auf COUNTING gesetzt
            if (updateZaehlung.getStatus().equalsIgnoreCase(Status.INSTRUCTED.name())) {
                this.externalZaehlungPersistierungsService.updateStatusIfDateIsInThePast(updateZaehlung, Status.COUNTING);
            }
            log.debug("Der Status wurde erfolgreich aktualisiert");
            return ResponseEntity.ok(backendIdDto);
        } catch (final DataNotFoundException | PlausibilityException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (final BrokenInfrastructureException bie) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(value = "/updateDienstleisterkennung", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole(T(de.muenchen.dave.security.AuthoritiesEnum).FACHADMIN.name())")
    public ResponseEntity<BackendIdDTO> updateDienstleisterkennung(@RequestBody @NotNull final DienstleisterDTO dienstleisterDTO,
            @RequestParam(value = REQUEST_PARAMETER_ZAEHLUNG_ID) @NotNull final String zaehlungId) {
        log.debug("Zaehlung {} Dienstleisterkennung aktualisieren: {}", zaehlungId, dienstleisterDTO);
        try {
            final BackendIdDTO backendIdDto = this.internalZaehlungPersistierungsService.updateDienstleisterkennung(zaehlungId, dienstleisterDTO);
            this.chatMessageService.saveUpdateMessageForZaehlungInternal(backendIdDto.getId());
            log.debug("Die Zaehlung wurde erfolgreich aktualisiert.");
            return ResponseEntity.ok(backendIdDto);
        } catch (final DataNotFoundException dnfe) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, dnfe.getMessage());
        } catch (final BrokenInfrastructureException bie) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
