package de.muenchen.dave.domain.dtos.bearbeiten;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;

@Data
public class BearbeiteZaehlstelleDTO {

    String id;

    String nummer;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int stadtbezirkNummer;

    String stadtbezirk;

    /*
      @TODO : Punkt noch zugunsten latitude/longitude auch in den Frontends auflösen
    */
    GeoPointDTOAdapter punkt = new GeoPointDTOAdapter();

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    double lat;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    double lng;

    List<String> customSuchwoerter;

    List<BearbeiteZaehlungDTO> zaehlungen;

    String kommentar;

    Boolean sichtbarDatenportal;

}
