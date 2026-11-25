package de.muenchen.dave.domain.dtos.init;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MapConfigurationDto {

    @NotNull
    private String lat;
    @NotNull
    private String lng;
    @NotNull
    private Integer zoom;

    @NotNull
    private List<LayerDto> baseLayers;
    @NotNull
    private List<LayerDto> overlayLayers;
}
