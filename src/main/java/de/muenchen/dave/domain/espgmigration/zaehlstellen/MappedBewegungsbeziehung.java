package de.muenchen.dave.domain.espgmigration.zaehlstellen;

import de.muenchen.dave.domain.BaseEntity;
import de.muenchen.dave.domain.Hochrechnungsfaktor;
import jakarta.persistence.*;
import lombok.*;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zaehlung_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ZaehlungEntity zaehlung;

    /*
     @TODO : Kompatibilität des ES mit dem PG Hochrechnungfaktor abgleichen
    */
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "hochrechnungsfaktor_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Hochrechnungsfaktor hochrechnungsfaktor;


}