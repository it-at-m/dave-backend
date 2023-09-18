package de.muenchen.dave.domain.elasticsearch;

import de.muenchen.dave.domain.enums.Fahrzeug;
import de.muenchen.dave.util.geo.CoordinateUtil;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.GeoPointField;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

@Data
@NoArgsConstructor
public class Zaehlung {

    String id;

    @Field(type = FieldType.Date, format = DateFormat.custom, pattern = "dd.MM.uuuu")
    LocalDate datum;

    String jahr;

    String monat;

    String jahreszeit;

    String zaehlart;

    /**
     * In WGS84-Koordinatendarstellung.
     */
    @GeoPointField
    GeoPoint punkt;

    /**
     * Wochenende, Wochentag, Feiertag
     */
    @Field(type = FieldType.Text)
    String tagesTyp;

    String projektNummer;

    String projektName;

    String kreuzungsname;

    Boolean sonderzaehlung;

    Boolean kreisverkehr;

    List<Fahrzeug> kategorien;

    String status;

    String zaehlsituation;

    String zaehlsituationErweitert;

    int zaehlIntervall;

    String wetter;

    /**
     * 2x4h, 16h, 24h
     */
    String zaehldauer;

    String quelle;

    String kommentar;

    /**
     * Ferien, Schule
     */
    String schulZeiten;

    List<String> suchwoerter;

    List<String> customSuchwoerter;

    PkwEinheit pkwEinheit;

    List<String> geographie;

    List<Knotenarm> knotenarme;

    List<Fahrbeziehung> fahrbeziehungen;

    Boolean unreadMessagesMobilitaetsreferat;

    Boolean unreadMessagesDienstleister;
    String dienstleisterkennung;

    public CoordinateUtil.PositionUTM getPunktUtm() {
        return CoordinateUtil.transformFromWGS84ToUTM(this.punkt);
    }

}
