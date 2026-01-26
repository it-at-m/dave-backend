package de.muenchen.dave.domain;

import de.muenchen.dave.domain.enums.FahrbewegungKreisverkehr;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

/**
 * Je nach Kreuzungstyp sind folgende Attribute gesetzt.
 * <p>
 * Kreuzung:
 * - {@link Verkehrsbeziehung#fahrbewegungKreisverkehr} ist immer "null". Erstreckt sich ein
 * Zeitintervall von einem Knotenarm zu allen anderen Knotenarmen:
 * - {@link Verkehrsbeziehung#von} mit Wert eingehenden Knotenarms gesetzt.
 * - {@link Verkehrsbeziehung#nach} ist "null". Erstreckt sich ein Zeitintervall von allen anderen
 * Knotenarmen zu einem Knotenarm:
 * - {@link Verkehrsbeziehung#von} ist "null"
 * - {@link Verkehrsbeziehung#nach} mit Wert eingehenden Knotenarms gesetzt.. Erstreckt sich ein
 * Zeitintervall über alle Fahrbeziehungen:
 * - {@link Verkehrsbeziehung#von} ist "null".
 * - {@link Verkehrsbeziehung#nach} ist "null".
 * - {@link Verkehrsbeziehung#fahrbewegungKreisverkehr} ist "null".
 * <p>
 * Kreisverkehr:
 * - {@link Verkehrsbeziehung#von} ist immer gesetzt und repräsentiert den Knotenarm
 * - {@link Verkehrsbeziehung#nach} ist "null".
 * - {@link Verkehrsbeziehung#fahrbewegungKreisverkehr} ist mit Wert gesetzt. Erstreckt sich ein
 * Zeitintervall über alle Fahrbeziehungen:
 * - {@link Verkehrsbeziehung#von} ist "null".
 * - {@link Verkehrsbeziehung#nach} ist "null".
 * - {@link Verkehrsbeziehung#fahrbewegungKreisverkehr} ist "null".
 */
@Embeddable
@Data
public class Verkehrsbeziehung {

    @Column(name = "von")
    private Integer von;

    @Column(name = "nach")
    private Integer nach;

    @Column(name = "fahrbewegungkreisverkehr")
    @Enumerated(EnumType.STRING)
    private FahrbewegungKreisverkehr fahrbewegungKreisverkehr;

}
