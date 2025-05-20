package de.muenchen.dave.domain.dtos.laden;

import lombok.Data;

import java.util.List;

@Data
public class FahrbeziehungVisumDTO {

    private Integer von;

    private Integer nach;

    private List<LadeZaehldatumDTO> zaehldaten;

}
