package de.muenchen.dave.domain.dtos.laden;

import de.muenchen.dave.util.geo.CoordinateUtil;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class LadeZaehlstelleVisumDTO {

    String id;

    String nummer;

    Integer stadtbezirkNummer;

    String kommentar;

    CoordinateUtil.PositionUTM punktUtm;

    List<LadeZaehlungVisumDTO> zaehlungen = new ArrayList<>();

}
