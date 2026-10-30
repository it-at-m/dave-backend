package de.muenchen.dave.domain.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Data;

// Definition of getter, setter, ...
@Data
public class EmailAddressDTO {

    private UUID id;
    private Long entityVersion;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private int participantId;
    @NotNull
    private String emailAddress;
}
