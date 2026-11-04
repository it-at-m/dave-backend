package de.muenchen.dave.domain.dtos.messstelle.auswertung;

import de.muenchen.dave.domain.enums.Verkehrsart;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import lombok.Data;

@Data
public class MessstelleAuswertungDTO implements Serializable {

    @NotNull
    private String mstId;
    private String standort;
    @NotNull
    private List<MessquerschnittAuswertungDTO> messquerschnitte;
    private Verkehrsart detektierteVerkehrsart;
}
