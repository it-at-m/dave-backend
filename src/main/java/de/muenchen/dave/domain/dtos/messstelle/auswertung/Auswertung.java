package de.muenchen.dave.domain.dtos.messstelle.auswertung;

import de.muenchen.dave.geodateneai.gen.model.TagesaggregatDto;
import de.muenchen.dave.services.messstelle.Zeitraum;
import lombok.Data;

@Data
public class Auswertung {

    private String objectId;
    private Zeitraum zeitraum;
    private TagesaggregatDto daten;

}
