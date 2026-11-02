package de.muenchen.dave.domain.relational.messtellen;

import de.muenchen.dave.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Entity-Klasse für Messquerschnitt (Migration von Elasticsearch zu PostgreSQL).
 */
@Entity
@Table(name = "messquerschnitt")
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class MessquerschnittEntity extends BaseEntity {

    @Column(name = "mq_id", unique = true)
    private String mqId;

    @Column
    private Double latitude;

    @Column
    private Double longitude;

    @Column(name = "strassenname")
    private String strassenname;

    @Column(name = "lage_messquerschnitt")
    private String lageMessquerschnitt;

    @Column(name = "fahrtrichtung")
    private String fahrtrichtung;

    @Column(name = "anzahl_fahrspuren")
    private Integer anzahlFahrspuren;

    @Column(name = "anzahl_detektoren")
    private Integer anzahlDetektoren;

    @Column(name = "standort")
    private String standort;

}
