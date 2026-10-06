package de.muenchen.dave.domain.dtos.laden.messwerte;

import de.muenchen.dave.domain.dtos.laden.LadeZaehldatenHeatmapDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatenSteplineDTO;
import de.muenchen.dave.domain.enums.TagesTyp;
import java.io.Serializable;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LadeProcessedMesswerteDTO implements Serializable {

    @NotNull
    TagesTyp tagesTyp;
    @NotNull
    LadeMesswerteListenausgabeDTO zaehldatenTable;
    @NotNull
    LadeZaehldatenSteplineDTO zaehldatenStepline;
    @NotNull
    LadeZaehldatenHeatmapDTO zaehldatenHeatmap;
    @NotNull
    BelastungsplanMessquerschnitteDTO belastungsplanMessquerschnitte;
    @NotNull
    private Long requestedMeasuringDays;
    @NotNull
    private Integer includedMeasuringDays;
}
