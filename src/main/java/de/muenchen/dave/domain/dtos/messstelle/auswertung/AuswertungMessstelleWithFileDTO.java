package de.muenchen.dave.domain.dtos.messstelle.auswertung;

import de.muenchen.dave.domain.dtos.laden.LadeZaehldatenSteplineDTO;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AuswertungMessstelleWithFileDTO {

    @NotNull
    private LadeZaehldatenSteplineDTO zaehldatenMessstellen;

    @NotNull
    private String spreadsheetBase64Encoded;

}
