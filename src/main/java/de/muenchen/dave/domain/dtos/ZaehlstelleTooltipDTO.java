package de.muenchen.dave.domain.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ZaehlstelleTooltipDTO implements ErhebungsstelleTooltipDTO {

    @NotNull
    private String zaehlstellennnummer;
    @NotNull
    private String stadtbezirk;
    @NotNull
    private Integer stadtbezirknummer;
    @NotNull
    private Integer anzahlZaehlungen;
    @NotNull
    private String datumLetzteZaehlung;
    @NotNull
    private String kreuzungsname;

}
