package de.muenchen.dave.domain.dtos.laden;

import java.io.Serializable;

import jakarta.validation.constraints.NotNull;
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
