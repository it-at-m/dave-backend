package de.muenchen.dave.domain.dtos;

import java.io.Serializable;
import java.util.List;
import lombok.Data;

@Data
public class LeseZaehlstelleDTO implements Serializable {

    String id;

    String nummer;

    String name;

    Integer stadtbezirkNummer;

    Double lat;

    Double lng;

    String kommentar;

    List<String> suchwoerter;

    List<LeseZaehlungDTO> zaehlungen;

    Boolean sichtbarDatenportal;
}
