/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.domain.dtos.messstelle;

import de.muenchen.dave.domain.enums.TagesTyp;
import de.muenchen.dave.domain.enums.ZaehldatenIntervall;
import de.muenchen.dave.domain.enums.Zeitblock;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Data
public class MessstelleOptionsDTO implements Serializable {

    @NotNull
    private List<LocalDate> zeitraum;

    @NotNull
    private FahrzeugOptionsDTO fahrzeuge;

    @NotNull
    private String zeitauswahl;

    @NotNull
    private Zeitblock zeitblock;

    private TagesTyp tagesTyp;

    @NotNull
    private ZaehldatenIntervall intervall;

    @NotNull
    private Set<String> messquerschnittIds;

    // Belastungsplan
    @NotNull
    private Boolean werteHundertRunden;
    // Listenausgabe
    @NotNull
    private Boolean stundensumme;
    @NotNull
    private Boolean blocksumme;
    @NotNull
    private Boolean tagessumme;
    @NotNull
    private Boolean spitzenstunde;
}
