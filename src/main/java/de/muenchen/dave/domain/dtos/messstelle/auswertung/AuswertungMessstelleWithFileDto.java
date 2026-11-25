package de.muenchen.dave.domain.dtos.messstelle.auswertung;

import de.muenchen.dave.domain.dtos.laden.LadeZaehldatenStepLineDto;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AuswertungMessstelleWithFileDto {

    @NotNull
    private LadeZaehldatenStepLineDto zaehldatenMessstellen;

    @NotNull
    private String spreadsheetBase64Encoded;

}
