package de.muenchen.dave.domain.dtos.init;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LayerDTO {

    private String baseUrl;
    private String layerName;
    private String layerNameToDisplay;
    private String attribution;

}
