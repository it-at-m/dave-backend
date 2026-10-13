package de.muenchen.dave.domain.dtos.external;

import de.muenchen.dave.domain.enums.Himmelsrichtung;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ExternalQuerungsverkehrDTO extends ExternalBewegungsbeziehungDTO {

    private Integer knotenarm;

    private Himmelsrichtung richtung;

}
