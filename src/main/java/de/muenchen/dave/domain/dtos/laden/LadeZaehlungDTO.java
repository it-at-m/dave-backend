package de.muenchen.dave.domain.dtos.laden;

import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteFahrbeziehungDTO;
import de.muenchen.dave.domain.enums.Fahrzeug;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class LadeZaehlungDTO {

    String id;

    LocalDate datum;

    String zaehlart;

    double lat;

    double lng;

    String quelle;

    String projektNummer;

    String projektName;

    String kreuzungsname;

    Boolean sonderzaehlung;

    Boolean kreisverkehr;

    String zaehlsituation;

    String zaehlsituationErweitert;

    Integer zaehlIntervall;

    String wetter;

    String zaehldauer;

    String kommentar;

    List<LadeKnotenarmDTO> knotenarme;

    List<Fahrzeug> kategorien;

    List<BearbeiteFahrbeziehungDTO> fahrbeziehungen;

    FahrbeziehungenDTO auswaehlbareFahrbeziehungen;

    ZeitauswahlDTO zeitauswahl;

}
