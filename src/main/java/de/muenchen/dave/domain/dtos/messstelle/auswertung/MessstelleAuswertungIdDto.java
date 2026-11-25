package de.muenchen.dave.domain.dtos.messstelle.auswertung;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Set;
import lombok.Data;

@Data
public class MessstelleAuswertungIdDto implements Serializable {

    @NotNull
    private String mstId;
    @NotNull
    private Set<String> mqIds;
}
