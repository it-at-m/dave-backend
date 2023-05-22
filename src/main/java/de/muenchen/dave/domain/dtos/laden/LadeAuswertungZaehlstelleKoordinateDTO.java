package de.muenchen.dave.domain.dtos.laden;

import lombok.Data;

import java.time.LocalDate;

@Data
public class LadeAuswertungZaehlstelleKoordinateDTO {

    private String nummer;

    private LocalDate letzteZaehlung;

    private String kommentar;

    private Double lat;

    private Double lng;

}
