package de.muenchen.dave.domain.dtos.messstelle.auswertung;

import lombok.Data;

import java.util.List;


@Data
public class AuswertungMessstelleWithFileDTO {

    private List<AuswertungMessstelle> auswertungMessstelle;

    private String spreadsheetBase64Encoded;

}
