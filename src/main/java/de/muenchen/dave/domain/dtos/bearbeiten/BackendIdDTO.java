package de.muenchen.dave.domain.dtos.bearbeiten;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BackendIdDTO {

    @NotNull
    private String id;

}
