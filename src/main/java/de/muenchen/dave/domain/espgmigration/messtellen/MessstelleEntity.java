package de.muenchen.dave.domain.espgmigration.messtellen;

import de.muenchen.dave.domain.BaseEntity;
import de.muenchen.dave.domain.enums.Fahrzeugklasse;
import de.muenchen.dave.domain.enums.MessstelleStatus;
import de.muenchen.dave.domain.enums.Verkehrsart;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.locationtech.jts.geom.Point;

/**
 * Entity-Klasse für Messstellen (Detektor) (Migration von Elasticsearch zu PostgreSQL).
  */
@Entity
@Table(name = "messstelle")
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class MessstelleEntity extends BaseEntity {

    @Column(name = "mst_id", unique = true, nullable = false)
    private String mstId;

    @Column(name = "name")
    private String name;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private MessstelleStatus status;

    @Column(name = "realisierungsdatum")
    private LocalDate realisierungsdatum;

    @Column(name = "abbaudatum")
    private LocalDate abbaudatum;

    @Column(name = "stadtbezirk_nummer")
    private Integer stadtbezirkNummer;

    @Column(name = "bemerkung")
    private String bemerkung;

    @Column(name = "fahrzeugklasse")
    @Enumerated(EnumType.STRING)
    private Fahrzeugklasse fahrzeugklasse;

    @Column(name = "detektierte_verkehrsart")
    @Enumerated(EnumType.STRING)
    private Verkehrsart detektierteVerkehrsart;

    @Column(name = "hersteller")
    private String hersteller;

    @Column(name = "datum_letzte_plausible_messung")
    private LocalDate datumLetztePlausibleMessung;

    /*
       @TODO : Offen ob ggf. einfach

         private Double point_latitude;
         private Double point_longitude;

        Fuer den Typ Point sind neue Dependencies in der pom.xml
    */
    @Column(name = "punkt", columnDefinition = "GEOGRAPHY(POINT, 4326)")
    private Point punkt;

    @Column(name = "sichtbar_datenportal")
    private Boolean sichtbarDatenportal = false;

    @Column(name = "geprueft")
    private Boolean geprueft = false;

    @Column(name = "kommentar")
    private String kommentar;

    @Column(name = "standort")
    private String standort;

    @Column(name = "lageplan_vorhanden")
    private Boolean lageplanVorhanden = false;

    @OneToMany(mappedBy = "messstelle", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<MessquerschnittEntity> messquerschnitte = new ArrayList<>();

    @OneToMany(mappedBy = "messstelle", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<MessfaehigkeitEntity> messfaehigkeiten = new ArrayList<>();

}
