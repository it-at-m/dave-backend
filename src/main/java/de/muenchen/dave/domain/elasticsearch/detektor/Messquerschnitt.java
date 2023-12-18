package de.muenchen.dave.domain.elasticsearch.detektor;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.GeoPointField;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

@Data
@NoArgsConstructor
public class Messquerschnitt {

    @Id
    String id;

    String mqId;

    /**
     * In WGS84-Koordinatendarstellung.
     */
    @GeoPointField
    GeoPoint punkt;

    String strassenname;
    String lage;
    String fahrtrichtung;
    Integer anzahlFahrspuren;
    String fahrzeugKlassen;
    String detektierteVerkehrsarten;
    String hersteller;
    Integer anzahlDetektoren;
    String standort;
}
