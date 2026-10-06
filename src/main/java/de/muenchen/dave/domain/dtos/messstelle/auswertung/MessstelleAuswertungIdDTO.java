package de.muenchen.dave.domain.dtos.messstelle.auswertung;

import java.io.Serializable;
import java.util.Set;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MessstelleAuswertungIdDTO implements Serializable {

    @NotNull
    private String mstId;
    @NotNull
    private Set<String> mqIds;
}
