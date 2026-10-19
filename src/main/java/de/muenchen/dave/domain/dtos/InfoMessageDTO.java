package de.muenchen.dave.domain.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Data;

@Data
public class InfoMessageDTO {

    @NotNull
    private UUID id;
    @NotNull
    private String content;
    @NotNull
    private LocalDate gueltigVon;
    @NotNull
    private LocalDate gueltigBis;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean gueltig;
    private boolean aktiv;

}
