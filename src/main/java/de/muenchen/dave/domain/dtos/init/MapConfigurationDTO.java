package de.muenchen.dave.domain.dtos.init;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MapConfigurationDTO {

    @NotNull
    private String lat;
    @NotNull
    private String lng;
    @NotNull
    private Integer zoom;

    @NotNull
    private List<LayerDTO> baseLayers;
    @NotNull
    private List<LayerDTO> overlayLayers;
}
