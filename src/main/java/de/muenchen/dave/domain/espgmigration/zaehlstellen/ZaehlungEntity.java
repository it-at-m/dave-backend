package de.muenchen.dave.domain.espgmigration.zaehlstellen;

import de.muenchen.dave.domain.BaseEntity;

import de.muenchen.dave.domain.enums.Quelle;
import de.muenchen.dave.domain.enums.Status;
import de.muenchen.dave.domain.enums.TagesTyp;
import de.muenchen.dave.domain.enums.Wetter;
import de.muenchen.dave.domain.enums.Zaehlart;
import de.muenchen.dave.domain.enums.Zaehldauer;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
 * Entity-Klasse für Zählungen (Migration von Elasticsearch zu PostgreSQL).
 */
@Entity
@Table(name = "zaehlung")
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ZaehlungEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zaehlstelle_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ZaehlstelleEntity zaehlstelleEntity;

    @Column(name = "datum")
    private LocalDate datum;

    @Column(name = "jahr", length = 4)
    private String jahr;

    @Column(name = "monat", length = 2)
    private String monat;

    @Column(name = "jahreszeit")
    private String jahreszeit;

    @Column(name = "zaehlart")
    @Enumerated(EnumType.STRING)
    private Zaehlart zaehlart;

    /*
          @TODO : Offen ob ggf. einfach

            private Double point_latitude;
            private Double point_longitude;

           Fuer den Typ Point sind neue Dependencies in der pom.xml
    */
    @Column(name = "punkt", columnDefinition = "GEOGRAPHY(POINT, 4326)")
    private Point punkt;

    @Column(name = "tages_typ")
    @Enumerated(EnumType.STRING)
    private TagesTyp tagesTyp;

    @Column(name = "projekt_nummer")
    private String projektNummer;

    @Column(name = "projekt_name")
    private String projektName;

    @Column(name = "kreuzungsname")
    private String kreuzungsname;

    @Column(name = "sonderzaehlung")
    private Boolean sonderzaehlung = false;

    @Column(name = "kreisverkehr")
    private Boolean kreisverkehr = false;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "zaehlsituation")
    private String zaehlsituation;

    @Column(name = "zaehlsituation_erweitert")
    private String zaehlsituationErweitert;

    @Column(name = "zaehl_intervall")
    private Integer zaehlIntervall;

    @Column(name = "wetter")
    @Enumerated(EnumType.STRING)
    private Wetter wetter;

    @Column(name = "zaehldauer")
    @Enumerated(EnumType.STRING)
    private Zaehldauer zaehldauer;

    @Column(name = "quelle")
    @Enumerated(EnumType.STRING)
    private Quelle quelle;

    @Column(name = "kommentar")
    private String kommentar;

    @Column(name = "schul_zeiten")
    private String schulZeiten;

    @Column(name = "sichtbar_datenportal")
    private Boolean sichtbarDatenportal = false;

    @Column(name = "unread_messages_mobilitaetsreferat")
    private Boolean unreadMessagesMobilitaetsreferat = false;

    @Column(name = "unread_messages_dienstleister")
    private Boolean unreadMessagesDienstleister = false;

    @Column(name = "dienstleisterkennung")
    private String dienstleisterkennung;

    @Column(name = "suchwoerter")
    private String suchwoerter;

    @OneToMany(mappedBy = "zaehlung", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<KnotenarmEntity> knotenarme = new ArrayList<>();

    @OneToMany(mappedBy = "zaehlung", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<LaengsverkehrEntity> laengsverkehre = new ArrayList<>();

    @OneToMany(mappedBy = "zaehlung", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<QuerungsverkehrEntity> querungsverkehre = new ArrayList<>();

    @OneToMany(mappedBy = "zaehlung", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<VerkehrsbeziehungEntity> verkehrsbeziehungen = new ArrayList<>();

/*
    Hinweis : Es gibt nur eine Entity in der Tabelle pkweinheit.
 */
//    @OneToMany(mappedBy = "zaehlung", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
//    @ToString.Exclude
//    @EqualsAndHashCode.Exclude
//    private List<PkwEinheitEntity> pkwEinheiten = new ArrayList<>();


}
