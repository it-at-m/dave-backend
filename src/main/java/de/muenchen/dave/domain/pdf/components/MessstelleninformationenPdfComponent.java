package de.muenchen.dave.domain.pdf.components;

import lombok.Data;

@Data
public class MessstelleninformationenPdfComponent {

    private boolean standortNeeded;
    private String standort;

    private String detektierteFahrzeuge;

    private String messzeitraum;

    private boolean zeitintervallNeeded;
    private String zeitintervall;

    private boolean wochentagNeeded;
    private String wochentag;

    private boolean kommentarNeeded;
    private String kommentar;
}
