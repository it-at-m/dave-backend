package de.muenchen.dave.domain.pdf.templates;

import lombok.Data;

@Data
public class ReportPdf extends PdfBean {
    private String cssFixed;
    private String cssCustom;
    private String body;
}
