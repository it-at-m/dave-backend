package de.muenchen.dave.domain.dtos.messstelle;

import de.muenchen.dave.domain.dtos.ErhebungsstelleTooltipDTO;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MessstelleTooltipDTO implements ErhebungsstelleTooltipDTO {

    @NotNull
    private String mstId;
    @NotNull
    private String standort;
    @NotNull
    private String stadtbezirk;
    @NotNull
    private Integer stadtbezirknummer;
    @NotNull
    private String realisierungsdatum;
    @NotNull
    private String abbaudatum;
    @NotNull
    private String datumLetztePlausibleMessung;
    @NotNull
    private String detektierteVerkehrsart;
}
