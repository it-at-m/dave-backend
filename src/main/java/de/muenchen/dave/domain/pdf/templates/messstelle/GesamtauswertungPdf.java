package de.muenchen.dave.domain.pdf.templates.messstelle;

import de.muenchen.dave.domain.pdf.helper.GesamtauswertungTable;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class GesamtauswertungPdf extends DiagrammPdf {

    private String gesamtauswertungCssMustachePart;

    private String gesamtauswertungTablesMustachePart;

    private String tableCellWidth;

    private List<GesamtauswertungTable> gesamtauswertungTables;
}
