package de.muenchen.dave.domain.dtos.bearbeiten;

import lombok.Data;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

import java.util.List;


@Data
public class BearbeiteZaehlstelleDTO {

    String id;

    String nummer;

    int stadtbezirkNummer;

    String stadtbezirk;

    GeoPoint punkt;

    double lat;

    double lng;

    List<String> customSuchwoerter;

    List<BearbeiteZaehlungDTO> zaehlungen;

    String kommentar;

    Boolean sichtbarDatenportal;

}