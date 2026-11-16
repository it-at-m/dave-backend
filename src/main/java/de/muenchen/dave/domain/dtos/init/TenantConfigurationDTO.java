package de.muenchen.dave.domain.dtos.init;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TenantConfigurationDTO {

    private String city;
    private String datenportalHeader;
    private MapConfigurationDTO mapConfiguration;
}
