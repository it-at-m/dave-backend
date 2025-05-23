package de.muenchen.dave.controller;

import de.muenchen.dave.domain.dtos.InfoMessageDTO;
import de.muenchen.dave.exceptions.ResourceNotFoundException;
import de.muenchen.dave.services.InfoMessageService;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController
@RequestMapping("/infomessage")
public class InfoMessageController {

    private final InfoMessageService infoMessageService;

    public InfoMessageController(final InfoMessageService infoMessageService) {
        this.infoMessageService = infoMessageService;
    }

    /**
     * Rest-Endpunkt für das Laden aller InfoMessages.
     *
     * @return Die InfoMessage
     */
    @PreAuthorize("hasRole(T(de.muenchen.dave.security.AuthoritiesEnum).FACHADMIN.name())")
    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public ResponseEntity<List<InfoMessageDTO>> getAllInfoMessages() {
        log.debug("Laden aller aktuellen InfoMessages");
        try {
            return ResponseEntity.ok(infoMessageService.loadAllInfoMessages());
        } catch (ResourceNotFoundException e) {
            log.error("Fehler im InfoMessageController, InfoMessages konnten nicht gefunden werden.");
            throw e;
        } catch (Exception e) {
            log.error("Unerwarteter Fehler im InfoMessageController beim Laden der InfoMessages.");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Es ist ein unerwarteter Fehler beim Laden der Infonachricht aufgetreten.");
        }
    }

    /**
     * Rest-Endpunkt für das Laden der aktiven InfoMessage.
     *
     * @return Die InfoMessage
     */
    @PreAuthorize(
        "hasAnyRole(T(de.muenchen.dave.security.AuthoritiesEnum).ANWENDER.name()," +
                " T(de.muenchen.dave.security.AuthoritiesEnum).POWERUSER.name()," +
                " T(de.muenchen.dave.security.AuthoritiesEnum).FACHADMIN.name())"
    )
    @GetMapping(value = "/active", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public ResponseEntity<InfoMessageDTO> getActiveInfoMessage() {
        log.debug("Laden der aktuellen InfoMessage");
        try {
            return ResponseEntity.ok(infoMessageService.loadActiveInfoMessage());
        } catch (ResourceNotFoundException e) {
            log.error("Fehler im InfoMessageController, InfoMessage konnte nicht gefunden werden.");
            throw e;
        } catch (Exception e) {
            log.error("Unerwarteter Fehler im InfoMessageController beim Laden der InfoMessage ");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Es ist ein unerwarteter Fehler beim Laden der Infonachricht aufgetreten.");
        }
    }

    /**
     * Rest-Endpunkt für das Speichern einer InfoMessage.
     *
     * @param infoMessageDTO Die zu speichernde Nachricht
     * @return Das gespeicherte ChatMessageDTO
     */
    @PostMapping(value = "/save", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole(T(de.muenchen.dave.security.AuthoritiesEnum).FACHADMIN.name())")
    @Transactional()
    public ResponseEntity<List<InfoMessageDTO>> saveInfoMessage(@RequestBody @NotNull final InfoMessageDTO infoMessageDTO) {
        log.debug("Neue Infomessage speichern: {}", infoMessageDTO.getContent());
        try {
            return ResponseEntity.ok(infoMessageService.saveInfoMessage(infoMessageDTO));
        } catch (Exception e) {
            log.error("Unerwarteter Fehler im InfoMessageController beim Speichern der InfoMessage: {}", infoMessageDTO, e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Es ist ein unerwarteter Fehler beim Speichern der Infonachricht aufgetreten.");
        }
    }

    @PostMapping(value = "/set-all-inactive")
    @PreAuthorize("hasRole(T(de.muenchen.dave.security.AuthoritiesEnum).FACHADMIN.name())")
    public ResponseEntity<?> setAllInfoMessagesInactive() {
        infoMessageService.setAllInfoMessagesInactiveAndDeleteInactiveExceptAllowedInfoMessages();
        return ResponseEntity.noContent().build();
    }

}
