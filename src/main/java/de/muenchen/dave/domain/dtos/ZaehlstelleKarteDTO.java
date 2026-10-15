package de.muenchen.dave.domain.dtos;

import java.util.HashSet;
import java.util.Set;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ZaehlstelleKarteDTO extends ErhebungsstelleKarteDTO {

    @NotNull
    private String letzteZaehlungId;
    @NotNull
    private Set<ZaehlartenKarteDTO> zaehlartenKarte = new HashSet<>();

}
