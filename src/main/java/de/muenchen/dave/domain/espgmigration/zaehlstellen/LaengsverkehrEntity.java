package de.muenchen.dave.domain.espgmigration.zaehlstellen;

import de.muenchen.dave.domain.enums.Bewegungsrichtung;
import de.muenchen.dave.domain.enums.Himmelsrichtung;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Entity-Klasse für Längsverkehr (Migration von Elasticsearch zu PostgreSQL).
 */
@Entity
@Table(name = "laengsverkehr")
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class LaengsverkehrEntity extends MappedBewegungsbeziehung {

    @Column(name = "knotenarm")
    private Integer knotenarm;

    @Column(name = "richtung")
    @Enumerated(EnumType.STRING)
    private Bewegungsrichtung richtung;

    @Column(name = "strassenseite")
    @Enumerated(EnumType.STRING)
    private Himmelsrichtung strassenseite;

}
