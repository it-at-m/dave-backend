package de.muenchen.dave.controller;

import de.muenchen.dave.domain.dtos.bearbeiten.BackendIdDTO;
import de.muenchen.dave.domain.dtos.messstelle.EditMessstelleDTO;
import de.muenchen.dave.domain.dtos.messstelle.ReadMessstelleDTO;
import de.muenchen.dave.exceptions.ResourceNotFoundException;
import de.muenchen.dave.services.messstelle.MessstelleService;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RequestMapping(value = "/messstelle")
@RestController
@Slf4j
@AllArgsConstructor
public class MessstelleController {

    private static final String REQUEST_PARAMETER_ID = "id";
    private final MessstelleService messstelleService;

    @PreAuthorize("hasAnyRole(T(de.muenchen.dave.security.AuthoritiesEnum).ANWENDER.name(), " +
            "T(de.muenchen.dave.security.AuthoritiesEnum).POWERUSER.name())")
    @GetMapping(value = "/byId", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public ResponseEntity<ReadMessstelleDTO> readMessstelle(@RequestParam(value = REQUEST_PARAMETER_ID) final String messstelleId) {
        log.debug("#readMessstelle with id {}", messstelleId);
        try {
            final ReadMessstelleDTO readMessstelleDTO = this.messstelleService.readMessstelleById(messstelleId);
            return ResponseEntity.ok(readMessstelleDTO);
        } catch (final ResourceNotFoundException e) {
            log.error("Fehler im MessstelleController, Messstelle konnte nicht gefunden werden. ID: {}", messstelleId, e);
            throw e;
        } catch (final Exception e) {
            log.error("Unerwarteter Fehler im MessstelleController beim Laden der Messstelle mit der ID: {}", messstelleId, e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Es ist ein unerwarteter Fehler beim Laden der Messstelle aufgetreten.");
        }
    }

    /**
     * Diese Methode erlaubt das Aktualisieren einer Messstelle.
     *
     * @param messstelle zum initialen Persistieren.
     * @return die eindeutige Backend-ID der Messstelle.
     */
    @PreAuthorize("hasRole(T(de.muenchen.dave.security.AuthoritiesEnum).FACHADMIN.name())")
    @PatchMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity<BackendIdDTO> updateMessstelle(@RequestBody @NotNull final EditMessstelleDTO messstelle) {
        log.debug("Messstelle speichern: {}", messstelle);
        final BackendIdDTO backendIdDTO = this.messstelleService.updateMessstelle(messstelle);
        return ResponseEntity.ok(backendIdDTO);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole(T(de.muenchen.dave.security.AuthoritiesEnum).FACHADMIN.name())")
    @GetMapping(value = "/edit", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EditMessstelleDTO> getBearbeiteMessstelle(@RequestParam(value = REQUEST_PARAMETER_ID) final String messstelleId) {
        log.debug("#getBearbeiteMessstelle with id {}", messstelleId);
        try {
            final EditMessstelleDTO dto = this.messstelleService.getMessstelleToEdit(messstelleId);
            return ResponseEntity.ok(dto);
        } catch (final ResourceNotFoundException e) {
            log.error("Fehler im MessstelleController, Messstelle konnte nicht gefunden werden. ID: {}", messstelleId, e);
            throw e;
        } catch (final Exception e) {
            log.error("Unerwarteter Fehler im MessstelleController beim Laden der Messstelle mit der ID: {}", messstelleId, e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Es ist ein unerwarteter Fehler beim Laden der Messstelle aufgetreten.");
        }
    }
}
