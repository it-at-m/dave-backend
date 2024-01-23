package de.muenchen.dave.domain.dtos;

import java.time.LocalDate;
import java.util.List;
import lombok.Data;

@Data
public class NichtPlausibleTageEaiRequestDTO {
    List<LocalDate> plausibleTage;
}
