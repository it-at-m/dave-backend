package de.muenchen.dave.migrationespg.domain.dtos.bearbeiten;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Data;

@Data
public class BackendIdDTO {

    @NotNull
    private UUID id;

}
