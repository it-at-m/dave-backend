package de.muenchen.dave.domain.elasticsearch.detektor;

import de.muenchen.dave.domain.enums.Fahrzeugklasse;
import de.muenchen.dave.domain.enums.MessstelleStatus;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.GeoPointField;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Document(indexName = "messstelle")
public class Messstelle {

    @Id
    String id;

    String mstId;

    String name;

    MessstelleStatus status;

    @Field(type = FieldType.Date, pattern = "dd.MM.uuuu")
    LocalDate realisierungsdatum;

    @Field(type = FieldType.Date, pattern = "dd.MM.uuuu")
    LocalDate abbaudatum;

    Integer stadtbezirkNummer;

    String bemerkung;

    Fahrzeugklasse fahrzeugklasse;

    String detektierteVerkehrsarten;

    String hersteller;

    @Field(type = FieldType.Date, pattern = "dd.MM.uuuu")
    LocalDate datumLetztePlausibleMessung;
    /**
     * In WGS84-Koordinatendarstellung.
     */
    @GeoPointField
    GeoPoint punkt;

    /**
     * Steuert die Sichtbarkeit der Messstelle im Datenportal.
     */
    Boolean sichtbarDatenportal = false;
    Boolean geprueft = false;
    String kommentar;
    String standort;
    List<String> suchwoerter;
    List<String> customSuchwoerter;
    List<Messquerschnitt> messquerschnitte = new ArrayList<>();
    List<Messfaehigkeit> messfaehigkeiten = new ArrayList<>();
}
