package de.muenchen.dave.domain.dtos.messstelle;

import java.time.LocalDate;
import java.util.List;
import lombok.Data;

@Data
public class AuffaelligeTageDTO {
    private List<LocalDate> auffaelligeTage;
}
