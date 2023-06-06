package de.muenchen.dave.domain.elasticsearch;

import de.muenchen.dave.util.geo.CoordinateUtil;
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
@Document(indexName = "zaehlstelle")
public class Zaehlstelle {

    @Id
    String id;

    @Field(type = FieldType.Text)
    String nummer;

    @Field(type = FieldType.Text)
    String stadtbezirk;

    @Field(type = FieldType.Integer)
    Integer stadtbezirkNummer;

    String kommentar;

    /**
     * In WGS84-Koordinatendarstellung.
     */
    @GeoPointField
    GeoPoint punkt;

    @Field(type = FieldType.Integer)
    Integer letzteZaehlungMonatNummer;

    @Field(type = FieldType.Text)
    String letzteZaehlungMonat;

    @Field(type = FieldType.Integer)
    Integer letzteZaehlungJahr;

    @Field(type = FieldType.Text)
    String grundLetzteZaehlung;

    List<String> suchwoerter;

    List<String> customSuchwoerter;

    List<Zaehlung> zaehlungen = new ArrayList<>();

    /**
     * Steuert die Sichtbarkeit der Zählstelle im Datenportal.
     */
    Boolean sichtbarDatenportal;

    public CoordinateUtil.PositionUTM getPunktUtm() {
        return CoordinateUtil.transformFromWGS84ToUTM(punkt);
    }

}
