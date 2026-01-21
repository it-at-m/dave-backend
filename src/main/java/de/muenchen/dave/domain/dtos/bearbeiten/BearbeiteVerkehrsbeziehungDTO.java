package de.muenchen.dave.domain.dtos.bearbeiten;

import de.muenchen.dave.domain.dtos.HochrechnungsfaktorDTO;

import de.muenchen.dave.domain.enums.Himmelsrichtung;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BearbeiteVerkehrsbeziehungDTO extends BearbeiteBewegungsbeziehungDTO {

    Boolean isKreuzung;

    // Kreuzung
    Integer von;

    Integer nach;

    // Kreisverkehr
    Integer knotenarm;

    Boolean hinein;

    Boolean heraus;

    Boolean vorbei;

    Himmelsrichtung strassenseite;

    // Knoten-Kanten-Modell
    String vonknotvonstrnr;

    String nachknotvonstrnr;

    String von_strnr;

    String vonknotennachstrnr;

    String nachknotnachstrnr;

    String nach_strnr;

    HochrechnungsfaktorDTO hochrechnungsfaktor;

}
