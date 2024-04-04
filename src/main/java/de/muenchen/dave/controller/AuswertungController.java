package de.muenchen.dave.controller;

import de.muenchen.dave.domain.dtos.messstelle.auswertung.MessstelleAuswertungDTO;
import de.muenchen.dave.services.messstelle.auswertung.AuswertungService;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping(value = "/auswertung")
@RestController
@Slf4j
@AllArgsConstructor
@PreAuthorize("hasAnyRole(T(de.muenchen.dave.security.AuthoritiesEnum).FACHADMIN.name(), " +
        "T(de.muenchen.dave.security.AuthoritiesEnum).POWERUSER.name())")
public class AuswertungController {

    private final AuswertungService auswertungService;

    @GetMapping(value = "/messstelle/getAllVisibleMessstellen")
    public ResponseEntity<List<MessstelleAuswertungDTO>> getAllVisibleMessstellen() {
        log.info("#getAllVisibleMessstellen");
        final List<MessstelleAuswertungDTO> dto = auswertungService.getAllVisibleMessstellen();
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }
}
