package de.muenchen.dave.domain.pdf.helper;

import java.util.List;
import lombok.Data;

@Data
public class GesamtauswertungTable {

    private List<GesamtauswertungTableRow> gesamtauswertungTableRows;

    private List<GesamtauswertungTableHeader> gesamtauswertungTableHeaders;

}
