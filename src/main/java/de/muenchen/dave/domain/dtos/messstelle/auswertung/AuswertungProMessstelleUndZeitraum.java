package de.muenchen.dave.domain.dtos.messstelle.auswertung;

import de.muenchen.dave.geodateneai.gen.model.TagesaggregatResponseDto;
import de.muenchen.dave.services.messstelle.Zeitraum;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class AuswertungProMessstelleUndZeitraum extends TagesaggregatResponseDto {

    private String mstId;

    private Zeitraum zeitraum;
}
