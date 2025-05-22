package de.muenchen.dave.controller;

import de.muenchen.dave.domain.dtos.OptionsmenueSettingsDTO;
import de.muenchen.dave.services.OptionsmenueSettingsService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/settings-optionsmenue")
@RequiredArgsConstructor
public class OptionsmenueSettingsController {

    private final OptionsmenueSettingsService optionsmenueSettingsService;

    @GetMapping(value = "/messstelle/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<OptionsmenueSettingsDTO>> getAllOptionsmenueSettingsForMessstellen() {
        final var settings = optionsmenueSettingsService.getAllOptionsmenueSettingsForMessstellen();
        return ResponseEntity.ok(settings);
    }

}
