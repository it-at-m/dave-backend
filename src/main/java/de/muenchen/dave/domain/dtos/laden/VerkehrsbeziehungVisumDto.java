package de.muenchen.dave.domain.dtos.laden;

import java.util.List;
import lombok.Data;

@Data
public class VerkehrsbeziehungVisumDto {

    private Integer von;

    private Integer nach;

    private List<LadeZaehldatumDto> zaehldaten;

}
