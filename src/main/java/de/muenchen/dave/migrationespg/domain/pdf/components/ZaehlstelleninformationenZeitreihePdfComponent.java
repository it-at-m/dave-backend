package de.muenchen.dave.migrationespg.domain.pdf.components;

import de.muenchen.dave.migrationespg.domain.relational.zaehlstellen.KnotenarmEntity;
import java.util.List;
import lombok.Data;

@Data
public class ZaehlstelleninformationenZeitreihePdfComponent {

    private boolean platzVorhanden;

    private String platz;

    private List<KnotenarmEntity> knotenarme;

}
