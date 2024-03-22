package de.muenchen.dave.domain.dtos.laden.messwerte;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class LadeBelastungsplanMessquerschnittDataDTO {

    private String mqId;
    private Integer sumKfz;
    private Integer sumSv;
    private Integer sumGv;
    private Integer sumRad;
    private String direction;

    private BigDecimal percentSv;
    private BigDecimal percentGV;
}
