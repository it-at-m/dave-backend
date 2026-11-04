package de.muenchen.dave.domain.relational.zaehlstellen;

import de.muenchen.dave.domain.BaseEntity;
import de.muenchen.dave.domain.Hochrechnungsfaktor;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Gemeinsame Basis Klasse fuer Leangsverkehr, Querverkehr und Verkehrbeziehungen.
 * Kein Mapping als Entity mit eigener Tabelle.
 * Alle geerbten Spalten landen physisch in den Tabellen der konkreten Subklassen.
 * Keine polymorphen Abfragen: SELECT b FROM BewegungsbeziehungEntity b möglich.
 * Jeder abgeleitete Typ ist mit @MappedSuperclass join freie eigene Liste in ZaehlungEntity.
 */
@MappedSuperclass
@Getter
@Setter
@ToString
@EqualsAndHashCode
public abstract class MappedBewegungsbeziehung extends BaseEntity {

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "hochrechnungsfaktor_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Hochrechnungsfaktor hochrechnungsfaktor;

}
