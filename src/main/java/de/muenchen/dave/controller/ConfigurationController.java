package de.muenchen.dave.controller;

import de.muenchen.dave.domain.dtos.init.ConfigurationDTO;
import de.muenchen.dave.services.ConfigurationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController
@RequestMapping("/configuration")
@RequiredArgsConstructor
public class ConfigurationController {

    private final ConfigurationService configurationService;

    /**
     * Rest-Endpunkt für das Laden der MapConfig.
     *
     * @return Das MapConfigurationDTO
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public ResponseEntity<ConfigurationDTO> getConfiguration() {
        log.debug("#getConfiguration");
        try {
            return ResponseEntity.ok(configurationService.getConfiguration());
        } catch (Exception ex) {
            log.error("Unerwarteter Fehler im ConfigurationController beim Laden der Configuration.", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Es ist ein unerwarteter Fehler beim Laden der Configuration aufgetreten.");
        }
    }

}
