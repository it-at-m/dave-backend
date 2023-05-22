package de.muenchen.dave.domain.dtos.external;

import de.muenchen.dave.domain.dtos.HochrechnungsfaktorDTO;
import de.muenchen.dave.domain.dtos.ZeitintervallDTO;
import lombok.Data;

import java.util.List;

@Data
public class ExternalFahrbeziehungDTO {

    String id;

    Boolean isKreuzung;

    // Kreuzung
    Integer von;

    Integer nach;

    // Kreisverkehr
    Integer knotenarm;

    Boolean hinein;

    Boolean heraus;

    Boolean vorbei;

    HochrechnungsfaktorDTO hochrechnungsfaktor;

    List<ZeitintervallDTO> zeitintervalle;

}
