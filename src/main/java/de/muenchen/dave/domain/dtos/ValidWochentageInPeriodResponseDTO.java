package de.muenchen.dave.domain.dtos;

import lombok.Data;

@Data
public class ValidWochentageInPeriodResponseDTO {
    Integer numberOfValidTagesTypDiMiDo;
    Integer numberOfValidTagesTypMoFr;
    Integer numberOfValidTagesTypSamstag;
    Integer numberOfValidTagesTypSonntagFeiertag;
    Integer numberOfValidTagesTypWerktagFerien;
    Integer numberOfValidTagesTypMoSo;
}
