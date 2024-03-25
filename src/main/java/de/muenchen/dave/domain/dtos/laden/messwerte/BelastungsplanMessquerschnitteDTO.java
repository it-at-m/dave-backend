package de.muenchen.dave.domain.dtos.laden.messwerte;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class BelastungsplanMessquerschnitteDTO {
    List<LadeBelastungsplanMessquerschnittDataDTO> ladeBelastungsplanMessquerschnittDataDTOList = new ArrayList<>();
    String strassenname;
    String messstelleId;
    Integer stadtbezirkNummer;
    Integer totalKfz;
    Integer totalSv;
    Integer totalGv;
    Integer totalRad;
    BigDecimal totalPercentSv;
    BigDecimal totalPercentGv;
}
