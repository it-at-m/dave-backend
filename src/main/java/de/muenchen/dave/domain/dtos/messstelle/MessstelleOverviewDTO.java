package de.muenchen.dave.domain.dtos.messstelle;

import lombok.Data;

import java.io.Serializable;

@Data
public class MessstelleOverviewDTO implements Serializable {
    private String id;
    private String mstId;
    private String name;
    private String status;
    private Boolean sichtbarDatenportal;
    private Boolean geprueft;
    private String stadtbezirk;
    private String stadtbezirkNummer;
}
