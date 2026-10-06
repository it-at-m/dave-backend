package de.muenchen.dave.domain.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CsvDTO {
    @NotNull
    String csvAsString;
}
