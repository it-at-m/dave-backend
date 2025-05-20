package de.muenchen.dave.domain.dtos.messstelle;

import de.muenchen.dave.domain.dtos.ErhebungsstelleTooltipDTO;
import lombok.Data;

@Data
public class MessstelleTooltipDTO implements ErhebungsstelleTooltipDTO {

    private String mstId;
    private String standort;
    private String stadtbezirk;
    private Integer stadtbezirknummer;
    private String realisierungsdatum;
    private String abbaudatum;
    private String datumLetztePlausibleMessung;
    private String detektierteVerkehrsarten;
}
