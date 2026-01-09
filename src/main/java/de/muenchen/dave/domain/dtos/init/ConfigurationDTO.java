package de.muenchen.dave.domain.dtos.init;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ConfigurationDTO {

    private ZaehlstelleConfigurationDTO zaehlstelle;

    private TenantConfigurationDTO tenant;

}
