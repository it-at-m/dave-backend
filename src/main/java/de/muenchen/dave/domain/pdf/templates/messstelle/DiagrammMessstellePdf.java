package de.muenchen.dave.domain.pdf.templates.messstelle;

import lombok.Data;

@Data
public class DiagrammMessstellePdf extends BasicMessstellePdf {

    private String chart;
    private String chartTitle;

}
