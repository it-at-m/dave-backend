package de.muenchen.dave.migrationespg.domain.dtos;

import de.muenchen.dave.domain.dtos.ErhebungsstelleKarteDTO;
import de.muenchen.dave.domain.dtos.ZaehlartenKarteDTO;
import jakarta.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ZaehlstelleKarteDTO extends ErhebungsstelleKarteDTO {

    @java.io.Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    private UUID letzteZaehlungId;
    @NotNull
    private Set<ZaehlartenKarteDTO> zaehlartenKarte = new HashSet<>();

}
