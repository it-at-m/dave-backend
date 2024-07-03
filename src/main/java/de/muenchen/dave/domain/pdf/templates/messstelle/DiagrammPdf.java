package de.muenchen.dave.domain.pdf.templates.messstelle;

import lombok.Data;

@Data
public class DiagrammPdf extends BasicPdf {

    private String chart;
    private String chartTitle;

}
