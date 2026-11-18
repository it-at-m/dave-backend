package de.muenchen.dave.domain.dtos.bearbeiten;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GeoPointDTOAdapter {

    private double lat;
    private double lon;

    public GeoPointDTOAdapter(double lat, double lng) {
    }
}
