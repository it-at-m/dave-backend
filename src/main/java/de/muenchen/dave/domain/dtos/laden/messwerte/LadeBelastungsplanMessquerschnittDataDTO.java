package de.muenchen.dave.domain.dtos.laden.messwerte;

import java.io.Serializable;
import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LadeBelastungsplanMessquerschnittDataDTO implements Serializable {

    @NotNull
    private String mqId;
    @NotNull
    private Integer sumKfz;
    @NotNull
    private Integer sumSv;
    @NotNull
    private Integer sumGv;
    @NotNull
    private Integer sumRad;
    @NotNull
    private String direction;

    @NotNull
    private BigDecimal percentSv;
    @NotNull
    private BigDecimal percentGV;
}
