package de.muenchen.dave.domain.dtos.init;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TenantConfigurationDto {

    @NotNull
    private String datenportalHeader;
    @NotNull
    private MapConfigurationDto mapConfiguration;
}
