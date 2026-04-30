package de.muenchen.dave.properties;

import de.muenchen.dave.domain.dtos.init.OverlayLayerDTO;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "dave.map")
@Getter
@Setter
public class MapConfigProperties {
    private String centerLat = "48.137227";
    private String centerLng = "11.575517";
    private Integer centerZoom = 12;
    private List<OverlayLayerDTO> overlayLayers;
}
