package de.muenchen.dave.domain.dtos.laden.messwerte;

import de.muenchen.dave.domain.dtos.laden.LadeZaehldatenHeatmapDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatenSteplineDTO;
import de.muenchen.dave.domain.enums.TagesTyp;
import java.io.Serializable;
import lombok.Data;

@Data
public class LadeProcessedMesswerteDTO implements Serializable {

    TagesTyp tagesTyp;

    LadeMesswerteListenausgabeDTO zaehldatenTable;

    LadeZaehldatenSteplineDTO zaehldatenStepline;

    LadeZaehldatenHeatmapDTO zaehldatenHeatmap;

    BelastungsplanMessquerschnitteDTO belastungsplanMessquerschnitte;

    private Long requestedMeasuringDays;
    private Integer includedMeasuringDays;
}
