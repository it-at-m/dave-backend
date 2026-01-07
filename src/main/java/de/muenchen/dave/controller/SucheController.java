package de.muenchen.dave.controller;

import de.muenchen.dave.domain.dtos.ErhebungsstelleKarteDTO;
import de.muenchen.dave.domain.dtos.suche.SearchAndFilterOptionsDTO;
import de.muenchen.dave.domain.dtos.suche.SucheComplexSuggestsDTO;
import de.muenchen.dave.exceptions.ResourceNotFoundException;
import de.muenchen.dave.services.SucheService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@Slf4j
@PreAuthorize(
    "hasAnyRole(T(de.muenchen.dave.security.AuthoritiesEnum).ANWENDER.name()," +
            " T(de.muenchen.dave.security.AuthoritiesEnum).POWERUSER.name()," +
            " T(de.muenchen.dave.security.AuthoritiesEnum).FACHADMIN.name())"
)
public class SucheController {

    private static final String REQUEST_PARAMETER_QUERY = "query";

    private final SucheService sucheService;

    public SucheController(final SucheService sucheService) {
        this.sucheService = sucheService;
    }

    @PostMapping(value = "/suggest-datenportal", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public ResponseEntity<SucheComplexSuggestsDTO> suggestDatenportal(@RequestParam(value = REQUEST_PARAMETER_QUERY) @NotNull final String query,
            @RequestBody @NotNull @Valid final SearchAndFilterOptionsDTO searchAndFilterOptions) {
        try {
            final SucheComplexSuggestsDTO sucheComplexSuggestsDTO = this.sucheService.getComplexSuggestSichtbarDatenportal(query, searchAndFilterOptions);
            return new ResponseEntity<>(sucheComplexSuggestsDTO, HttpStatus.OK);
        } catch (final ResourceNotFoundException e) {
            log.error("Fehler im SucheController beim suggest der Query: {}", query, e);
            throw e;
        } catch (final Exception e) {
            log.error("Unerwarteter Fehler im SucheController beim suggest der Query: {}", query, e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Es ist ein unerwarteter Fehler bei der Suche nach Vorschlägen aufgetreten.");
        }
    }

    @PostMapping(value = "/suggest", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public ResponseEntity<SucheComplexSuggestsDTO> suggest(@RequestParam(value = REQUEST_PARAMETER_QUERY) @NotNull final String query,
            @RequestBody @NotNull @Valid final SearchAndFilterOptionsDTO searchAndFilterOptions) {
        try {
            final SucheComplexSuggestsDTO sucheComplexSuggestsDTO = this.sucheService.getComplexSuggest(query, searchAndFilterOptions, true);
            return new ResponseEntity<>(sucheComplexSuggestsDTO, HttpStatus.OK);
        } catch (final ResourceNotFoundException e) {
            log.error("Fehler im SucheController beim suggest der Query: {}", query, e);
            throw e;
        } catch (final Exception e) {
            log.error("Unerwarteter Fehler im SucheController beim suggest der Query: {}", query, e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Es ist ein unerwarteter Fehler bei der Suche nach Vorschlägen aufgetreten.");
        }
    }

    @PostMapping(value = "/search-datenportal", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public ResponseEntity<Set<ErhebungsstelleKarteDTO>> searchErhebungsstelleForMapDatenportal(
            @RequestParam(value = REQUEST_PARAMETER_QUERY) @NotNull final String query,
            @RequestBody @NotNull @Valid final SearchAndFilterOptionsDTO searchAndFilterOptions) {
        try {
            final Set<ErhebungsstelleKarteDTO> erhebungsstellenForMap = this.sucheService.sucheErhebungsstelleSichtbarDatenportal(query,
                    searchAndFilterOptions);
            return new ResponseEntity<>(erhebungsstellenForMap, HttpStatus.OK);
        } catch (final ResourceNotFoundException e) {
            log.error("Fehler im SucheController beim Suchen der Query: {}", query, e);
            throw e;
        } catch (final Exception e) {
            log.error("Unerwarteter Fehler im SucheController beim Suchen", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Es ist ein unerwarteter Fehler beim Laden der Zähl-/Messstellen aufgetreten.");
        }
    }

    @PostMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public ResponseEntity<Set<ErhebungsstelleKarteDTO>> searchErhebungsstelleForMap(
            @RequestParam(value = REQUEST_PARAMETER_QUERY, defaultValue = "") @NotNull final String query,
            @RequestBody @NotNull @Valid final SearchAndFilterOptionsDTO searchAndFilterOptions) {
        try {
            final Set<ErhebungsstelleKarteDTO> erhebungsstellenForMap = this.sucheService.sucheErhebungsstelle(query, searchAndFilterOptions, true);
            return new ResponseEntity<>(erhebungsstellenForMap, HttpStatus.OK);
        } catch (final ResourceNotFoundException e) {
            log.error("Fehler im SucheController beim Suchen der Query: {}", query, e);
            throw e;
        } catch (final Exception e) {
            log.error("Unerwarteter Fehler im SucheController beim Suchen", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Es ist ein unerwarteter Fehler beim Laden der Zähl-/Messstellen aufgetreten.");
        }
    }
}
