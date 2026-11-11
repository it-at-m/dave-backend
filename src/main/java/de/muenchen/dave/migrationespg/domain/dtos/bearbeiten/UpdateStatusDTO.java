package de.muenchen.dave.migrationespg.domain.dtos.bearbeiten;

import java.util.UUID;
import lombok.Data;

@Data
public class UpdateStatusDTO {

    private UUID zaehlungId;

    private String status;

    private String dienstleisterkennung;

}
