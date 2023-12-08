package de.muenchen.dave.domain.elasticsearch.detektor;

import de.muenchen.dave.util.geo.CoordinateUtil;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.GeoPointField;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

@Data
@Document(indexName = "messstelle")
public class Messstelle {

    @Id
    String id;

    String nummer;

    String name;

    String status;

    @Field(type = FieldType.Date, pattern = "dd.MM.uuuu")
    LocalDate realisierungsdatum;

    @Field(type = FieldType.Date, pattern = "dd.MM.uuuu")
    LocalDate abbaudatum;

    Integer stadtbezirkNummer;

    String bemerkung;

    @Field(type = FieldType.Date, pattern = "dd.MM.uuuu")
    LocalDate datumLetztePlausibleMeldung;
    /**
     * In WGS84-Koordinatendarstellung.
     */
    @GeoPointField
    GeoPoint punkt;

    //    @Field(type = FieldType.Integer)
    //    Integer letzteZaehlungMonatNummer;
    //
    //    @Field(type = FieldType.Text)
    //    String letzteZaehlungMonat;
    //
    //    @Field(type = FieldType.Integer)
    //    Integer letzteZaehlungJahr;
    //
    //    @Field(type = FieldType.Text)
    //    String grundLetzteZaehlung;
    //
    //    @Field(type = FieldType.Text)
    String stadtbezirk;

    /**
     * Steuert die Sichtbarkeit der Messstelle im Datenportal.
     */
    Boolean sichtbarDatenportal;

    String kommentar;

    String standort;

    List<String> suchwoerter;

    List<String> customSuchwoerter;

    List<Messquerschnitt> messquerschnitte = new ArrayList<>();

    public CoordinateUtil.PositionUTM getPunktUtm() {
        return CoordinateUtil.transformFromWGS84ToUTM(punkt);
    }

}
