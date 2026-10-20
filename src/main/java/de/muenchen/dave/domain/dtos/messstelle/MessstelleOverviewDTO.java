package de.muenchen.dave.domain.dtos.messstelle;

import java.io.Serializable;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MessstelleOverviewDTO implements Serializable {
    private String id;
    @NotNull
    private String mstId;
    private String name;
    private String status;
    private Boolean sichtbarDatenportal;
    private Boolean geprueft;
    private String stadtbezirk;
    private String stadtbezirkNummer;
    private Boolean lageplanVorhanden;
}
