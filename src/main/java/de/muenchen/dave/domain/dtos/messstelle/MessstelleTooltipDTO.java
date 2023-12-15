package de.muenchen.dave.domain.dtos.messstelle;

import de.muenchen.dave.domain.dtos.ErhebungsstelleTooltipDTO;
import java.io.Serializable;
import lombok.Data;

@Data
public class MessstelleTooltipDTO implements ErhebungsstelleTooltipDTO, Serializable {

    private String mstId;
    private String standort;
    private String stadtbezirk = null;
    private Integer stadtbezirknummer = null;
    private String realisierungsdatum;
    private String abbaudatum;
    private String datumLetztePlausibleMessung;
    private String detektierteVerkehrsarten;

}
