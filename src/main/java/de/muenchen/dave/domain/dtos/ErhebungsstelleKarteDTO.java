package de.muenchen.dave.domain.dtos;

import java.io.Serializable;
import lombok.Data;

@Data
public class ErhebungsstelleKarteDTO implements Serializable {

    private String id;

    private String fachId;

    private String type;

    private Double longitude;

    private Double latitude;

    private ErhebungsstelleTooltipDTO tooltip;

    private Boolean sichtbarDatenportal;

}
