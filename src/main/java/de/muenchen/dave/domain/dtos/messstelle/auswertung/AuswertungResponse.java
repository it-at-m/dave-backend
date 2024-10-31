/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2023
 */
package de.muenchen.dave.domain.dtos.messstelle.auswertung;

import de.muenchen.dave.geodateneai.gen.model.TagesaggregatResponseDto;
import de.muenchen.dave.services.messstelle.Zeitraum;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AuswertungResponse extends TagesaggregatResponseDto {

    private Zeitraum zeitraum;
}
