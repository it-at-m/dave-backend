package de.muenchen.dave.domain.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Data;

@Data
public class InfoMessageDto {

    private UUID id;
    private String content;
    private LocalDate gueltigVon;
    private LocalDate gueltigBis;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean gueltig;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean aktiv;

}
