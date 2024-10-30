/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2023
 */
package de.muenchen.dave.domain.dtos.messstelle.auswertung;

import de.muenchen.dave.services.messstelle.Zeitraum;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AuswertungResponse {

    // Stammdaten

    private Integer mqId;

    // Anzahl

    private BigDecimal anzahlPkw;

    private BigDecimal anzahlPkwA;

    private BigDecimal anzahlLfw;

    private BigDecimal anzahlKrad;

    private BigDecimal anzahlLkw;

    private BigDecimal anzahlLkwA;

    private BigDecimal anzahlSattelKfz;

    private BigDecimal anzahlBus;

    private BigDecimal anzahlNkKfz;

    private BigDecimal anzahlRad;

    // Summen

    private BigDecimal summeAllePkw;

    private BigDecimal summeLastzug;

    private BigDecimal summeGueterverkehr;

    private BigDecimal summeSchwerverkehr;

    private BigDecimal summeKraftfahrzeugverkehr;

    private BigDecimal prozentSchwerverkehr;

    private BigDecimal prozentGueterverkehr;

    // Messtage

    private Long includedMeasuringDays;
    private Zeitraum zeitraum;
}
