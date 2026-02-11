package de.muenchen.dave.domain;

import de.muenchen.dave.domain.enums.FahrbewegungKreisverkehr;
import de.muenchen.dave.domain.enums.Himmelsrichtung;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Je nach Kreuzungstyp sind folgende Attribute gesetzt.
 * <p>
 * Kreuzung:
 * - {@link Verkehrsbeziehung#von} als Startknotenarm der Verkehrsbeziehung
 * - {@link Verkehrsbeziehung#nach} als Endknotenarm der Verkehrsbeziehung
 * - {@link Verkehrsbeziehung#fahrbewegungKreisverkehr} ist immer "null".
 * <p>
 * Kreisverkehr:
 * - {@link Verkehrsbeziehung#von} ist immer gesetzt und repräsentiert den Knotenarm
 * - {@link Verkehrsbeziehung#nach} ist "null".
 * - {@link Verkehrsbeziehung#fahrbewegungKreisverkehr} ist mit Wert gesetzt.
 */
@Embeddable
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Verkehrsbeziehung extends Bewegungsbeziehung {

    @Column(name = "von")
    private Integer von;

    @Column(name = "nach")
    private Integer nach;

    @Column(name = "fahrbewegungkreisverkehr")
    @Enumerated(EnumType.STRING)
    private FahrbewegungKreisverkehr fahrbewegungKreisverkehr;

    @Column(name = "strassenseite")
    @Enumerated(EnumType.STRING)
    private Himmelsrichtung strassenseite;

}
