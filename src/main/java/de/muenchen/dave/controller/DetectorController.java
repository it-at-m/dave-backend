package de.muenchen.dave.controller;

import de.muenchen.dave.domain.dtos.external.DetectionDTO;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@Slf4j
@Validated
public class DetectorController {

    @PreAuthorize(
        "hasAnyRole(T(de.muenchen.dave.security.AuthoritiesEnum).FACHADMIN.name()," +
                " T(de.muenchen.dave.security.AuthoritiesEnum).EXTERNAL.name())"
    )
    @PostMapping(value = "/saveDetection", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> saveDetection(@RequestBody @NotNull final DetectionDTO detection) {
        log.debug("Detection received: {}", detection);
        try {
            //TODO: persist detection
            //final BackendIdDTO backendIdDto = this.externalZaehlungPersistierungsService.saveZaehlung(zaehlung);
            log.debug("Der Messpunkt wurde erfolgreich gespeichert.");
            return ResponseEntity.ok().build();
            //} catch (final DataNotFoundException dnfe) {
            //    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, dnfe.getMessage());
        } catch (final Exception bie) {
            //catch (final BrokenInfrastructureException bie) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
