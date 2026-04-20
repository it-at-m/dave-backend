package de.muenchen.dave.domain.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;
import lombok.Data;

@Data
public class ResetAuffaelligkeitenDTO {

    @NotNull
    @Past
    private LocalDate dateToReset;

}
