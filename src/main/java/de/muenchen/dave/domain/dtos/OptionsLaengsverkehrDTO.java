package de.muenchen.dave.domain.dtos;

import de.muenchen.dave.domain.enums.Bewegungsrichtung;
import de.muenchen.dave.domain.enums.Himmelsrichtung;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class OptionsLaengsverkehrDTO extends OptionsBewegungsbeziehungDTO {

    private Integer knotenarm;

    private Bewegungsrichtung richtung;

    private Himmelsrichtung strassenseite;

}
