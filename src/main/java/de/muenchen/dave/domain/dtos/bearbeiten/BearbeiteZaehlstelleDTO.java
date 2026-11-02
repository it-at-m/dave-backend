package de.muenchen.dave.domain.dtos.bearbeiten;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

@Data
public class BearbeiteZaehlstelleDTO {

    String id;

    String nummer;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int stadtbezirkNummer;

    String stadtbezirk;

    @NotNull
    GeoPoint punkt;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    double lat;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    double lng;

    List<String> customSuchwoerter;

    List<BearbeiteZaehlungDTO> zaehlungen;

    String kommentar;

    Boolean sichtbarDatenportal;

}
