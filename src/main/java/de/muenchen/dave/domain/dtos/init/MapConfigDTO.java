package de.muenchen.dave.domain.dtos.init;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MapConfigDTO {

    private String lat;
    private String lng;
    private Integer zoom;

    private List<OverlayLayerDTO> overlayLayers;
}
