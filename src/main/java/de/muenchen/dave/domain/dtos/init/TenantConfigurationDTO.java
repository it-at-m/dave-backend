package de.muenchen.dave.domain.dtos.init;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TenantConfigurationDTO {

    @NotNull
    private String datenportalHeader;
    @NotNull
    private MapConfigurationDTO mapConfiguration;
}
