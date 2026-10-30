package de.muenchen.dave.domain.dtos.messstelle;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;

@Data
public class AuffaelligeTageDTO implements Serializable {

    @java.io.Serial
    private static final long serialVersionUID = 1L;
    private List<LocalDate> auffaelligeTage;
}
