package de.muenchen.dave.domain.dtos.messstelle;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ValidatedZeitraumAndTagestypDTO {
    @NotNull
    private Boolean isValid;
}
