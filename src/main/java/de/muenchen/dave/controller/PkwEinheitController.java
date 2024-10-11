/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2021
 */
package de.muenchen.dave.controller;

import de.muenchen.dave.domain.dtos.PkwEinheitDTO;
import de.muenchen.dave.exceptions.DataNotFoundException;
import de.muenchen.dave.services.PkwEinheitService;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/pkw-einheit")
public class PkwEinheitController {

    private final PkwEinheitService pkwEinheitService;

    public PkwEinheitController(final PkwEinheitService pkwEinheitService) {
        this.pkwEinheitService = pkwEinheitService;
    }

    @PostMapping(value = "/save", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole(T(de.muenchen.dave.security.AuthoritiesEnum).FACHADMIN.name())")
    public ResponseEntity<PkwEinheitDTO> savePkwEinheit(@RequestBody @NotNull final PkwEinheitDTO pkwEinheitDto) {
        return ResponseEntity.ok(
                pkwEinheitService.savePkwEinheit(pkwEinheitDto));
    }

    @GetMapping(value = "/latest", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize(
        "hasAnyRole(T(de.muenchen.dave.security.AuthoritiesEnum).ANWENDER.name(), " +
                "T(de.muenchen.dave.security.AuthoritiesEnum).POWERUSER.name(), " +
                "T(de.muenchen.dave.security.AuthoritiesEnum).FACHADMIN.name())"
    )
    public ResponseEntity<PkwEinheitDTO> getLatestPkwEinheiten() {
        try {
            return ResponseEntity.ok(
                    pkwEinheitService.getLatestPkwEinheiten());
        } catch (final DataNotFoundException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        }
    }

}
