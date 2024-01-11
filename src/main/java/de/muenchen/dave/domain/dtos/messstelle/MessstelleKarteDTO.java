package de.muenchen.dave.domain.dtos.messstelle;

import java.io.Serializable;
import lombok.Data;

@Data
public class MessstelleKarteDTO implements Serializable {

    private String id;

    private String mstId;

    private Double longitude;

    private Double latitude;

    private MessstelleTooltipDTO tooltip;

    private Boolean sichtbarDatenportal;

}
