package de.muenchen.dave.domain.dtos.laden;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import lombok.Data;

@Data
public class LadeProcessedZaehldatenDTO implements Serializable {

    @java.io.Serial
    private static final long serialVersionUID = 1L;

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
