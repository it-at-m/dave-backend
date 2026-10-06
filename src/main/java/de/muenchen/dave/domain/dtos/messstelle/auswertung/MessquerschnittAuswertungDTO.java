package de.muenchen.dave.domain.dtos.messstelle.auswertung;

import java.io.Serializable;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MessquerschnittAuswertungDTO implements Serializable {

    @NotNull
    private String mqId;
    private String fahrtrichtung;
    private String standort;
}
