package de.muenchen.dave.domain.dtos.messstelle;

import de.muenchen.dave.domain.dtos.ErhebungsstelleKarteDTO;
import de.muenchen.dave.domain.enums.MessstelleStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MessstelleKarteDTO extends ErhebungsstelleKarteDTO {

    @java.io.Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    private MessstelleStatus status;

}
