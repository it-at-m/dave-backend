package de.muenchen.dave.domain.espgmigration.zaehlstellen;

import de.muenchen.dave.domain.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.locationtech.jts.geom.Point;

/**
 * Entity-Klasse für Zählstellen (Migration von Elasticsearch zu PostgreSQL).
 */
@Entity
@Table(name = "zaehlstelle")
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ZaehlstelleEntity extends BaseEntity {

    @Column(name = "nummer", nullable = false)
    private String nummer;

    @Column(name = "stadtbezirk")
    private String stadtbezirk;

    @Column(name = "stadtbezirk_nummer")
    private Integer stadtbezirkNummer;

    @Column(name = "kommentar")
    private String kommentar;

    /*
       @TODO : Offen ob ggf. einfach

         private Double point_latitude;
         private Double point_longitude;

        Fuer den Typ Point sind neue Dependencies in der pom.xml
    */
    @Column(name = "punkt", columnDefinition = "GEOGRAPHY(POINT, 4326)")
    private Point punkt;

    @Column(name = "letzte_zaehlung_monat_nummer")
    private Integer letzteZaehlungMonatNummer;

    @Column(name = "letzte_zaehlung_monat")
    private String letzteZaehlungMonat;

    @Column(name = "letzte_zaehlung_jahr")
    private Integer letzteZaehlungJahr;

    @Column(name = "grund_letzte_zaehlung")
    private String grundLetzteZaehlung;

    @Column(name = "sichtbar_datenportal")
    private Boolean sichtbarDatenportal = false;

    @Column(name = "suchwoerter")
    private String suchwoerter;

    @Column(name = "custom_suchwoerter")
    private String customSuchwoerter;

    @OneToMany(mappedBy = "zaehlstelle", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<ZaehlungEntity> zaehlungen = new ArrayList<>();

}
