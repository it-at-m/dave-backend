package de.muenchen.dave.domain.dtos;

import lombok.Data;

@Data
public class ValidWochentageInPeriodEaiRequestDTO {
    String startDate;
    String endDate;

    String messstelleId;
}
