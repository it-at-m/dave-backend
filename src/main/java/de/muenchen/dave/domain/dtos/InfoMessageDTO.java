package de.muenchen.dave.domain.dtos;

import java.time.LocalDate;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InfoMessageDTO {

    @NotNull
    private UUID id;
    @NotNull
    private String content;
    private LocalDate gueltigVon;
    private LocalDate gueltigBis;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean gueltig;
    private boolean aktiv;

}
