package de.muenchen.dave.domain.dtos.laden;

import lombok.Data;

@Data
public class LadeSpitzenstundeDataDTO extends LadeZaehldatumDTO {

    private Integer von;

    private Integer nach;

}
