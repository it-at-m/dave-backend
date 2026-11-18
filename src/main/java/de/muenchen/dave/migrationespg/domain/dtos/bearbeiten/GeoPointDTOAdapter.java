package de.muenchen.dave.migrationespg.domain.dtos.bearbeiten;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GeoPointDTOAdapter {

    private double lat;
    private double lon;

}
