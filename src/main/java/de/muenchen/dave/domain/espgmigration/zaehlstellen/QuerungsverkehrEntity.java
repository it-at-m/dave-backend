package de.muenchen.dave.domain.espgmigration.zaehlstellen;

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
 * Entity-Klasse für Querungsverkehr (Migration von Elasticsearch zu PostgreSQL).
 */
@Entity
@Table(name = "querungsverkehr")
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class QuerungsverkehrEntity extends MappedBewegungsbeziehung {

    @Column(name = "knotenarm")
    private Integer knotenarm;

    @Column(name = "richtung")
    @Enumerated(EnumType.STRING)
    private Himmelsrichtung richtung;

}
