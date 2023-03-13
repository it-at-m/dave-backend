package de.muenchen.dave.domain.pdf.templates;

import lombok.Data;


@Data
public class DiagrammPdf extends BasicPdf {

    private String chart;

    private String belastungsplanKreisverkehr;

    private String chartTitle;

}