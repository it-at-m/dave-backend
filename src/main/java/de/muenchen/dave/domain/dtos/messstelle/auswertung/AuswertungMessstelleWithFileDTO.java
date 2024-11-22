package de.muenchen.dave.domain.dtos.messstelle.auswertung;

import de.muenchen.dave.domain.dtos.laden.LadeZaehldatenSteplineDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatenSteplineForMessstelleDTO;
import lombok.Data;

import java.util.List;


@Data
public class AuswertungMessstelleWithFileDTO {

    private List<LadeZaehldatenSteplineForMessstelleDTO> zaehldatenMessstellen;

    private String spreadsheetBase64Encoded;

}
