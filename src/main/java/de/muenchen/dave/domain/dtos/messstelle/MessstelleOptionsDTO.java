/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.domain.dtos.messstelle;

import de.muenchen.dave.domain.enums.TagesTyp;
import de.muenchen.dave.domain.enums.ZaehldatenIntervall;
import de.muenchen.dave.domain.enums.Zeitblock;
import de.muenchen.dave.domain.validation.TagestypValid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import lombok.Data;

@Data
@TagestypValid
public class MessstelleOptionsDTO implements Serializable {

    @NotEmpty
    @Size(min = 2, max = 2)
    private List<@NotNull LocalDate> zeitraum;

    @NotNull
    private FahrzeugOptionsDTO fahrzeuge;

    @NotEmpty
    private String zeitauswahl;

    @NotNull
    private Zeitblock zeitblock;

    private TagesTyp tagesTyp;

    @NotNull
    private ZaehldatenIntervall intervall;

    @NotEmpty
    private Set<@NotEmpty String> messquerschnittIds;

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
