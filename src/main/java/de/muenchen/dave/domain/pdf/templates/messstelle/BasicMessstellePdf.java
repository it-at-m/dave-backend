package de.muenchen.dave.domain.pdf.templates.messstelle;

import de.muenchen.dave.domain.pdf.components.MessstelleninformationenPdfComponent;
import de.muenchen.dave.domain.pdf.templates.PdfBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BasicMessstellePdf extends PdfBean {

    private MessstelleninformationenPdfComponent messstelleninformationen;

    private String messstelleninformationenMustachePart;

    private String documentTitle;

    public BasicMessstellePdf() {
        messstelleninformationen = new MessstelleninformationenPdfComponent();
    }

}
