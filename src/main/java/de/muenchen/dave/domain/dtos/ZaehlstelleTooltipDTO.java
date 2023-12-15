package de.muenchen.dave.domain.dtos;

import java.io.Serializable;
import lombok.Data;

@Data
public class ZaehlstelleTooltipDTO implements ErhebungsstelleTooltipDTO, Serializable {

    private String zaehlstellennnummer;

    private String stadtbezirk = null;

    private Integer stadtbezirknummer = null;

    private Integer anzahlZaehlungen;

    private String datumLetzteZaehlung;

    private String kreuzungsname;

}
