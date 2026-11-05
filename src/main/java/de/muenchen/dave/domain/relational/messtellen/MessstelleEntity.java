package de.muenchen.dave.domain.relational.messtellen;

import de.muenchen.dave.domain.BaseEntity;
import de.muenchen.dave.domain.enums.Fahrzeugklasse;
import de.muenchen.dave.domain.enums.MessstelleStatus;
import de.muenchen.dave.domain.enums.Verkehrsart;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
    @JdbcTypeCode(SqlTypes.DATE)
    private LocalDate realisierungsdatum;

    @Column(name = "abbaudatum")
    @JdbcTypeCode(SqlTypes.DATE)
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
    @JdbcTypeCode(SqlTypes.DATE)
    private LocalDate datumLetztePlausibleMessung;

    @Column
    private Double latitude;

    @Column
    private Double longitude;

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

    @Column(name = "suchwoerter", columnDefinition = "text[]")
    private List<String> suchwoerter;

    @Column(name = "custom_suchwoerter", columnDefinition = "text[]")
    private List<String> customSuchwoerter;

    @OneToMany(cascade = { CascadeType.ALL }, orphanRemoval = true)
    @JoinColumn(name = "messstelle_id", referencedColumnName = "id")
    private List<MessquerschnittEntity> messquerschnitte = new ArrayList<>();

    @OneToMany(cascade = { CascadeType.ALL }, orphanRemoval = true)
    @JoinColumn(name = "messstelle_id", referencedColumnName = "id")
    private List<MessfaehigkeitEntity> messfaehigkeiten = new ArrayList<>();

}
