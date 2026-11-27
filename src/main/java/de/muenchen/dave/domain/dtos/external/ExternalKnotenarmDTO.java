package de.muenchen.dave.domain.dtos.external;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ExternalKnotenarmDTO {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int nummer;

    String Strassenname;

    String filename;
}
