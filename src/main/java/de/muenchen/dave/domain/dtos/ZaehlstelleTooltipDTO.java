package de.muenchen.dave.domain.dtos;

import lombok.Data;

@Data
public class ZaehlstelleTooltipDTO implements ErhebungsstelleTooltipDTO {

    private String zaehlstellennnummer;

    private Integer stadtbezirknummer;

    private Integer anzahlZaehlungen;

    private String datumLetzteZaehlung;

    private String kreuzungsname;

}
