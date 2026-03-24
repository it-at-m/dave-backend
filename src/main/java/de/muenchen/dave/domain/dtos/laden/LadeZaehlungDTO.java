package de.muenchen.dave.domain.dtos.laden;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.muenchen.dave.config.EmptyListSerializer;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteLaengsverkehrDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteQuerungsverkehrDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteVerkehrsbeziehungDTO;
import de.muenchen.dave.domain.enums.Fahrzeug;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;

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

    @JsonSerialize(nullsUsing = EmptyListSerializer.class)
    List<BearbeiteLaengsverkehrDTO> laengsverkehr;

    @JsonSerialize(nullsUsing = EmptyListSerializer.class)
    List<BearbeiteQuerungsverkehrDTO> querungsverkehr;

    @JsonSerialize(nullsUsing = EmptyListSerializer.class)
    List<BearbeiteVerkehrsbeziehungDTO> verkehrsbeziehungen;

    ZeitauswahlDTO zeitauswahl;

}
