package de.muenchen.dave.domain.dtos.messstelle;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class ReadMessquerschnittDTO implements Serializable {

    private String id;
    private String mqId;
    private Double longitude;
    private Double latitude;
    private String strassenname;
    private String lageMessquerschnitt;
    private String fahrtrichtung;
    private Integer anzahlFahrspuren;
    private Integer anzahlDetektoren;
    private String standort;
}
