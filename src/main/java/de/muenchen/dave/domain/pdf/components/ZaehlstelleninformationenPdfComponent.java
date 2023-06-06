package de.muenchen.dave.domain.pdf.components;

import lombok.Data;

@Data
public class ZaehlstelleninformationenPdfComponent {

    private String wetter;

    private String zaehlsituation;

    private String zaehlsituationErweitert;

    private String projektname;

    private String zaehldatum;

    private String zaehldauer;

    private String kreuzungsname;

}
