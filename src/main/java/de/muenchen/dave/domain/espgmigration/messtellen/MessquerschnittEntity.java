package de.muenchen.dave.domain.espgmigration.messtellen;

import de.muenchen.dave.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.locationtech.jts.geom.Point;

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

    @ManyToOne
    @JoinColumn(name = "messstelle_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private MessstelleEntity messstelle;

    @Column(name = "mq_id", unique = true)
    private String mqId;

   /*
       @TODO : Offen ob ggf. einfach

         private Double point_latitude;
         private Double point_longitude;

        Fuer den Typ Point sind neue Dependencies in der pom.xml
    */
    @Column(name = "punkt", columnDefinition = "GEOGRAPHY(POINT, 4326)")
    private Point punkt;

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
