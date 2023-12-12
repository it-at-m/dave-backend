package de.muenchen.dave.domain.dtos.messstelle;

import java.io.Serializable;
import lombok.Data;

@Data
public class MessstelleTooltipDTO implements Serializable {

    private String nummer;
    private String standort;
    private String stadtbezirk;
    private Integer stadtbezirknummer;
    private String realisierungsdatum;
    private String abbaudatum;
    private String datumLetztePlausibleMeldung;
    private String detektierteVerkehrsarten;
}
