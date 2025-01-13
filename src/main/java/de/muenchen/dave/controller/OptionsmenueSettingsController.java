package de.muenchen.dave.controller;

import de.muenchen.dave.domain.dtos.OptionsmenueSettingsDTO;
import de.muenchen.dave.services.OptionsmenueSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/settings-optionsmenue")
@RequiredArgsConstructor
public class OptionsmenueSettingsController {

    private final OptionsmenueSettingsService optionsmenueSettingsService;

    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<OptionsmenueSettingsDTO>> getAllOptionsmenueSettings() {
        final var settings = optionsmenueSettingsService.getAllOptionsmenueSettings();
        return ResponseEntity.ok(settings);
    }

}
