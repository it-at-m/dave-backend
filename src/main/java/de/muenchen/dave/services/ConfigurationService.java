package de.muenchen.dave.services;

import de.muenchen.dave.domain.dtos.init.ConfigurationDto;
import de.muenchen.dave.domain.dtos.init.MapConfigurationDto;
import de.muenchen.dave.domain.dtos.init.TenantConfigurationDto;
import de.muenchen.dave.domain.dtos.init.ZaehlstelleConfigurationDto;
import de.muenchen.dave.properties.MapConfigProperties;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@Getter
public class ConfigurationService {

    private final ConfigurationDto configuration;

    public ConfigurationService(
            @Value("${dave.zaehlstelle.automatic-number-assignment:true}") final boolean zaehlstelleAutomaticNumberAssignment,
            @Value("${dave.tenant.datenportal-header:Datenportal}") final String datenportalHeader,
            @Value("${dave.zaehlstelle.link-documentation-csv-file-for-upload-zaehlung}") final String linkDocumentationCsvFileForUploadZaehlung,
            MapConfigProperties mapProperties) {
        final var zaehlstelleConfig = new ZaehlstelleConfigurationDto(
                zaehlstelleAutomaticNumberAssignment,
                linkDocumentationCsvFileForUploadZaehlung);
        final var mapConfiguration = new MapConfigurationDto(
                mapProperties.getCenterLat(),
                mapProperties.getCenterLng(),
                mapProperties.getCenterZoom(),
                mapProperties.getBaseLayers(),
                mapProperties.getOverlayLayers());
        final var tenantConfiguration = new TenantConfigurationDto(datenportalHeader, mapConfiguration);
        this.configuration = new ConfigurationDto(zaehlstelleConfig, tenantConfiguration);
    }

}
