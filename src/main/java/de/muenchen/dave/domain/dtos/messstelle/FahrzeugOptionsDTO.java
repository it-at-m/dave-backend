/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.domain.dtos.messstelle;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
public class FahrzeugOptionsDTO implements Serializable {

    @NotNull
    private boolean kraftfahrzeugverkehr;
    @NotNull
    private boolean schwerverkehr;
    @NotNull
    private boolean gueterverkehr;
    @NotNull
    private boolean schwerverkehrsanteilProzent;
    @NotNull
    private boolean gueterverkehrsanteilProzent;
    @NotNull
    private boolean radverkehr;
    @NotNull
    private boolean fussverkehr;
    @NotNull
    private boolean lastkraftwagen;
    @NotNull
    private boolean lastzuege;
    @NotNull
    private boolean busse;
    @NotNull
    private boolean kraftraeder;
    @NotNull
    private boolean personenkraftwagen;
    @NotNull
    private boolean lieferwagen;
}
