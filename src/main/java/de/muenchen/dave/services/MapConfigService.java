package de.muenchen.dave.services;

import de.muenchen.dave.domain.dtos.init.MapConfigDTO;
import de.muenchen.dave.properties.MapConfigProperties;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@Getter
public class MapConfigService {

    private final MapConfigDTO mapConfig;

    public MapConfigService(MapConfigProperties props) {
        this.mapConfig = new MapConfigDTO(
                props.getCenterLat(),
                props.getCenterLng(),
                props.getCenterZoom(),
                props.getOverlayLayers());
    }

}
