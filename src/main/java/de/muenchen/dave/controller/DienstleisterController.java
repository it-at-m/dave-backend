package de.muenchen.dave.controller;

import de.muenchen.dave.domain.dtos.DienstleisterDTO;
import de.muenchen.dave.services.DienstleisterService;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/dienstleister")
public class DienstleisterController {

    private static final String REQUEST_PARAMETER_DIENSTLEISTER_KENNUNG = "kennung";
    private final DienstleisterService dienstleisterService;

    public DienstleisterController(final DienstleisterService dienstleisterService) {
        this.dienstleisterService = dienstleisterService;
    }

    /**
     * Rest-Endpunkt für das Speichern eines Dienstleister (für das Admin-Portal).
     *
     * @param dienstleisterDTO Die zu speichernder Dienstleister
     * @return Der gespeicherte Dienstleister als DTO
     */
    @PreAuthorize("hasRole(T(de.muenchen.dave.security.AuthoritiesEnum).FACHADMIN.name())")
    @RequestMapping(value = "/save", method = { RequestMethod.POST, RequestMethod.PUT }, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DienstleisterDTO> saveDienstleister(@RequestBody @NotNull final DienstleisterDTO dienstleisterDTO) {
        log.debug("Dienstleister speichern: {}", dienstleisterDTO);
        try {
            return ResponseEntity.ok(this.dienstleisterService.saveOrUpdateDienstleister(dienstleisterDTO));
        } catch (final Exception e) {
            log.error("Unerwarteter Fehler im DienstleisterController beim Speichern des Dienstleisters: {}", dienstleisterDTO, e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Es ist ein unerwarteter Fehler beim Speichern des Dienstleisters aufgetreten.");
        }
    }

    /**
     * Rest-Endpunkt zum Laden eines Dienstleister (für das Admin-Portal).
     *
     * @param dienstleisterkennung Kennung des Dienstleisters
     * @return Der gespeicherte Dienstleister als DTO
     */
    @PreAuthorize("hasRole(T(de.muenchen.dave.security.AuthoritiesEnum).FACHADMIN.name())")
    @GetMapping(value = "/getByDienstleisterkennung", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DienstleisterDTO> getDienstleisterByKennung(
            @RequestParam(value = REQUEST_PARAMETER_DIENSTLEISTER_KENNUNG) @NotNull final String dienstleisterkennung) {
        log.debug("Dienstleister laden: {}", dienstleisterkennung);
        try {
            return ResponseEntity.ok(this.dienstleisterService.loadDienstleisterByKennung(dienstleisterkennung));
        } catch (final Exception e) {
            log.error("Unerwarteter Fehler im DienstleisterController beim Laden des Dienstleisters mit Kennung: {}", dienstleisterkennung, e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Es ist ein unerwarteter Fehler beim Laden des Dienstleisters aufgetreten.");
        }
    }

    /**
     * Rest-Endpunkt zum Laden aller Dienstleister (für das Admin-Portal).
     *
     * @return Liste der gespeicherten Dienstleister als DTO
     */
    @PreAuthorize("hasRole(T(de.muenchen.dave.security.AuthoritiesEnum).FACHADMIN.name())")
    @GetMapping(value = "/getAll", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<DienstleisterDTO>> getAllDienstleister() {
        log.debug("Alle Dienstleister laden.");
        try {
            return ResponseEntity.ok(this.dienstleisterService.loadAllDienstleister());
        } catch (final Exception e) {
            log.error("Unerwarteter Fehler im DienstleisterController beim Laden der Dienstleister.", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Es ist ein unerwarteter Fehler beim Laden der Dienstleister aufgetreten.");
        }
    }

    /**
     * Rest-Endpunkt zum Laden aller aktiven Dienstleister (für das Admin-Portal).
     *
     * @return Liste der gespeicherten Dienstleister als DTO
     */
    @PreAuthorize("hasRole(T(de.muenchen.dave.security.AuthoritiesEnum).FACHADMIN.name())")
    @GetMapping(value = "/getAllActive", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<DienstleisterDTO>> getAllActiveDienstleister() {
        log.debug("Alle Dienstleister laden.");
        try {
            return ResponseEntity.ok(this.dienstleisterService.loadAllActiveDienstleister());
        } catch (final Exception e) {
            log.error("Unerwarteter Fehler im DienstleisterController beim Laden der Dienstleister.", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Es ist ein unerwarteter Fehler beim Laden der Dienstleister aufgetreten.");
        }
    }

    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole(T(de.muenchen.dave.security.AuthoritiesEnum).FACHADMIN.name())")
    public ResponseEntity<?> deleteDienstleister(@PathVariable(value = "id") final UUID id) {
        log.debug("Dienstleister löschen.");
        try {
            this.dienstleisterService.deleteDienstleister(id);
            return ResponseEntity.noContent().build();
        } catch (final Exception e) {
            log.error("Unerwarteter Fehler im DienstleisterController beim Löschen der Dienstleister.", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Es ist ein unerwarteter Fehler beim Löschen des Dienstleisters aufgetreten.");
        }
    }
}
