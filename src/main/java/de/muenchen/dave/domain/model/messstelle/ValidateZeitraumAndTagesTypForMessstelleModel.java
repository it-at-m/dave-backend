package de.muenchen.dave.domain.model.messstelle;

import de.muenchen.dave.domain.enums.TagesTyp;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ValidateZeitraumAndTagesTypForMessstelleModel {
    private List<LocalDate> zeitraum;
    private String mstId;
    private TagesTyp tagesTyp;
}
