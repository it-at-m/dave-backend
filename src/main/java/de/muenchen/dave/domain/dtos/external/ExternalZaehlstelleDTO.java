package de.muenchen.dave.domain.dtos.external;

import lombok.Data;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

import java.util.List;

@Data
public class ExternalZaehlstelleDTO {

    String id;

    String nummer;

    String stadtbezirk;

    GeoPoint punkt;

    String kommentar;

    List<ExternalZaehlungDTO> zaehlungen;

    Boolean sichtbarDatenportal;

}
