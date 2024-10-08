package de.muenchen.dave.domain.pdf.components;

import lombok.Data;

@Data
public class MessstelleninformationenPdfComponent {

    private String standort;

    private String detektierteFahrzeuge;

    private String messzeitraum;

    private String wochentag;

    private String kommentar;

    private boolean wochentagNeeded;
}
