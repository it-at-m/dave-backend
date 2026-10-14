package de.muenchen.dave.domain.dtos.laden;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import lombok.Data;

@Data
public class LadeProcessedZaehldatenDTO implements Serializable {

    @NotNull
    LadeZaehldatenTableDTO zaehldatenTable;
    @NotNull
    LadeZaehldatenSteplineDTO zaehldatenStepline;
    @NotNull
    LadeZaehldatenHeatmapDTO zaehldatenHeatmap;
    @NotNull
    AbstractLadeBelastungsplanDTO<?> zaehldatenBelastungsplan;
    @NotNull
    LadeZaehldatenZeitreiheDTO zaehldatenZeitreihe;

}
