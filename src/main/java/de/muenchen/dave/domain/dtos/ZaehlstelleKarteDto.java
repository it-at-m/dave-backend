package de.muenchen.dave.domain.dtos;

import jakarta.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ZaehlstelleKarteDto extends ErhebungsstelleKarteDto {

    @java.io.Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    private String letzteZaehlungId;
    @NotNull
    private Set<ZaehlartenKarteDto> zaehlartenKarte = new HashSet<>();

}
