package de.muenchen.dave.domain.dtos.messstelle;

import de.muenchen.dave.domain.enums.TagesTyp;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ValidateZeitraumAndTagestypForMessstelleDTO {
    @NotNull
    private List<LocalDate> zeitraum;
    @NotNull
    private String mstId;
    private TagesTyp tagesTyp;
}
