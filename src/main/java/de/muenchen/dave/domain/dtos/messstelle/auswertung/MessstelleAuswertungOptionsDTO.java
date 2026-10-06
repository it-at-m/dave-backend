package de.muenchen.dave.domain.dtos.messstelle.auswertung;

import de.muenchen.dave.domain.dtos.messstelle.FahrzeugOptionsDTO;
import de.muenchen.dave.domain.enums.AuswertungsZeitraum;
import de.muenchen.dave.domain.enums.TagesTyp;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MessstelleAuswertungOptionsDTO implements Serializable {

    @NotNull
    private List<Integer> jahre;
    @NotNull
    private TagesTyp tagesTyp;
    @NotNull
    private List<AuswertungsZeitraum> zeitraum;
    @NotNull
    private Set<MessstelleAuswertungIdDTO> messstelleAuswertungIds;
    @NotNull
    private FahrzeugOptionsDTO fahrzeuge;

}
