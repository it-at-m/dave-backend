package de.muenchen.dave.domain.dtos.laden.messwerte;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class ListBelastungsplanMessquerschnitteDTO {
    List<LadeBelastungsplanMessquerschnittDataDTO> ladeBelastungsplanMessquerschnittDataDTOList = new ArrayList<>();
    String strassenname;
}
