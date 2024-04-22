package de.muenchen.dave.domain.pdf.templates.messstelle;

import de.muenchen.dave.domain.pdf.components.MessstelleninformationenPdfComponent;
import de.muenchen.dave.domain.pdf.templates.PdfBean;
import lombok.Data;

@Data
public class BasicPdf extends PdfBean {

    private MessstelleninformationenPdfComponent messstelleninformationen;

    private String messstelleninformationenMustachePart;

    private String documentTitle;

    public BasicPdf() {
        messstelleninformationen = new MessstelleninformationenPdfComponent();
    }

}
