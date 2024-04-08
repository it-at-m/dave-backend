package de.muenchen.dave.domain.dtos.messstelle.auswertung;

import java.io.Serializable;
import java.util.List;
import lombok.Data;

@Data
public class MessstelleAuswertungDTO implements Serializable {

    private String mstId;
    private String standort;
    private List<MessquerschnittAuswertungDTO> messquerschnitte;
    private String detektierteVerkehrsarten;
}
