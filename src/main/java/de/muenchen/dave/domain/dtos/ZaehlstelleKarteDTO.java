package de.muenchen.dave.domain.dtos;

import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ZaehlstelleKarteDTO extends ErhebungsstelleKarteDTO {

    private String letzteZaehlungId;

    private Set<ZaehlartenKarteDTO> zaehlartenKarte = new HashSet<>();

}
