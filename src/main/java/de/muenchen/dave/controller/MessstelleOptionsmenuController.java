package de.muenchen.dave.controller;

import de.muenchen.dave.domain.dtos.NichtPlausibleTageResponseDTO;
import de.muenchen.dave.services.MessstelleOptionsmenuService;
import javax.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/messstelleOptionsmenu")
@AllArgsConstructor
@Slf4j
public class MessstelleOptionsmenuController {
    public static final String REQUEST_PARAM_MESSSTELLE_ID = "messstelle_id";

    private final MessstelleOptionsmenuService messstelleOptionsmenuService;

    @GetMapping("/nichtPlausibleTage")
    public ResponseEntity<NichtPlausibleTageResponseDTO> getPlausibleTage(@RequestParam(value = REQUEST_PARAM_MESSSTELLE_ID) @NotEmpty String messstelleId) {
        log.debug("#getPlausibleTage for MessstelleId {}", messstelleId);
        return ResponseEntity.ok(messstelleOptionsmenuService.getNichtPlausibleDatenFromEai(messstelleId));
    }
}
