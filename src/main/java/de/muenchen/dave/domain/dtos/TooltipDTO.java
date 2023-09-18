package de.muenchen.dave.domain.dtos;

import java.io.Serializable;
import lombok.Data;

@Data
public class TooltipDTO implements Serializable {

    private String zaehlstellennnummer;

    private String stadtbezirk;

    private Integer stadtbezirknummer;

    private Integer anzahlZaehlungen;

    private String datumLetzteZaehlung;

    private String kreuzungsname;

}
