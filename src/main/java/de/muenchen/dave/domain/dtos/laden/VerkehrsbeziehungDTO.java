package de.muenchen.dave.domain.dtos.laden;

import de.muenchen.dave.domain.enums.Himmelsrichtung;
import lombok.Data;

@Data
public class VerkehrsbeziehungDTO {
    private int von;
    private int nach;
    private Himmelsrichtung strassenseite;

}
