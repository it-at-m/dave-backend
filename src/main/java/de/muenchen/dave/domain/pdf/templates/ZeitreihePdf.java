package de.muenchen.dave.domain.pdf.templates;

import de.muenchen.dave.domain.pdf.components.ZaehlstelleninformationenZeitreihePdfComponent;
import de.muenchen.dave.domain.pdf.components.ZusatzinformationenZeitreihePdfComponent;
import de.muenchen.dave.domain.pdf.helper.ZeitreiheTable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ZeitreihePdf extends PdfBean {

    // Tabelle
    private ZeitreiheTable zeitreiheTable;

    // Zusatzinformationen
    private List<ZusatzinformationenZeitreihePdfComponent> zusatzinformationenZeitreihe;
    private boolean sindZusatzinformationenVorhanden;

    // Zählstellen- bzw. Headerinformationen
    private ZaehlstelleninformationenZeitreihePdfComponent zaehlstelleninformationenZeitreihe;

    // Mustache Parts
    private String zeitreiheCssMustachePart;
    private String zaehlstelleninformationenZeitreiheMustachePart;
    private String zeitreiheTableMustachePart;
    private String zusatzinformationenZeitreiheMustachePart;
    private String schematischeUebersichtMustachePart;

    private String documentTitle;
    private String schematischeUebersichtAsBase64Png;
    private String chart;
    private String chartTitle;
    private String zeitauswahl;

    private boolean schematischeUebersichtNeeded;

    public ZeitreihePdf() {
        this.zaehlstelleninformationenZeitreihe = new ZaehlstelleninformationenZeitreihePdfComponent();
        this.zusatzinformationenZeitreihe = new ArrayList<>();
        this.zeitreiheTable = new ZeitreiheTable();
    }

}
