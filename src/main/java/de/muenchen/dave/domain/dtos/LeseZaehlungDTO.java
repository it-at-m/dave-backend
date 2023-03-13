package de.muenchen.dave.domain.dtos;

import de.muenchen.dave.domain.dtos.laden.ZeitauswahlDTO;
import de.muenchen.dave.domain.elasticsearch.Fahrbeziehung;
import de.muenchen.dave.domain.elasticsearch.Knotenarm;
import de.muenchen.dave.domain.elasticsearch.PkwEinheit;
import de.muenchen.dave.domain.enums.Fahrzeug;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;


@Data
public class LeseZaehlungDTO implements Serializable {

    String id;

    Date datum;

    int jahr;

    String monat;

    String jahreszeit;

    String zaehlart;

    Double lat;

    Double lng;

    /**
     * Wochenende, Wochentag, Feiertag
     */
    String tagesTyp;

    String projektNummer;

    String projektName;

    String kreuzungsname;

    Boolean sonderzaehlung;

    Boolean kreisverkehr;

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

    PkwEinheit pkwEinheit;

    List<Fahrzeug> kategorien;

    List<Knotenarm> knotenarme;

    List<Fahrbeziehung> fahrbeziehungen;

    ZeitauswahlDTO zeitauswahl;

    Boolean unreadMessagesMobilitaetsreferat;

    Boolean unreadMessagesDienstleister;

    String dienstleisterkennung;
}