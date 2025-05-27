package de.muenchen.dave.domain.dtos.messstelle;

import java.io.Serializable;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EditMessquerschnittDTO implements Serializable {

    private String id;
    private String mqId;
    private Double longitude;
    private Double latitude;
    private String strassenname;
    private String lageMessquerschnitt;
    private String fahrtrichtung;
    private Integer anzahlFahrspuren;
    private String fahrzeugklasse;
    private String detektierteVerkehrsarten;
    private String hersteller;
    private Integer anzahlDetektoren;
    private String standort;
}
