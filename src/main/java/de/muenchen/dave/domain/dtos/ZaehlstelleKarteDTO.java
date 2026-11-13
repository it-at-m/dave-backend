package de.muenchen.dave.domain.dtos;

import jakarta.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ZaehlstelleKarteDTO extends ErhebungsstelleKarteDTO {

    @java.io.Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    private String letzteZaehlungId;
    @NotNull
    private Set<ZaehlartenKarteDTO> zaehlartenKarte = new HashSet<>();

}
