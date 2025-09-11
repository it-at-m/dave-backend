package de.muenchen.dave.domain.dtos.init;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MapConfigurationDTO {

    private String lat;
    private String lng;
    private Integer zoom;
}
