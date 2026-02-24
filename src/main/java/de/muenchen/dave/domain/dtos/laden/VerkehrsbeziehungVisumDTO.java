package de.muenchen.dave.domain.dtos.laden;

import java.util.List;
import lombok.Data;

@Data
public class VerkehrsbeziehungVisumDTO {

    private Integer von;

    private Integer nach;

    private List<LadeZaehldatumDTO> zaehldaten;

}
