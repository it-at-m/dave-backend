/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.domain.dtos.messstelle;

import de.muenchen.dave.domain.enums.ZaehldatenIntervall;
import de.muenchen.dave.domain.enums.Zeitblock;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import javax.validation.constraints.NotNull;
import lombok.Data;

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

    private String tagesTyp;

    @NotNull
    private ZaehldatenIntervall intervall;

    @NotNull
    private Set<String> messquerschnitte;
}
