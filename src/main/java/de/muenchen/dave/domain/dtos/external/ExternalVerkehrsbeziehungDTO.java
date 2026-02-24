package de.muenchen.dave.domain.dtos.external;

import de.muenchen.dave.domain.dtos.HochrechnungsfaktorDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ExternalVerkehrsbeziehungDTO extends ExternalBewegungsbeziehungDTO {

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

}
