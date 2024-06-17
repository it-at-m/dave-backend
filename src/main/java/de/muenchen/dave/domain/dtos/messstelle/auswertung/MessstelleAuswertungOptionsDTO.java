package de.muenchen.dave.domain.dtos.messstelle.auswertung;

import de.muenchen.dave.domain.dtos.messstelle.FahrzeugOptionsDTO;
import de.muenchen.dave.domain.enums.AuswertungsZeitraum;
import de.muenchen.dave.domain.enums.TagesTyp;
import java.io.Serializable;
import java.util.List;
import lombok.Data;

@Data
public class MessstelleAuswertungOptionsDTO implements Serializable {

    private List<String> jahre;
    private TagesTyp tagesTyp;
    private List<AuswertungsZeitraum> zeitraum;
    private List<String> mstIds;
    private List<String> mqIds;
    private FahrzeugOptionsDTO fahrzeuge;

}
