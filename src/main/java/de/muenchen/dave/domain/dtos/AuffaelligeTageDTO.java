package de.muenchen.dave.domain.dtos;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class AuffaelligeTageDTO {
    private List<LocalDate> auffaelligeTage;
}
