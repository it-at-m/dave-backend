package de.muenchen.dave.services;

import de.muenchen.dave.domain.dtos.init.ConfigurationDTO;
import de.muenchen.dave.domain.dtos.init.MapConfigurationDTO;
import de.muenchen.dave.domain.dtos.init.ZaehlstelleConfiguration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@Getter
public class ConfigurationService {

    private final ConfigurationDTO configuration;

    public ConfigurationService(
            @Value("${dave.map.center.lat:48.137227}") final String lat,
            @Value("${dave.map.center.lng:11.575517}") final String lng,
            @Value("${dave.map.center.zoom:12}") final Integer zoom,
            @Value("${dave.zaehlstelle.automatic-number-assignment:true}") final boolean zaehlstelleAutomaticNumberAssignment) {
        final var zaehlstelleConfig = new ZaehlstelleConfiguration(zaehlstelleAutomaticNumberAssignment);
        final var mapConfiguration = new MapConfigurationDTO(lat, lng, zoom);
        this.configuration = new ConfigurationDTO(mapConfiguration, zaehlstelleConfig);
    }

}
