package de.muenchen.dave.domain.dtos;

import de.muenchen.dave.domain.enums.MessstelleStatus;
import java.io.Serializable;
import lombok.Data;

@Data
public class ErhebungsstelleKarteDTO implements Serializable {

    private String id;

    private String fachId;

    private String type;

    private Double longitude;

    private Double latitude;

    private MessstelleStatus status;

    private ErhebungsstelleTooltipDTO tooltip;

    private Boolean sichtbarDatenportal;

}
