package de.muenchen.dave.domain.dtos.external;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.muenchen.dave.config.EmptyListSerializer;
import de.muenchen.dave.domain.enums.Fahrzeug;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

@Data
public class ExternalZaehlungDTO {

    String id;

    LocalDate datum;

    String zaehlart;

    GeoPoint punkt;

    String projektNummer;

    String projektName;

    String kreuzungsname;

    boolean sonderzaehlung;

    boolean kreisverkehr;

    List<Fahrzeug> kategorien;

    String zaehlsituation;

    String zaehlsituationErweitert;

    int zaehlIntervall;

    String wetter;

    String status;

    String quelle;

    String zaehldauer;

    String kommentar;

    List<ExternalKnotenarmDTO> knotenarme;

    @JsonSerialize(nullsUsing = EmptyListSerializer.class)
    List<ExternalLaengsverkehrDTO> laengsverkehr;

    @JsonSerialize(nullsUsing = EmptyListSerializer.class)
    List<ExternalQuerungsverkehrDTO> querungsverkehr;

    @JsonSerialize(nullsUsing = EmptyListSerializer.class)
    List<ExternalVerkehrsbeziehungDTO> verkehrsbeziehungen;

    // Zählstelle
    String zaehlstelleNummer;

    String zaehlstelleStadtbezirk;

    GeoPoint zaehlstellePunkt;

    String zaehlstelleKommentar;

    boolean unreadMessagesDienstleister;

    String dienstleisterkennung;

}
