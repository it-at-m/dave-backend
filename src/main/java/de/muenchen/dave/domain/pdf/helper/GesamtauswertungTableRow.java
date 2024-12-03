package de.muenchen.dave.domain.pdf.helper;

import lombok.Data;

import java.util.List;

@Data
public class GesamtauswertungTableRow {
    private String legend;
    private String cssColorBox;
    private List<GesamtauswertungTableColumn> gesamtauswertungTableColumns;

}
