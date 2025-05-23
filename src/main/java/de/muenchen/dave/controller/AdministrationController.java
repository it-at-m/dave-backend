package de.muenchen.dave.controller;

import de.muenchen.dave.services.administration.ResetSuggestionService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RequestMapping(value = "/administration")
@RestController
@Slf4j
@AllArgsConstructor
@PreAuthorize("hasRole(T(de.muenchen.dave.security.AuthoritiesEnum).FACHADMIN.name())")
public class AdministrationController {

    private final ResetSuggestionService resetSuggestionService;

    @GetMapping(value = "/reset-suggestions/all", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity<?> resetAllSuggestions() {
        log.debug("#resetAllSuggestions");
        try {
            this.resetSuggestionService.resetAllSuggestions();
            return ResponseEntity.ok().build();
        } catch (final Exception e) {
            log.error("Unerwarteter Fehler im AdministrationController", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Es ist ein unerwarteter Fehler beim Zurücksetzen aller Vorschläge aufgetreten.");
        }
    }

    @GetMapping(value = "/reset-suggestions/zaehlstelle", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity<?> resetSuggestionsOfAllZaehlstellen() {
        log.debug("#resetSuggestionsOfAllZaehlstellen");
        try {
            this.resetSuggestionService.resetSuggestionsOfAllZaehlstellen();
            return ResponseEntity.ok().build();
        } catch (final Exception e) {
            log.error("Unerwarteter Fehler im AdministrationController", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Es ist ein unerwarteter Fehler beim Zurücksetzen der Zählstellen-Vorschläge aufgetreten.");
        }
    }

    @GetMapping(value = "/reset-suggestions/messstelle", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity<?> resetSuggestionsOfAllMessstellen() {
        log.debug("#resetSuggestionsOfAllMessstellen");
        try {
            this.resetSuggestionService.resetSuggestionsOfAllMessstellen();
            return ResponseEntity.ok().build();
        } catch (final Exception e) {
            log.error("Unerwarteter Fehler im AdministrationController", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Es ist ein unerwarteter Fehler beim Zurücksetzen der Messstellen-Vorschläge aufgetreten.");
        }
    }
}
