package de.muenchen.dave.domain.dtos.init;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ConfigurationDTO {

    @NotNull
    private ZaehlstelleConfigurationDTO zaehlstelle;

    @NotNull
    private TenantConfigurationDTO tenant;

}
