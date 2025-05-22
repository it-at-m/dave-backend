package de.muenchen.dave.domain.pdf.helper;

import java.util.List;
import lombok.Data;

@Data
public class GesamtauswertungTableRow {
    private String legend;
    private String cssColorBox;
    private List<GesamtauswertungTableColumn> gesamtauswertungTableColumns;

}
