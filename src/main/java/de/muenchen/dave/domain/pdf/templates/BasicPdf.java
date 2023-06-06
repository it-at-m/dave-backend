package de.muenchen.dave.domain.pdf.templates;

import de.muenchen.dave.domain.pdf.components.ZaehlstelleninformationenPdfComponent;
import de.muenchen.dave.domain.pdf.components.ZusatzinformationenPdfComponent;
import lombok.Data;

@Data
public class BasicPdf extends PdfBean {

    private ZaehlstelleninformationenPdfComponent zaehlstelleninformationen;

    private ZusatzinformationenPdfComponent zusatzinformationen;

    private String zaehlstelleninformationenMustachePart;

    private String zusatzinformationenMustachePart;

    private String documentTitle;

    public BasicPdf() {
        zaehlstelleninformationen = new ZaehlstelleninformationenPdfComponent();
        zusatzinformationen = new ZusatzinformationenPdfComponent();
    }

}
