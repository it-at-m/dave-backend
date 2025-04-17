package de.muenchen.dave.controller;

import de.muenchen.dave.domain.dtos.init.MapConfigDTO;
import de.muenchen.dave.services.MapConfigService;
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
@RequestMapping("/mapconfig")
@RequiredArgsConstructor
public class MapConfigController {

    private final MapConfigService mapConfigService;

    /**
     * Rest-Endpunkt für das Laden der MapConfig.
     *
     * @return Das MapConfigDTO
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public ResponseEntity<MapConfigDTO> getMapConfig() {
        log.debug("#getMapConfig");
        try {
            return ResponseEntity.ok(mapConfigService.getMapConfig());
        } catch (Exception ex) {
            log.error("Unerwarteter Fehler im MapConfigController beim Laden der MapConfig.", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Es ist ein unerwarteter Fehler beim Laden der MapConfig aufgetreten.");
        }
    }

}
