package de.muenchen.dave.services;

import de.muenchen.dave.domain.dtos.init.ConfigurationDTO;
import de.muenchen.dave.domain.dtos.init.MapConfigurationDTO;
import de.muenchen.dave.domain.dtos.init.TenantConfigurationDTO;
import de.muenchen.dave.domain.dtos.init.ZaehlstelleConfigurationDTO;
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
            @Value("${dave.tenant.map.center.lat:48.137227}") final String lat,
            @Value("${dave.tenant.map.center.lng:11.575517}") final String lng,
            @Value("${dave.tenant.map.center.zoom:12}") final Integer zoom,
            @Value("${dave.zaehlstelle.automatic-number-assignment:true}") final boolean zaehlstelleAutomaticNumberAssignment,
            @Value("${dave.tenant.department:Mobilitätsreferat}") final String department,
            @Value("${dave.zaehlstelle.link-documentation-csv-file-for-upload-zaehlung}") final String linkDocumentationCsvFileForUploadZaehlung) {
        final var zaehlstelleConfig = new ZaehlstelleConfigurationDTO(
                zaehlstelleAutomaticNumberAssignment,
                linkDocumentationCsvFileForUploadZaehlung);
        final var mapConfiguration = new MapConfigurationDTO(lat, lng, zoom);
        final var tenantConfiguration = new TenantConfigurationDTO(department, mapConfiguration);
        this.configuration = new ConfigurationDTO(zaehlstelleConfig, tenantConfiguration);
    }

}
