package de.muenchen.dave.controller;

import de.muenchen.dave.domain.ConfigurationEntity;
import de.muenchen.dave.domain.dtos.init.ConfigurationDTO;
import de.muenchen.dave.services.ConfigurationService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
        log.debug("#getMapConfig");
        try {
            return ResponseEntity.ok(configurationService.getConfiguration());
        } catch (Exception ex) {
            log.error("Unerwarteter Fehler im ConfigurationController beim Laden der Configuration.", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Es ist ein unerwarteter Fehler beim Laden der Configuration aufgetreten.");
        }
    }

    @Operation(summary = "Get all configuration entries")
    @GetMapping(value = "/all")
    public List<ConfigurationEntity> getAllConfigurations() {
        return configurationService.getRepository().findAll();
    }

    @Operation(summary = "Set all configuration entries")
    @PreAuthorize("hasRole(T(de.muenchen.dave.security.AuthoritiesEnum).FACHADMIN.name())")
    @PostMapping(value = "/all")
    public List<ConfigurationEntity> setConfiguration(@RequestBody List<ConfigurationEntity> configs) {
        return configurationService.saveOrUpdateList(configs);
    }

    @Operation(summary = "Set configuration entry by key")
    @PreAuthorize("hasRole(T(de.muenchen.dave.security.AuthoritiesEnum).FACHADMIN.name())")
    @PostMapping(value = "/setbykey")
    public ResponseEntity<ConfigurationEntity> setConfigurationByKey(@RequestBody ConfigurationEntity config) {

        try {
            ConfigurationEntity result = configurationService.saveOrUpdate(config);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            log.error("Error setting configuration for key {}: {}", config.getKeyname(), e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

}
