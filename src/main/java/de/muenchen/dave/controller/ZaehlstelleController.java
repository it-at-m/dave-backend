package de.muenchen.dave.controller;

import de.muenchen.dave.domain.dtos.LeseZaehlstelleDTO;
import de.muenchen.dave.domain.dtos.NextZaehlstellennummerDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BackendIdDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteZaehlstelleDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehlstelleWithUnreadMessageDTO;
import de.muenchen.dave.exceptions.BrokenInfrastructureException;
import de.muenchen.dave.exceptions.DataNotFoundException;
import de.muenchen.dave.exceptions.ResourceNotFoundException;
import de.muenchen.dave.security.SecurityContextInformationExtractor;
import de.muenchen.dave.services.ZaehlstelleIndexService;
import java.util.List;
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

@RequestMapping(value = "/zaehlstelle")
@RestController
@Slf4j
@PreAuthorize("hasAnyRole(T(de.muenchen.dave.security.AuthoritiesEnum).ANWENDER.name(), " +
        "T(de.muenchen.dave.security.AuthoritiesEnum).POWERUSER.name())")
public class ZaehlstelleController {

    private static final String REQUEST_PARAMETER_ID = "id";
    private static final String REQUEST_PARAMETER_PARTICIPANT = "participant";

    private final ZaehlstelleIndexService indexService;

    public ZaehlstelleController(final ZaehlstelleIndexService indexService) {
        this.indexService = indexService;
    }

    @GetMapping(value = "/byId", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public ResponseEntity<LeseZaehlstelleDTO> getZaehlstelleHeader(@RequestParam(value = REQUEST_PARAMETER_ID) final String zaehlstelleId) {
        try {
            final LeseZaehlstelleDTO dto = this.indexService.readZaehlstelleDTO(zaehlstelleId, SecurityContextInformationExtractor.isFachadmin());
            return ResponseEntity.ok(dto);
        } catch (final ResourceNotFoundException e) {
            log.error("Fehler im ZaehlstellenController, Zählstelle konnte nicht gefunden werden. ID: {}", zaehlstelleId, e);
            throw e;
        } catch (final Exception e) {
            log.error("Unerwarteter Fehler im ZaehlstellenController beim Laden der Zählstelle mit der ID: {}", zaehlstelleId, e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Es ist ein unerwarteter Fehler beim Laden der Zählstellen aufgetreten.");
        }
    }

    @GetMapping(value = "/nextZaehlstellennummer", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole(T(de.muenchen.dave.security.AuthoritiesEnum).FACHADMIN.name())")
    public ResponseEntity<NextZaehlstellennummerDTO> getNextCurrentNumber(@RequestParam(value = REQUEST_PARAMETER_ID) @NotNull final String partOfzaehlstelleId,
            @RequestParam(value = "stadtbezirksnummer") @NotNull final Integer stadtbezirksnummer) {
        try {
            final NextZaehlstellennummerDTO nextCurrentNumber = this.indexService.getNextZaehlstellennummer(partOfzaehlstelleId, stadtbezirksnummer);
            return ResponseEntity.ok(nextCurrentNumber);
        } catch (final ResourceNotFoundException e) {
            log.error("Fehler im ZaehlstellenController, Zählstelle konnte nicht gefunden werden. ID: {}", partOfzaehlstelleId, e);
            throw e;
        } catch (final Exception e) {
            log.error("Unerwarteter Fehler im ZaehlstellenController beim Laden der Zählstelle mit der ID: {}", partOfzaehlstelleId, e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Es ist ein unerwarteter Fehler beim Laden der Zählstellen aufgetreten.");
        }
    }

    /**
     * Diese Methode erlaubt das initiale Persistieren einer Zaehlstelle.
     *
     * @param zaehlstelle zum initialen Persistieren.
     * @return die eindeutige Backend-ID der Zaehlstelle.
     */
    @PreAuthorize("hasRole(T(de.muenchen.dave.security.AuthoritiesEnum).FACHADMIN.name())")
    @PostMapping(value = "/save", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BackendIdDTO> saveZaehlstelle(@RequestBody @NotNull final BearbeiteZaehlstelleDTO zaehlstelle) {
        log.debug("Zaehlstelle speichern: {}", zaehlstelle);
        try {
            final BackendIdDTO backendIdDTO = this.indexService.speichereZaehlstelle(zaehlstelle);
            return ResponseEntity.ok(backendIdDTO);
        } catch (final BrokenInfrastructureException bie) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (final DataNotFoundException dataNotFoundException) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole(T(de.muenchen.dave.security.AuthoritiesEnum).FACHADMIN.name())")
    @GetMapping(value = "/editZaehlstelle", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BearbeiteZaehlstelleDTO> getEditZaehlstelle(@RequestParam(value = REQUEST_PARAMETER_ID) final String zaehlstelleId) {
        try {
            final BearbeiteZaehlstelleDTO dto = this.indexService.readEditZaehlstelleDTO(zaehlstelleId);
            return ResponseEntity.ok(dto);
        } catch (final ResourceNotFoundException e) {
            log.error("Fehler im ZaehlstellenController, Zählstelle konnte nicht gefunden werden. ID: {}", zaehlstelleId, e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } catch (final Exception e) {
            log.error("Unerwarteter Fehler im ZaehlstellenController beim Laden der Zählstelle mit der ID: {}", zaehlstelleId, e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Es ist ein unerwarteter Fehler beim Laden der Zählstellen aufgetreten.");
        }
    }

    /**
     * Gibt die Zählstellen zurück, für die für einen bestimmten Participant ungelesene Nachrichten
     * vorliegen
     *
     * @param participantId Participant bei dem ungelesene Nachrichten gefunden werden sollen
     * @return Zählstellen mit ungelesenen Nachrichten für bestimmten Participant
     */
    @PreAuthorize("hasAnyRole(T(de.muenchen.dave.security.AuthoritiesEnum).FACHADMIN.name()," +
            " T(de.muenchen.dave.security.AuthoritiesEnum).EXTERNAL.name())")
    @GetMapping(value = "/byUnreadMessages", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public ResponseEntity<List<LadeZaehlstelleWithUnreadMessageDTO>> getZaehlstellenByUnreadMessages(
            @RequestParam(value = REQUEST_PARAMETER_PARTICIPANT) final int participantId) {
        try {
            final List<LadeZaehlstelleWithUnreadMessageDTO> dto = this.indexService.readZaehlstellenWithUnreadMessages(participantId);
            return ResponseEntity.ok(dto);
        } catch (final ResourceNotFoundException e) {
            log.error("Fehler im ZaehlstellenController, Zählstellen mit ungelesenen Nachrichten konnten nicht gefunden werden. ParticipantId: {}",
                    participantId, e);
            throw e;
        } catch (final Exception e) {
            log.error("Unerwarteter Fehler im ZaehlstellenController beim Laden der Zählstellen mit ungelesenen Nachrichten. ParticipantId: {}", participantId,
                    e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Es ist ein unerwarteter Fehler beim Laden der Zählstellen mit ungelesenen Nachrichten aufgetreten.");
        }
    }
}
