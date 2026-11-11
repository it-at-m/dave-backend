package de.muenchen.dave.migrationespg.domain.dtos.external;

import java.util.List;
import java.util.UUID;
import lombok.Data;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

@Data
public class ExternalZaehlstelleDTO {

    UUID id;

    String nummer;

    String stadtbezirk;

    GeoPoint punkt;

    String kommentar;

    List<ExternalZaehlungDTO> zaehlungen;

    Boolean sichtbarDatenportal;

}
