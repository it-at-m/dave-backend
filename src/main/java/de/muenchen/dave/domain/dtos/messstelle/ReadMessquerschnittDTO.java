package de.muenchen.dave.domain.dtos.messstelle;

import java.io.Serializable;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReadMessquerschnittDTO implements Serializable {

    private String id;
    private String nummer;
    private String datum;
    private Double longitude;
    private Double latitude;
    private String strassenname;
    private String lage;
    private String fahrrichtung;
    private Integer anzahlFahrspuren;
    private String fahrzeugKlassen;
    private String detektierteVerkehrsarten;
    private String hersteller;
    private Integer anzahlDetektoren;
}
