/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.domain;

import de.muenchen.dave.domain.enums.FahrbewegungKreisverkehr;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

/**
 * Je nach Kreuzungstyp sind folgende Attribute gesetzt.
 * <p>
 * Kreuzung:
 * - {@link Fahrbeziehung#fahrbewegungKreisverkehr} ist immer "null".
 * Erstreckt sich ein Zeitintervall von einem Knotenarm zu allen anderen Knotenarmen:
 * - {@link Fahrbeziehung#von} mit Wert eingehenden Knotenarms gesetzt.
 * - {@link Fahrbeziehung#nach} ist "null".
 * Erstreckt sich ein Zeitintervall von allen anderen Knotenarmen zu einem Knotenarm:
 * - {@link Fahrbeziehung#von} ist "null"
 * - {@link Fahrbeziehung#nach} mit Wert eingehenden Knotenarms gesetzt..
 * Erstreckt sich ein Zeitintervall über alle Fahrbeziehungen:
 * - {@link Fahrbeziehung#von} ist "null".
 * - {@link Fahrbeziehung#nach} ist "null".
 * - {@link Fahrbeziehung#fahrbewegungKreisverkehr} ist "null".
 * <p>
 * Kreisverkehr:
 * - {@link Fahrbeziehung#von} ist immer gesetzt und repräsentiert den Knotenarm
 * - {@link Fahrbeziehung#nach} ist "null".
 * - {@link Fahrbeziehung#fahrbewegungKreisverkehr} ist mit Wert gesetzt.
 * Erstreckt sich ein Zeitintervall über alle Fahrbeziehungen:
 * - {@link Fahrbeziehung#von} ist "null".
 * - {@link Fahrbeziehung#nach} ist "null".
 * - {@link Fahrbeziehung#fahrbewegungKreisverkehr} ist "null".
 */
@Embeddable
@Data
public class Fahrbeziehung {

    @Column(name = "von")
    private Integer von;

    @Column(name = "nach")
    private Integer nach;

    @Column(name = "fahrbewegungkreisverkehr")
    @Enumerated(EnumType.STRING)
    private FahrbewegungKreisverkehr fahrbewegungKreisverkehr;

}
