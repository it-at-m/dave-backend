package de.muenchen.dave.domain.pdf.helper;

import lombok.Data;

import java.util.List;

@Data
public class GesamtauswertungTable {

    private List<GesamtauswertungTableRow> gesamtauswertungTableRows;

    private List<GesamtauswertungTableHeader> gesamtauswertungTableHeaders;

}
