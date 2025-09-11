package de.muenchen.dave.domain.dtos.init;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ConfigurationDTO {

    private MapConfigurationDTO map;

    private ZaehlstelleConfigurationDTO zaehlstelle;

}
