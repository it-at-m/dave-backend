package de.muenchen.dave.domain.dtos.messstelle;

import de.muenchen.dave.domain.dtos.ErhebungsstelleKarteDTO;
import de.muenchen.dave.domain.enums.MessstelleStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MessstelleKarteDTO extends ErhebungsstelleKarteDTO {

    private MessstelleStatus status;

}
