package de.muenchen.dave.domain.pdf.components;

import de.muenchen.dave.domain.elasticsearch.Knotenarm;
import java.util.List;
import lombok.Data;

@Data
public class ZaehlstelleninformationenZeitreihePdfComponent {

    private boolean platzVorhanden;

    private String platz;

    private List<Knotenarm> knotenarme;

}
