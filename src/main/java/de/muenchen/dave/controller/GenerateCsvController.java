package de.muenchen.dave.controller;

import de.muenchen.dave.domain.dtos.CsvDTO;
import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.exceptions.DataNotFoundException;
import de.muenchen.dave.services.GenerateCsvService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@RestController
@Slf4j
@Validated
@PreAuthorize("hasAnyRole(T(de.muenchen.dave.security.AuthoritiesEnum).ANWENDER.name(), " +
        "T(de.muenchen.dave.security.AuthoritiesEnum).POWERUSER.name())")
public class GenerateCsvController {

    private static final String REQUEST_PARAMETER_ZAEHLUNG_ID = "zaehlung_id";

    private final GenerateCsvService generateCsvService;

    public GenerateCsvController(final GenerateCsvService generateCsvService) {
        this.generateCsvService = generateCsvService;
    }

    /**
     * Nimmt Daten aus dem Frontend entgegen und gibt eine CSV als String zurück.
     *
     * @param zaehlungId Die im Frontend ausgewählte Zählung.
     * @param options    Die im Frontend ausgewählten Optionen.
     * @return ResponseEntity of Type CsvDTO
     */
    @PostMapping(value = "/generate-csv")
    public ResponseEntity<CsvDTO> generateCSV(
            @RequestParam(value = REQUEST_PARAMETER_ZAEHLUNG_ID) @NotEmpty final String zaehlungId,
            @Valid @RequestBody @NotNull final OptionsDTO options) {
        try {
            log.info("GenerateCSV für Zaehlung {} aufgerufen", zaehlungId);
            final CsvDTO dto = generateCsvService.generateDatentabelleCsv(zaehlungId, options);
            log.info("CSV wurde erstellt");
            return new ResponseEntity<>(dto, HttpStatus.OK);
        } catch (final DataNotFoundException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        }
    }

}
