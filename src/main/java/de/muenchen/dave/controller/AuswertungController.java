package de.muenchen.dave.controller;

import de.muenchen.dave.domain.dtos.messstelle.auswertung.MessstelleAuswertungDTO;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.MessstelleAuswertungOptionsDTO;
import de.muenchen.dave.services.messstelle.auswertung.AuswertungService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RequestMapping(value = "/auswertung")
@RestController
@Slf4j
@AllArgsConstructor
@PreAuthorize(
    "hasAnyRole(T(de.muenchen.dave.security.AuthoritiesEnum).FACHADMIN.name(), " +
            "T(de.muenchen.dave.security.AuthoritiesEnum).POWERUSER.name())"
)
public class AuswertungController {

    private final AuswertungService auswertungService;

    @GetMapping(value = "/messstelle/getAllVisibleMessstellen")
    public ResponseEntity<List<MessstelleAuswertungDTO>> getAllVisibleMessstellen() {
        log.info("#getAllVisibleMessstellen");
        final List<MessstelleAuswertungDTO> dto = auswertungService.getAllVisibleMessstellen();
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @PostMapping(value = "/messstelle", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> generateAuswertung(
            @Valid @RequestBody @NotNull final MessstelleAuswertungOptionsDTO options) {
        log.info("generateAuswertung für Messstellen {} aufgerufen", options.getMstIds());
        try {
            final byte[] file = auswertungService.createAuswertungsfile(options);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(file.length)
                    .body(file);
        } catch (Exception exception) {
            log.error("Unerwarteter Fehler im AuswertungsController beim Erstellen der Auswertung mit die messstellen: {}", options.getMstIds(), exception);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Es ist ein unerwarteter Fehler beim Erstellen der Auswertung aufgetreten.");
        }

    }
}
