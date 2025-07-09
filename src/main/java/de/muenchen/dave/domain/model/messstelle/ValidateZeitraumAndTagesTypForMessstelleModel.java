package de.muenchen.dave.domain.model.messstelle;

import de.muenchen.dave.domain.dtos.messstelle.ReadMessfaehigkeitDTO;
import de.muenchen.dave.domain.enums.TagesTyp;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import lombok.Data;

@Data
public class ValidateZeitraumAndTagesTypForMessstelleModel {
    private List<LocalDate> zeitraum;
    private String mstId;
    private Set<String> mqIds;
    private TagesTyp tagesTyp;
    private List<ReadMessfaehigkeitDTO> messfaehigkeiten;
}
