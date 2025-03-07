package de.muenchen.dave.controller;

import de.muenchen.dave.documentstorage.gen.model.DocumentDto;
import de.muenchen.dave.exceptions.ResourceNotFoundException;
import de.muenchen.dave.services.lageplan.LageplanService;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RequestMapping(value = "/lageplan")
@RestController
@Slf4j
@AllArgsConstructor
public class LageplanController {

    private static final String REQUEST_PARAMETER_MSTID = "mstId";
    private final LageplanService lageplanService;

    @PreAuthorize(
        "hasAnyRole(T(de.muenchen.dave.security.AuthoritiesEnum).ANWENDER.name(), " +
                "T(de.muenchen.dave.security.AuthoritiesEnum).POWERUSER.name())"
    )
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public ResponseEntity<DocumentDto> loadLageplan(@RequestParam(value = REQUEST_PARAMETER_MSTID) @NotBlank final String messstelleId) {
        log.debug("#loadLageplan with id {}", messstelleId);
        try {
            final DocumentDto documentDto = this.lageplanService.ladeLageplan(messstelleId);
            return ResponseEntity.ok(documentDto);
        } catch (final ResourceNotFoundException e) {
            log.error("Fehler beim Laden des Lageplans zur Messstelle: {}", messstelleId, e);
            throw e;
        } catch (final Exception e) {
            log.error("Fehler beim Laden des Lageplans zur Messstelle: {}", messstelleId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Es ist ein Fehler beim Laden des Lageplans zur Messstelle aufgetreten.");
        }
    }

}
