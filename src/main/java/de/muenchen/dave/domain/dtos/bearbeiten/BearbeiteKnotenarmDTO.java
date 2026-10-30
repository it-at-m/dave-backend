package de.muenchen.dave.domain.dtos.bearbeiten;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BearbeiteKnotenarmDTO {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int nummer;
    @NotNull
    String Strassenname;

}
