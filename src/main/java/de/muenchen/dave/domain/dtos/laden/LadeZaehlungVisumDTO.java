package de.muenchen.dave.domain.dtos.laden;

import de.muenchen.dave.util.geo.CoordinateUtil;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;


@Data
public class LadeZaehlungVisumDTO {

    String id;

    String zaehlart;

    LocalDate datum;

    String zaehldauer;

    Boolean sonderzaehlung;

    Boolean kreisverkehr;

    String kreuzungsname;

    CoordinateUtil.PositionUTM punktUtm;

    List<FahrbeziehungVisumDTO> fahrbeziehungen;

}
