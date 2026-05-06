package de.muenchen.dave.properties;

import de.muenchen.dave.domain.dtos.init.LayerDTO;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "dave.map")
@Validated
@Getter
@Setter
public class MapConfigProperties {
    private String centerLat = "48.137227";
    private String centerLng = "11.575517";
    private Integer centerZoom = 12;

    @NotEmpty(message = "Es muss mindestens ein Basis-Layer konfiguriert sein (dave.map.baseLayers).")
    private List<LayerDTO> baseLayers;

    private List<LayerDTO> overlayLayers;
}
