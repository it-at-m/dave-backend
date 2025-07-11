package de.muenchen.dave.domain.model.messstelle;

import de.muenchen.dave.domain.dtos.messstelle.ReadMessfaehigkeitDTO;
import de.muenchen.dave.domain.enums.TagesTyp;
import de.muenchen.dave.services.messstelle.Zeitraum;
import java.util.List;
import java.util.Set;
import lombok.Data;

@Data
public class ValidateZeitraumAndTagesTypForMessstelleModel {
    private Zeitraum zeitraum;
    private String mstId;
    private Set<String> mqIds;
    private TagesTyp tagesTyp;
    private List<ReadMessfaehigkeitDTO> messfaehigkeiten;
}
