package de.muenchen.dave.domain.dtos.laden.messwerte;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class ListBelastungsplanMessquerschnitteDTO {
    List<LadeBelastungsplanMessquerschnittDataDTO> ladeBelastungsplanMessquerschnittDataDTOList = new ArrayList<>();
    String strassenname;
    Integer totalKfz;
    Integer totalSv;
    Integer totalGv;
    BigDecimal totalPercentSv;
    BigDecimal totalPercentGv;
}
