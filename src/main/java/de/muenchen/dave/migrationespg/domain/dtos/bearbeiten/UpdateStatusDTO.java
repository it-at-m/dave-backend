package de.muenchen.dave.migrationespg.domain.dtos.bearbeiten;

import de.muenchen.dave.domain.enums.Status;
import java.util.UUID;
import lombok.Data;

@Data
public class UpdateStatusDTO {

    private UUID zaehlungId;

    private Status status;

    private String dienstleisterkennung;

}
