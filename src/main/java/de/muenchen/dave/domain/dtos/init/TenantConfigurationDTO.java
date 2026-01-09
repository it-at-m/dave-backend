package de.muenchen.dave.domain.dtos.init;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TenantConfigurationDTO {

    private String department;
    private MapConfigurationDTO mapConfiguration;
}
