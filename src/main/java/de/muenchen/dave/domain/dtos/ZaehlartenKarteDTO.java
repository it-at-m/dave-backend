/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2021
 */
package de.muenchen.dave.domain.dtos;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Set;

@RequiredArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode(exclude = { "zaehlarten" })
public class ZaehlartenKarteDTO implements Serializable {

    private Double longitude;

    private Double latitude;

    private Set<String> zaehlarten;

}
