package de.muenchen.dave.domain.dtos.bearbeiten;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BackendIdDto {

    @NotNull
    private String id;

}
