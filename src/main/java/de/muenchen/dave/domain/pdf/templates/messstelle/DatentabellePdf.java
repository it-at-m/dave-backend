package de.muenchen.dave.domain.pdf.templates.messstelle;

import de.muenchen.dave.domain.pdf.helper.DatentabellePdfZaehldaten;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DatentabellePdf extends BasicPdf {

    private String datentabelleTableMustachePart;

    private String datentabelleCssMustachePart;

    private String schematischeUebersichtMustachePart;

    private DatentabellePdfZaehldaten datentabelleZaehldaten;

    private String schematischeUebersichtAsBase64Png;

    private boolean schematischeUebersichtNeeded;

    private String tableTitle;

}
