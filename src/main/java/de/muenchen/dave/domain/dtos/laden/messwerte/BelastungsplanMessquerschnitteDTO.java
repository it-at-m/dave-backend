/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.domain.dtos.laden.messwerte;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

@Data
public class BelastungsplanMessquerschnitteDTO implements Serializable {
    private List<LadeBelastungsplanMessquerschnittDataDTO> ladeBelastungsplanMessquerschnittDataDTOList;
    private String strassenname;
    private String mstId;
    private Integer stadtbezirkNummer;
    private Integer totalKfz;
    private Integer totalSv;
    private Integer totalGv;
    private Integer totalRad;
    private BigDecimal totalPercentSv;
    private BigDecimal totalPercentGv;
    private LocalTime startUhrzeitSpitzenstunde;
    private LocalTime endeUhrzeitSpitzenstunde;
}
