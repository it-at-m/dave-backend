package de.muenchen.dave.domain.dtos.laden;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@RequiredArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class LadeAuswertungSpitzenstundeDTO extends LadeZaehldatumDTO {

    private Integer von;

    private Integer nach;

    // Info Zählstelle
    private String nummerZaehlstelle;

    private String stadtbezirk;

    private Integer stadtbezirkNummer;

    // Info Zählung
    private LocalDate datum;

    private String zaehlart;

    private String kreuzungsname;

    private Boolean sonderzaehlung;

    private String zaehlsituation;

    private String zaehlsituationErweitert;

}
