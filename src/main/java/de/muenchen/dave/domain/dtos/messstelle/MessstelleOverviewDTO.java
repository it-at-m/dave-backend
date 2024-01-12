package de.muenchen.dave.domain.dtos.messstelle;

import java.io.Serializable;
import lombok.Data;

@Data
public class MessstelleOverviewDTO implements Serializable {
    private String id;
    private String mstId;
    private String name;
    private Boolean sichtbarDatenportal;
    private Boolean geprueft;
}
