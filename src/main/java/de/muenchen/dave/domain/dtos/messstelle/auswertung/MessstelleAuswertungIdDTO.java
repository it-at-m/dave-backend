package de.muenchen.dave.domain.dtos.messstelle.auswertung;

import java.io.Serializable;
import java.util.Set;
import lombok.Data;

@Data
public class MessstelleAuswertungIdDTO implements Serializable {

    private String mstId;
    private Set<String> mqIds;
}
