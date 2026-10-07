package de.muenchen.dave.domain.dtos.init;

import lombok.AllArgsConstructor;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@AllArgsConstructor
public class ZaehlstelleConfigurationDTO {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean automaticNumberAssignment;

    private String linkDocumentationCsvFileForUploadZaehlung;
}
