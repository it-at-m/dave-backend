package de.muenchen.dave.domain.dtos.external;

import java.util.List;
import lombok.Data;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

@Data
public class ExternalZaehlstelleDto {

    String id;

    String nummer;

    String stadtbezirk;

    GeoPoint punkt;

    String kommentar;

    List<ExternalZaehlungDto> zaehlungen;

    Boolean sichtbarDatenportal;

}
