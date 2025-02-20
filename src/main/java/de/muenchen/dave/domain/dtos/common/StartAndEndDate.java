package de.muenchen.dave.domain.dtos.common;

import lombok.Data;

import java.time.LocalDate;

// Definition of getter, setter, ...
@Data
public class StartAndEndDate {

    private LocalDate startDate;
    private LocalDate endDate;
}
