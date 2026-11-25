package de.muenchen.dave.domain.dtos.bearbeiten;

import de.muenchen.dave.domain.enums.Himmelsrichtung;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BearbeiteQuerungsverkehrDto extends BearbeiteBewegungsbeziehungDto {

    private Integer knotenarm;

    private Himmelsrichtung richtung;

}
