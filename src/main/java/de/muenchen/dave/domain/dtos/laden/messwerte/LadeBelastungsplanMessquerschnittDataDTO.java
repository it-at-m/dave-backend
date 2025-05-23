/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.domain.dtos.laden.messwerte;

import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class LadeBelastungsplanMessquerschnittDataDTO implements Serializable {

    private String mqId;
    private Integer sumKfz;
    private Integer sumSv;
    private Integer sumGv;
    private Integer sumRad;
    private String direction;

    private BigDecimal percentSv;
    private BigDecimal percentGV;
}
