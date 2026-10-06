package de.muenchen.dave.domain.dtos.init;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LayerDTO {

    @NotNull
    private String baseUrl;
    @NotNull
    private String layerName;
    @NotNull
    private String layerNameToDisplay;
    @NotNull
    private String attribution;

}
