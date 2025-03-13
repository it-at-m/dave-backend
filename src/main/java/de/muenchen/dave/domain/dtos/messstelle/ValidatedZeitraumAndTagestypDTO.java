package de.muenchen.dave.domain.dtos.messstelle;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ValidatedZeitraumAndTagestypDTO {
    private Boolean isValid;
}
