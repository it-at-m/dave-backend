package de.muenchen.dave.domain.dtos;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Data;

@Data
public class ResetAuffaelligkeitenDTO {

    @NotNull
    private LocalDate dateToReset;

}
