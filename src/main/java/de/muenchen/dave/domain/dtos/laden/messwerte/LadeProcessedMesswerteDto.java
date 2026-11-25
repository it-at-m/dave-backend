package de.muenchen.dave.domain.dtos.laden.messwerte;

import de.muenchen.dave.domain.dtos.laden.LadeZaehldatenHeatmapDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatenSteplineDTO;
import de.muenchen.dave.domain.enums.TagesTyp;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import lombok.Data;

@Data
public class LadeProcessedMesswerteDto implements Serializable {

    TagesTyp tagesTyp;
    @NotNull
    LadeMesswerteListenausgabeDto zaehldatenTable;
    @NotNull
    LadeZaehldatenSteplineDTO zaehldatenStepline;
    @NotNull
    LadeZaehldatenHeatmapDTO zaehldatenHeatmap;
    @NotNull
    BelastungsplanMessquerschnitteDto belastungsplanMessquerschnitte;
    private Long requestedMeasuringDays;
    private Integer includedMeasuringDays;
}
