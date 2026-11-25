package de.muenchen.dave.domain.dtos.laden;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.muenchen.dave.configuration.json.EmptyListSerializer;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteLaengsverkehrDto;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteQuerungsverkehrDto;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteVerkehrsbeziehungDto;
import de.muenchen.dave.domain.enums.Fahrzeug;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;

@Data
public class LadeZaehlungDTO {

    String id;

    LocalDate datum;

    String zaehlart;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    double lat;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
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
    List<BearbeiteLaengsverkehrDto> laengsverkehr;

    @JsonSerialize(nullsUsing = EmptyListSerializer.class)
    List<BearbeiteQuerungsverkehrDto> querungsverkehr;

    @JsonSerialize(nullsUsing = EmptyListSerializer.class)
    List<BearbeiteVerkehrsbeziehungDto> verkehrsbeziehungen;

    ZeitauswahlDTO zeitauswahl;

}
