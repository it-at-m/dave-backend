package de.muenchen.dave.domain.dtos.bearbeiten;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.muenchen.dave.configuration.json.EmptyListSerializer;
import de.muenchen.dave.domain.dtos.PkwEinheitDto;
import de.muenchen.dave.domain.enums.Fahrzeug;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

@Data
public class BearbeiteZaehlungDto {

    String id;

    LocalDate datum;

    String zaehlart;

    GeoPoint punkt;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    double lat;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    double lng;

    String tagesTyp;

    String monat;

    String jahreszeit;

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

    String schulZeiten;

    String kommentar;

    List<String> customSuchwoerter;

    PkwEinheitDto pkwEinheit;

    List<BearbeiteKnotenarmDto> knotenarme;

    @JsonSerialize(nullsUsing = EmptyListSerializer.class)
    List<BearbeiteLaengsverkehrDto> laengsverkehr;

    @JsonSerialize(nullsUsing = EmptyListSerializer.class)
    List<BearbeiteQuerungsverkehrDto> querungsverkehr;

    @JsonSerialize(nullsUsing = EmptyListSerializer.class)
    List<BearbeiteVerkehrsbeziehungDto> verkehrsbeziehungen;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    boolean unreadMessagesMobilitaetsreferat;

    String dienstleisterkennung;

}
