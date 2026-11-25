package de.muenchen.dave.domain.dtos.laden;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import lombok.Data;

@Data
public class LadeProcessedZaehldatenDto implements Serializable {

    @java.io.Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    LadeZaehldatenTableDto zaehldatenTable;
    @NotNull
    LadeZaehldatenStepLineDto zaehldatenStepline;
    @NotNull
    LadeZaehldatenHeatmapDto zaehldatenHeatmap;
    @NotNull
    AbstractLadeBelastungsplanDto<?> zaehldatenBelastungsplan;
    @NotNull
    LadeZaehldatenZeitreiheDto zaehldatenZeitreihe;

}
