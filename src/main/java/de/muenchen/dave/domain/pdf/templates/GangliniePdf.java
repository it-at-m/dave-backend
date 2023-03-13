package de.muenchen.dave.domain.pdf.templates;

import de.muenchen.dave.domain.pdf.helper.GanglinieTable;
import lombok.Data;

import java.util.List;


@Data
public class GangliniePdf extends DiagrammPdf {

    private String ganglinieCssMustachePart;

    private String ganglinieTablesMustachePart;

    private String schematischeUebersichtMustachePart;

    // Breite einer Zelle in der Tabelle
    private String tableCellWidth;

    private String kreuzungsgeometrie;

    private List<GanglinieTable> ganglinieTables;

    private String schematischeUebersichtAsBase64Png;

    private boolean schematischeUebersichtNeeded;

    private boolean kraftfahrzeugverkehr;

    private boolean schwerverkehr;

    private boolean gueterverkehr;

    private boolean radverkehr;

    private boolean fussverkehr;

    private boolean schwerverkehrsanteilProzent;

    private boolean gueterverkehrsanteilProzent;

    private boolean pkwEinheiten;

    private boolean personenkraftwagen;

    private boolean lastkraftwagen;

    private boolean lastzuege;

    private boolean busse;

    private boolean kraftraeder;

}
