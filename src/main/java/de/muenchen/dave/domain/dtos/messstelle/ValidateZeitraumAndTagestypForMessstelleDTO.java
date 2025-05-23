package de.muenchen.dave.domain.dtos.messstelle;

import de.muenchen.dave.domain.enums.TagesTyp;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;

@Data
public class ValidateZeitraumAndTagestypForMessstelleDTO {
    @NotEmpty
    private List<@NotNull LocalDate> zeitraum;
    @NotEmpty
    private String mstId;
    @NotNull
    private TagesTyp tagesTyp;
}
