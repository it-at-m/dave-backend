package de.muenchen.dave.controller;

import de.muenchen.dave.domain.dtos.StadtbezirkAndViertelDTO;
import de.muenchen.dave.services.StadtbezirkService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController
@RequestMapping("/stadtbezirk")
@RequiredArgsConstructor
public class StadtbezirkController {

    private final StadtbezirkService stadtbezirkService;

    /**
     * Rest-Endpunkt für das Laden der Stadtbezirke und -viertel.
     *
     * @return Liste von Stadtbezirken und -vierteln
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public ResponseEntity<StadtbezirkAndViertelDTO> getStadtbezirkeAndViertel(@RequestParam @NotNull final String stadt) {
        log.debug("#getStadtbezirkeAndViertel");
        try {
            return ResponseEntity.ok(stadtbezirkService.findStadtbezirkeAndViertelByStadt(stadt));
        } catch (Exception ex) {
            log.error("Unerwarteter Fehler im StadtbezirkController beim Laden der Stadtbezirke.", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Es ist ein unerwarteter Fehler beim Laden der Stadtbezirke aufgetreten.");
        }
    }

}
