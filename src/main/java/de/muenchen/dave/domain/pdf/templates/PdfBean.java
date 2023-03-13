package de.muenchen.dave.domain.pdf.templates;

import de.muenchen.dave.domain.pdf.MustacheBean;
import lombok.Data;


@Data
public class PdfBean implements MustacheBean {

    private String logoMustachePart;

    private String globalCssMustachePart;

    private String footerMustachePart;

    private String footerDate;

    private String footerOrganisationseinheit;

}
