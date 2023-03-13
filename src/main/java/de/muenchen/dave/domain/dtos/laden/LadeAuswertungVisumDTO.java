package de.muenchen.dave.domain.dtos.laden;

import lombok.Data;

import java.util.List;

@Data
public class LadeAuswertungVisumDTO {

    private List<LadeZaehlstelleVisumDTO> zaehlstellen;

}
