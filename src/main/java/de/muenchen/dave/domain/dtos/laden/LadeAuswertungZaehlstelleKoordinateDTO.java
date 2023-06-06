package de.muenchen.dave.domain.dtos.laden;

import java.time.LocalDate;
import lombok.Data;

@Data
public class LadeAuswertungZaehlstelleKoordinateDTO {

    private String nummer;

    private LocalDate letzteZaehlung;

    private String kommentar;

    private Double lat;

    private Double lng;

}
