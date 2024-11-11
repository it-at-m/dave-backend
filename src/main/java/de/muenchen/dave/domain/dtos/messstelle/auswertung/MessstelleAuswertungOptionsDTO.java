package de.muenchen.dave.domain.dtos.messstelle.auswertung;

import de.muenchen.dave.domain.dtos.messstelle.FahrzeugOptionsDTO;
import de.muenchen.dave.domain.enums.AuswertungsZeitraum;
import de.muenchen.dave.domain.enums.TagesTyp;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

@Data
public class MessstelleAuswertungOptionsDTO implements Serializable {

    private List<Integer> jahre;
    private TagesTyp tagesTyp;
    private List<AuswertungsZeitraum> zeitraum;

    // TODO: Zusammenfassen der mstIds und mqids in eine Liste von Objekten für Mst welche jeweils die mqId beinhalten.
    private Set<String> mstIds;
    private Set<String> mqIds;
    private FahrzeugOptionsDTO fahrzeuge;

}
