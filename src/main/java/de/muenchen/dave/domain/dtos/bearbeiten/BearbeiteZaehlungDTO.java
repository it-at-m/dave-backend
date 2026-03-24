package de.muenchen.dave.domain.dtos.bearbeiten;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.muenchen.dave.configuration.json.EmptyListSerializer;
import de.muenchen.dave.domain.dtos.PkwEinheitDTO;
import de.muenchen.dave.domain.enums.Fahrzeug;
import de.muenchen.dave.validation.BearbeiteZaehlungValid;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

@Data
@BearbeiteZaehlungValid
public class BearbeiteZaehlungDTO {

    String id;

    LocalDate datum;

    String zaehlart;

    GeoPoint punkt;

    double lat;

    double lng;

    String tagesTyp;

    String monat;

    String jahreszeit;

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

    boolean unreadMessagesMobilitaetsreferat;

    String dienstleisterkennung;

}
