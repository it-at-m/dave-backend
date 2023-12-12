package de.muenchen.dave.domain.elasticsearch.detektor;

import java.time.LocalDate;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.GeoPointField;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

@Data
@NoArgsConstructor
public class Messquerschnitt {

    @Id
    String id;

    String nummer;

    @Field(type = FieldType.Date, pattern = "dd.MM.uuuu")
    LocalDate datum;

    /**
     * In WGS84-Koordinatendarstellung.
     */
    @GeoPointField
    GeoPoint punkt;

    String strassenname;
    String lage;
    String fahrrichtung;
    Integer anzahlFahrspuren;
    String fahrzeugKlassen;
    String detektierteVerkehrsarten;
    String hersteller;
    Integer anzahlDetektoren;
}
