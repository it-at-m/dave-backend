package de.muenchen.dave.domain.dtos.external;

import java.util.List;
import lombok.Data;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

@Data
public class ExternalZaehlstelleDTO {

    String id;

    String nummer;

    Integer stadtbezirkNummer;

    GeoPoint punkt;

    String kommentar;

    List<ExternalZaehlungDTO> zaehlungen;

    Boolean sichtbarDatenportal;

}
