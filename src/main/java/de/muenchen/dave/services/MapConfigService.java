package de.muenchen.dave.services;

import de.muenchen.dave.domain.dtos.init.MapConfigDTO;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@Getter
public class MapConfigService {

    private final MapConfigDTO mapConfig;

    public MapConfigService(
            @Value("${dave.map.center.lat:48.137227}") final String lat,
            @Value("${dave.map.center.lng:11.575517}") final String lng,
            @Value("${dave.map.center.zoom:8}") final Integer zoom) {
        this.mapConfig = new MapConfigDTO(lat, lng, zoom);
    }

}
