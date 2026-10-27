package de.muenchen.dave.domain.dtos.external;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.muenchen.dave.configuration.json.EmptyListSerializer;
import de.muenchen.dave.domain.enums.Fahrzeug;
import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    boolean sonderzaehlung;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    boolean kreisverkehr;

    List<Fahrzeug> kategorien;

    String zaehlsituation;

    String zaehlsituationErweitert;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
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

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    boolean unreadMessagesDienstleister;

    String dienstleisterkennung;

}
