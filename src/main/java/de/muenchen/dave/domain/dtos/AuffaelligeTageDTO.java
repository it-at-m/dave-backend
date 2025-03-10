package de.muenchen.dave.domain.dtos;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class AuffaelligeTageDTO {
    List<LocalDate> auffaelligeTage;
}
