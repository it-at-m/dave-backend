package de.muenchen.dave.domain.dtos.messstelle.auswertung;

import de.muenchen.dave.domain.dtos.laden.LadeZaehldatenSteplineDTO;
import lombok.Data;

@Data
public class AuswertungMessstelleWithFileDTO {

    private LadeZaehldatenSteplineDTO zaehldatenMessstellen;

    private String spreadsheetBase64Encoded;



}
