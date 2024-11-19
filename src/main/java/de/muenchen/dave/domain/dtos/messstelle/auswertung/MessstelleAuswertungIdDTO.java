package de.muenchen.dave.domain.dtos.messstelle.auswertung;

import lombok.Data;

import java.io.Serializable;
import java.util.Set;

@Data
public class MessstelleAuswertungIdDTO implements Serializable {

    private String mstId;
    private Set<String> mqIds;
}
