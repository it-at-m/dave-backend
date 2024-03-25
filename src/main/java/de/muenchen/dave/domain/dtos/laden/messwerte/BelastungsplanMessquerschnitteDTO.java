package de.muenchen.dave.domain.dtos.laden.messwerte;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class BelastungsplanMessquerschnitteDTO implements Serializable {
    private List<LadeBelastungsplanMessquerschnittDataDTO> ladeBelastungsplanMessquerschnittDataDTOList;
    private String strassenname;
    private String mstId;
    private Integer stadtbezirkNummer;
    private Integer totalKfz;
    private Integer totalSv;
    private Integer totalGv;
    private Integer totalRad;
    private BigDecimal totalPercentSv;
    private BigDecimal totalPercentGv;
}
