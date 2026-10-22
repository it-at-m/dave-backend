package de.muenchen.dave.domain.dtos.bearbeiten;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

@Data
public class BearbeiteZaehlstelleDTO {

    String id;

    String nummer;

    @NotNull
    int stadtbezirkNummer;

    String stadtbezirk;

    @NotNull
    GeoPoint punkt;

    double lat;

    double lng;

    List<String> customSuchwoerter;

    List<BearbeiteZaehlungDTO> zaehlungen;

    String kommentar;

    Boolean sichtbarDatenportal;

}
