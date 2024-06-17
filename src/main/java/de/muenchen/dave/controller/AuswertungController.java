package de.muenchen.dave.controller;

import de.muenchen.dave.domain.dtos.CsvDTO;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.MessstelleAuswertungDTO;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.MessstelleAuswertungOptionsDTO;
import de.muenchen.dave.services.messstelle.auswertung.AuswertungService;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @PostMapping(value = "/messstelle", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CsvDTO> generateAuswertung(
            @Valid @RequestBody @NotNull final MessstelleAuswertungOptionsDTO options) {
        //        try {
        log.info("generateAuswertung für Messstellen {} aufgerufen", options.getMstIds());
        auswertungService.generateAuswertung(options);
        log.info("CSV wurde erstellt");
        // TODO Excel file zurückgeben
        return new ResponseEntity<>(HttpStatus.OK);
        //        } catch (final DataNotFoundException exception) {
        //            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        //        }

    }
}
