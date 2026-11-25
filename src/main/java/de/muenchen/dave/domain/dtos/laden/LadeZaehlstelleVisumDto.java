package de.muenchen.dave.domain.dtos.laden;

import de.muenchen.dave.util.geo.CoordinateUtil;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class LadeZaehlstelleVisumDto {

    String id;

    String nummer;

    Integer stadtbezirkNummer;

    String kommentar;

    CoordinateUtil.PositionUTM punktUtm;

    List<LadeZaehlungVisumDto> zaehlungen = new ArrayList<>();

}
