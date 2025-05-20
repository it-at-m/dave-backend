package de.muenchen.dave.domain.dtos;

import de.muenchen.dave.domain.enums.TagesTyp;
import lombok.Data;

@Data
public class ChosenTagesTypValidEaiRequestDTO {
    String startDate;
    String endDate;
    TagesTyp tagesTyp;
}
