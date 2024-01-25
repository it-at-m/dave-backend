package de.muenchen.dave.domain.dtos;

import java.time.LocalDate;
import java.util.List;
import lombok.Data;

@Data
public class NichtPlausibleTageResponseDTO {
    List<LocalDate> nichtPlausibleTage;
}
