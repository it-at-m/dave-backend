package de.muenchen.dave.domain.pdf.templates.messstelle;

import de.muenchen.dave.domain.pdf.helper.messstelle.DatentabellePdfMessstelle;
import lombok.Data;

@Data
public class DatentabellePdf extends BasicPdf {

    private String datentabelleTableMustachePart;

    private String datentabelleCssMustachePart;

    private String schematischeUebersichtMustachePart;

    private DatentabellePdfMessstelle datentabellePdfMessstelle;

    private String schematischeUebersichtAsBase64Png;

    private boolean schematischeUebersichtNeeded;

    private String tableTitle;

}
