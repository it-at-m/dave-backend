package de.muenchen.dave.domain.relational.zaehlstellen;

import de.muenchen.dave.domain.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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

    @Column
    private Double latitude;
    @Column
    private Double longitude;

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

    @Column(name = "suchwoerter", columnDefinition = "text[]")
    private List<String> suchwoerter;

    @Column(name = "custom_suchwoerter", columnDefinition = "text[]")
    private List<String> customSuchwoerter;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "suchwoerter_neu", columnDefinition = "tsvector")
    @ColumnTransformer(write = "to_tsvector('simple'::regconfig, ?)")
    private String suchwoerterNeu;

    @OneToMany(cascade = { CascadeType.ALL }, orphanRemoval = true)
    @JoinColumn(name = "zaehlstelle_id", referencedColumnName = "id")
    private List<ZaehlungEntity> zaehlungen = new ArrayList<>();

}
