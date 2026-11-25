package de.muenchen.dave.domain.dtos.messstelle.auswertung;

import de.muenchen.dave.domain.dtos.messstelle.FahrzeugOptionsDto;
import de.muenchen.dave.domain.enums.AuswertungsZeitraum;
import de.muenchen.dave.domain.enums.TagesTyp;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import java.util.Set;
import lombok.Data;

@Data
public class MessstelleAuswertungOptionsDto implements Serializable {

    @NotNull
    private List<Integer> jahre;
    @NotNull
    private TagesTyp tagesTyp;
    @NotNull
    private List<AuswertungsZeitraum> zeitraum;
    @NotNull
    private Set<MessstelleAuswertungIdDto> messstelleAuswertungIds;
    @NotNull
    private FahrzeugOptionsDto fahrzeuge;

}
