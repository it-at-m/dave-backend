package de.muenchen.dave.controller;

import de.muenchen.dave.domain.CityDistrictEntity;
import de.muenchen.dave.services.CityDistrictService;
import de.muenchen.dave.services.ConfigurationService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/cityinformation")
@RequiredArgsConstructor
public class CityInformationController {

    private final CityDistrictService cityDistrictService;

    private final ConfigurationService configurationService;

    @Operation(summary = "Get all city districts")
    @GetMapping(value = "/all")
    public List<CityDistrictEntity> getAllConfigurations() {
        String city = configurationService.getConfiguredCity();
        log.debug("Loading all city districts for configured city ");
        return cityDistrictService.findByCity(city);
    }

}
