package de.muenchen.dave.domain.dtos.bearbeiten;

import lombok.Data;

@Data
public class UpdateStatusDTO {

    private String zaehlungId;

    private String status;

    private String dienstleisterkennung;

}
