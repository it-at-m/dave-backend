package de.muenchen.dave.domain.dtos.bearbeiten;

import de.muenchen.dave.domain.enums.Bewegungsrichtung;
import de.muenchen.dave.domain.enums.Himmelsrichtung;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BearbeiteLaengsverkehrDTO extends BearbeiteBewegungsbeziehungDTO {

    private Integer knotenarm;

    private Bewegungsrichtung richtung;

    private Himmelsrichtung strassenseite;

}
