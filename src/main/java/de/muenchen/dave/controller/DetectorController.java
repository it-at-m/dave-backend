package de.muenchen.dave.controller;

import de.muenchen.dave.domain.dtos.bearbeiten.BackendIdDTO;
import de.muenchen.dave.domain.dtos.external.DetectionDTO;
import de.muenchen.dave.exceptions.BrokenInfrastructureException;
import de.muenchen.dave.exceptions.DataNotFoundException;
import de.muenchen.dave.services.persist.ExternalDetectorService;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@Slf4j
@Validated
@RequestMapping("/detector")
public class DetectorController {

    private final ExternalDetectorService externalDetectorService;

    public DetectorController(final ExternalDetectorService externalDetectorService) {
        this.externalDetectorService = externalDetectorService;
    }

    @PreAuthorize("hasRole(T(de.muenchen.dave.security.AuthoritiesEnum).FACHADMIN.name())")
    @PostMapping(value = "/saveDetection", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BackendIdDTO> saveDetection(@RequestBody @NotNull final DetectionDTO detection) {
        log.debug("Detection received: {}", detection);
        try {
            final BackendIdDTO backendIdDto = this.externalDetectorService.saveDetection(detection);
            log.debug("Der Messpunkt wurde erfolgreich gespeichert.");
            return ResponseEntity.ok(backendIdDto);
        } catch (final DataNotFoundException dnfe) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, dnfe.getMessage());
        } catch (final BrokenInfrastructureException bie) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasRole(T(de.muenchen.dave.security.AuthoritiesEnum).FACHADMIN.name())")
    @PostMapping(value = "/saveLatestDetections", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BackendIdDTO> saveLatestDetections(@RequestBody @NotNull final List<DetectionDTO> detections) {
        log.debug("Detection received: {}", detections);
        try {
            final BackendIdDTO backendIdDto = this.externalDetectorService.saveLastestDetections(detections);
            log.debug("Der Messpunkt wurde erfolgreich gespeichert.");
            return ResponseEntity.ok(backendIdDto);
        } catch (final DataNotFoundException dnfe) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, dnfe.getMessage());
        } catch (final BrokenInfrastructureException bie) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
