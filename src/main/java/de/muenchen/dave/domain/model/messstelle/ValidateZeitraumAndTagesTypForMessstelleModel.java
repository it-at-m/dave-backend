package de.muenchen.dave.domain.model.messstelle;

import de.muenchen.dave.domain.enums.TagesTyp;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;

@Data
public class ValidateZeitraumAndTagesTypForMessstelleModel {
    private List<LocalDate> zeitraum;
    private String mstId;
    private TagesTyp tagesTyp;
}
