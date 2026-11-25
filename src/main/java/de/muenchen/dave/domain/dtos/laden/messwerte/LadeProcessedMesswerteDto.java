package de.muenchen.dave.domain.dtos.laden.messwerte;

import de.muenchen.dave.domain.dtos.laden.LadeZaehldatenHeatmapDto;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatenStepLineDto;
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
    LadeZaehldatenStepLineDto zaehldatenStepline;
    @NotNull
    LadeZaehldatenHeatmapDto zaehldatenHeatmap;
    @NotNull
    BelastungsplanMessquerschnitteDto belastungsplanMessquerschnitte;
    private Long requestedMeasuringDays;
    private Integer includedMeasuringDays;
}
