package de.muenchen.dave.domain.dtos.messstelle.auswertung;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class MessquerschnittAuswertungDTO implements Serializable {

    private String mqId;
    private String fahrtrichtung;
    private String standort;
}
