package de.muenchen.dave.domain.pdf.helper;

import lombok.Data;

import java.util.List;

@Data
public class ZeitreiheTable {

    private List<ZeitreiheTableRow> zeitreiheTableRows;

    private boolean kraftfahrzeugverkehr;

    private boolean gueterverkehr;

    private boolean schwerverkehr;

    private boolean radverkehr;

    private boolean fussverkehr;

    private boolean schwerverkehrsanteilProzent;

    private boolean gueterverkehrsanteilProzent;

    private boolean zeitreiheGesamt;

}
