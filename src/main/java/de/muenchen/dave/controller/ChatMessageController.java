package de.muenchen.dave.controller;

import de.muenchen.dave.domain.dtos.ChatMessageDTO;
import de.muenchen.dave.exceptions.BrokenInfrastructureException;
import de.muenchen.dave.exceptions.ResourceNotFoundException;
import de.muenchen.dave.services.ChatMessageService;
import java.util.List;
import java.util.UUID;
import jakarta.validation.constraints.NotNull;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController
@RequestMapping("/chat-message")
public class ChatMessageController {

    private static final String REQUEST_PARAMETER_ZAEHLUNG_ID = "zaehlungId";
    private static final String CALLING_PARTICIPANT_ID = "callingParticipantId";
    private final ChatMessageService chatMessageService;

    public ChatMessageController(final ChatMessageService chatMessageService) {
        this.chatMessageService = chatMessageService;
    }

    /**
     * Rest-Endpunkt für das Laden von Chat Nachrichten zu einer Zählung (für das Upload- und
     * Admin-Portal).
     *
     * @param zaehlungId Die ID der Zählung
     * @return Eine Liste der Chat Nachrichten
     */
    @PreAuthorize("hasAnyRole(T(de.muenchen.dave.security.AuthoritiesEnum).FACHADMIN.name()," +
            " T(de.muenchen.dave.security.AuthoritiesEnum).EXTERNAL.name())")
    @GetMapping(value = "/allChatMessagesByZaehlungId", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public ResponseEntity<List<ChatMessageDTO>> getAllChatMessagesByZaehlungId(@RequestParam(value = REQUEST_PARAMETER_ZAEHLUNG_ID) String zaehlungId) {
        log.debug("Lade ChatMessages zur Zaehlung ID: {}", zaehlungId);
        try {
            return ResponseEntity.ok(chatMessageService.loadChatMessages(UUID.fromString(zaehlungId)));
        } catch (ResourceNotFoundException e) {
            log.error("Fehler im ChatMessageController, ChatMessages konnten nicht gefunden werden. ID der Zählung: {}", zaehlungId, e);
            throw e;
        } catch (Exception e) {
            log.error("Unerwarteter Fehler im ChatMessageController beim Laden der ChatMessages mit der Zählung ID: {}", zaehlungId, e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Es ist ein unerwarteter Fehler beim Laden der ChatMessages aufgetreten.");
        }
    }

    /**
     * Rest-Endpunkt für das Speichern einer Chat Nachricht (für das Upload- und Admin-Portal).
     *
     * @param chatMessageDTO Die zu speichernde Nachricht
     * @return Das gespeicherte ChatMessageDTO
     */
    @PreAuthorize("hasAnyRole(T(de.muenchen.dave.security.AuthoritiesEnum).FACHADMIN.name()," +
            " T(de.muenchen.dave.security.AuthoritiesEnum).EXTERNAL.name())")
    @PostMapping(value = "/save", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ChatMessageDTO> saveChatMessage(@RequestBody @NotNull final ChatMessageDTO chatMessageDTO) {
        log.debug("Neue ChatMessage speichern: {}", chatMessageDTO.getContent());
        try {
            return ResponseEntity.ok(chatMessageService.saveChatMessage(chatMessageDTO));
        } catch (Exception e) {
            log.error("Unerwarteter Fehler im ChatMessageController beim Speichern der ChatMessage: {}", chatMessageDTO, e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Es ist ein unerwarteter Fehler beim Speichern der ChatMessage aufgetreten.");
        }
    }

    /**
     * Rest-Endpunkt um den Status ungelesener Nachrichten zu updaten.
     *
     * @param zaehlungId Zählungs-ID, in der der Nachrichtenstatus geupdated werden soll.
     * @param participantId Participant-ID, für den der Nachrichtenstatus geupdated werden soll.
     * @return Die geupdateten ChatMessages
     */
    @PreAuthorize("hasAnyRole(T(de.muenchen.dave.security.AuthoritiesEnum).FACHADMIN.name()," +
            " T(de.muenchen.dave.security.AuthoritiesEnum).EXTERNAL.name())")
    @GetMapping(value = "/updateUnreadMessages", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity updateUnreadMessages(@RequestParam(value = REQUEST_PARAMETER_ZAEHLUNG_ID) final String zaehlungId,
            @RequestParam(value = CALLING_PARTICIPANT_ID) final Integer participantId) {
        log.debug("Update der unread Messages in Zählung {} für Participant {}", zaehlungId, participantId);
        try {
            chatMessageService.updateUnreadMessages(zaehlungId, participantId);
            return ResponseEntity.noContent().build();
        } catch (BrokenInfrastructureException brokenInfrastructureException) {
            log.error("Fehler im ChatMessageController, UnreadMessages Status konnte nicht gesetzt werden: {}", zaehlungId, brokenInfrastructureException);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Es ist ein unerwarteter Fehler beim setzen der nicht gelesenene Nachrichten aufgetreten.");
        }
    }

}
