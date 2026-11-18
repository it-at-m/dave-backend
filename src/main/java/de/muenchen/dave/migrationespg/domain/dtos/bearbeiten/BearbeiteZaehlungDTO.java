package de.muenchen.dave.migrationespg.domain.dtos.bearbeiten;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.muenchen.dave.configuration.json.EmptyListSerializer;
import de.muenchen.dave.domain.dtos.PkwEinheitDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteKnotenarmDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteLaengsverkehrDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteQuerungsverkehrDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteVerkehrsbeziehungDTO;
import de.muenchen.dave.domain.enums.Fahrzeug;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.Data;

@Data
public class BearbeiteZaehlungDTO {

    UUID id;

    LocalDate datum;

    String zaehlart;

    /*
      @TODO : Punkt noch zugunsten latitude/longitude auch in den Frontends auflösen
    */
    GeoPointDTOAdapter punkt = new GeoPointDTOAdapter();

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

    Set<Fahrzeug> kategorien;

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

    PkwEinheitDTO pkwEinheit;

    List<BearbeiteKnotenarmDTO> knotenarme;

    @JsonSerialize(nullsUsing = EmptyListSerializer.class)
    List<BearbeiteLaengsverkehrDTO> laengsverkehr;

    @JsonSerialize(nullsUsing = EmptyListSerializer.class)
    List<BearbeiteQuerungsverkehrDTO> querungsverkehr;

    @JsonSerialize(nullsUsing = EmptyListSerializer.class)
    List<BearbeiteVerkehrsbeziehungDTO> verkehrsbeziehungen;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    boolean unreadMessagesMobilitaetsreferat;

    String dienstleisterkennung;

}
