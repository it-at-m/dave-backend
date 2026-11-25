package de.muenchen.dave.domain.dtos.messstelle;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ValidatedZeitraumAndTagestypDto {
    @NotNull
    private Boolean isValid;
}
