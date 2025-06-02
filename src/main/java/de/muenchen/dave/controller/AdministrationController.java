package de.muenchen.dave.controller;

import de.muenchen.dave.domain.dtos.ResetAuffaelligkeitenDTO;
import de.muenchen.dave.services.messstelle.UnauffaelligeTageService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RequestMapping(value = "/administration")
@RestController
@Slf4j
@AllArgsConstructor
@PreAuthorize("hasRole(T(de.muenchen.dave.security.AuthoritiesEnum).FACHADMIN.name())")
public class AdministrationController {

    private final UnauffaelligeTageService unauffaelligeTageService;

    @PostMapping(value = "/reset-unauffaelliger-tag")
    @Transactional
    public ResponseEntity<Void> resetUnauffaelligerTag(@RequestBody @NotNull @Valid final ResetAuffaelligkeitenDTO reloadAuffaelligkeiten) {
        log.debug("#resetUnauffaelligerTag");
        try {
            this.unauffaelligeTageService.deleteAndReloadUnauffaelligerTagByDatum(reloadAuffaelligkeiten.getDateToReload());
            return ResponseEntity.noContent().build();
        } catch (final Exception e) {
            log.error("Unerwarteter Fehler im AdministrationController", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Es ist ein unerwarteter Fehler beim Laden des unauffälligen Tages %s aufgetreten.", reloadAuffaelligkeiten));
        }
    }
}
