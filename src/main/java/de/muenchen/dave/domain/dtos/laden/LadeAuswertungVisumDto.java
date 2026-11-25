package de.muenchen.dave.domain.dtos.laden;

import java.util.List;
import lombok.Data;

@Data
public class LadeAuswertungVisumDto {

    private List<LadeZaehlstelleVisumDto> zaehlstellen;

}
