package de.muenchen.dave.controller;

import de.muenchen.dave.domain.dtos.ZaehlstelleKarteDTO;
import de.muenchen.dave.domain.dtos.suche.SucheComplexSuggestsDTO;
import de.muenchen.dave.exceptions.ResourceNotFoundException;
import de.muenchen.dave.services.SucheService;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@Slf4j
@PreAuthorize("hasAnyRole(T(de.muenchen.dave.security.AuthoritiesEnum).ANWENDER.name()," +
        " T(de.muenchen.dave.security.AuthoritiesEnum).POWERUSER.name()," +
        " T(de.muenchen.dave.security.AuthoritiesEnum).FACHADMIN.name())")
public class SucheController {

    private static final String REQUEST_PARAMETER_QUERY = "query";
    private static final String REQUEST_PARAMETER_NOFILTER = "nofilter";

    private final SucheService sucheService;

    public SucheController(final SucheService sucheService) {
        this.sucheService = sucheService;
    }

    @GetMapping(value = "/suggest-datenportal", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public ResponseEntity<SucheComplexSuggestsDTO> suggestDatenportal(@RequestParam(value = REQUEST_PARAMETER_QUERY) final String query,
            @RequestParam(value = REQUEST_PARAMETER_NOFILTER, defaultValue = "false") final boolean noFilter) {
        try {
            final SucheComplexSuggestsDTO sucheComplexSuggestsDTO = this.sucheService.complexSuggestSichtbarDatenportal(query, noFilter);
            return new ResponseEntity<>(sucheComplexSuggestsDTO, HttpStatus.OK);
        } catch (final ResourceNotFoundException e) {
            log.error("Fehler im SucheController beim suggest der Query: {}", query, e);
            throw e;
        } catch (final Exception e) {
            log.error("Unerwarteter Fehler im SucheController beim suggest der Query: {}", query, e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Es ist ein unerwarteter Fehler bei der Suche nach Vorschlägen aufgetreten.");
        }
    }

    @GetMapping(value = "/suggest", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public ResponseEntity<SucheComplexSuggestsDTO> suggest(@RequestParam(value = REQUEST_PARAMETER_QUERY) final String query,
            @RequestParam(value = REQUEST_PARAMETER_NOFILTER, defaultValue = "false") final boolean noFilter) {
        try {
            final SucheComplexSuggestsDTO sucheComplexSuggestsDTO = this.sucheService.complexSuggest(query, noFilter);
            return new ResponseEntity<>(sucheComplexSuggestsDTO, HttpStatus.OK);
        } catch (final ResourceNotFoundException e) {
            log.error("Fehler im SucheController beim suggest der Query: {}", query, e);
            throw e;
        } catch (final Exception e) {
            log.error("Unerwarteter Fehler im SucheController beim suggest der Query: {}", query, e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Es ist ein unerwarteter Fehler bei der Suche nach Vorschlägen aufgetreten.");
        }
    }

    @GetMapping(value = "/search-datenportal", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public ResponseEntity<Set<ZaehlstelleKarteDTO>> searchZaehlstelleForMapDatenportal(@RequestParam(value = REQUEST_PARAMETER_QUERY) final String query,
            @RequestParam(value = REQUEST_PARAMETER_NOFILTER, defaultValue = "false") final boolean noFilter) {
        try {
            final Set<ZaehlstelleKarteDTO> zaehlstellenForMap = this.sucheService.sucheZaehlstelleSichtbarDatenportal(query, noFilter);
            return new ResponseEntity<>(zaehlstellenForMap, HttpStatus.OK);
        } catch (final ResourceNotFoundException e) {
            log.error("Fehler im SucheController beim Suchen der Query: {}", query, e);
            throw e;
        } catch (final Exception e) {
            log.error("Unerwarteter Fehler im SucheController beim Suchen der Query: {}", query, e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Es ist ein unerwarteter Fehler beim Laden der Zählstellen aufgetreten.");
        }
    }

    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public ResponseEntity<Set<ZaehlstelleKarteDTO>> searchZaehlstelleForMap(@RequestParam(value = REQUEST_PARAMETER_QUERY) final String query,
            @RequestParam(value = REQUEST_PARAMETER_NOFILTER, defaultValue = "false") final boolean noFilter) {
        try {
            final Set<ZaehlstelleKarteDTO> zaehlstellenForMap = this.sucheService.sucheZaehlstelle(query, noFilter);
            return new ResponseEntity<>(zaehlstellenForMap, HttpStatus.OK);
        } catch (final ResourceNotFoundException e) {
            log.error("Fehler im SucheController beim Suchen der Query: {}", query, e);
            throw e;
        } catch (final Exception e) {
            log.error("Unerwarteter Fehler im SucheController beim Suchen der Query: {}", query, e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Es ist ein unerwarteter Fehler beim Laden der Zählstellen aufgetreten.");
        }
    }
}
