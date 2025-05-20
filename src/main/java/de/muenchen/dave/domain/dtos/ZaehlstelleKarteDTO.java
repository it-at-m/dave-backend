package de.muenchen.dave.domain.dtos;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
public class ZaehlstelleKarteDTO extends ErhebungsstelleKarteDTO {

    private String letzteZaehlungId;

    private Set<ZaehlartenKarteDTO> zaehlartenKarte = new HashSet<>();

}
