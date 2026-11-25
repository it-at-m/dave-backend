package de.muenchen.dave.domain.dtos.init;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ConfigurationDto {

    @NotNull
    private ZaehlstelleConfigurationDto zaehlstelle;

    @NotNull
    private TenantConfigurationDto tenant;

}
