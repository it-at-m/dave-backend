package de.muenchen.dave.domain.dtos.laden;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LadeKnotenarmDTO {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int nummer;
    @NotNull
    String strassenname;

}
