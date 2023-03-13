package de.muenchen.dave.domain.pdf.components;

import lombok.Data;


@Data
public class ZusatzinformationenPdfComponent {

    private boolean istKommentarVorhanden;

    private boolean istKommentarVorhandenZaehlstelle;

    private String kommentarZaehlstelle;

    private boolean istKommentarVorhandenZaehlung;

    private String kommentarZaehlung;

}