package de.muenchen.dave.controller;

import de.muenchen.dave.domain.dtos.CsvDTO;
import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.dtos.messstelle.MessstelleOptionsDTO;
import de.muenchen.dave.exceptions.DataNotFoundException;
import de.muenchen.dave.services.GenerateCsvService;
import de.muenchen.dave.services.messstelle.GenerateCsvMstService;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
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

@RestController
@Slf4j
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasAnyRole(T(de.muenchen.dave.security.AuthoritiesEnum).ANWENDER.name(), " +
        "T(de.muenchen.dave.security.AuthoritiesEnum).POWERUSER.name())")
public class GenerateCsvController {

    private static final String REQUEST_PARAMETER_ZAEHLUNG_ID = "zaehlung_id";
    private static final String REQUEST_PARAMETER_MESSSTELLE_ID = "messstelle_id";

    private final GenerateCsvService generateCsvService;
    private final GenerateCsvMstService generateCsvServiceMst;

    /**
     * Nimmt Daten aus dem Frontend entgegen und gibt eine CSV als String zurück.
     *
     * @param zaehlungId Die im Frontend ausgewählte Zählung.
     * @param options Die im Frontend ausgewählten Optionen.
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

    @PostMapping(value = "/generate-csv-mst")
    public ResponseEntity<CsvDTO> generateCSVMessstelle(
            @RequestParam(value = REQUEST_PARAMETER_MESSSTELLE_ID) @NotEmpty final String messstelleId,
            @Valid @RequestBody @NotNull final MessstelleOptionsDTO options) {
        try {
            log.info("GenerateCSV für Messstelle {} aufgerufen", messstelleId);
            final CsvDTO dto = generateCsvServiceMst.generateDatentabelleCsv(messstelleId, options);
            log.info("CSV wurde erstellt");
            return new ResponseEntity<>(dto, HttpStatus.OK);
        } catch (final DataNotFoundException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        }
    }

}
