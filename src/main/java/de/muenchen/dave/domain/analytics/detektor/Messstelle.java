package de.muenchen.dave.domain.analytics.detektor;

import de.muenchen.dave.domain.BaseEntity;
import de.muenchen.dave.domain.converter.StringListConverter;
import de.muenchen.dave.domain.enums.Fahrzeugklasse;
import de.muenchen.dave.domain.enums.MessstelleStatus;
import de.muenchen.dave.domain.enums.Verkehrsart;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

@Entity
// Definition of getter, setter, ...
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Document(indexName = "#{ 'messstelle' + @environment.getProperty('elasticsearch.index.suffix') }")
public class Messstelle extends BaseEntity {

    @Column(name = "mst_id")
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

    @Embedded
    @AttributeOverrides(
        {
                @AttributeOverride(name = "lat", column = @Column(name = "latitude")),
                @AttributeOverride(name = "lon", column = @Column(name = "longitude")),
        }
    )
    private GeoPoint punkt;

    /**
     * Steuert die Sichtbarkeit der Messstelle im Datenportal.
     */
    @Column(name = "sichtbar_datenportal")
    private Boolean sichtbarDatenportal;

    @Column(name = "geprueft")
    private Boolean geprueft = false;

    @Column(name = "kommentar")
    private String kommentar;

    @Column(name = "standort")
    private String standort;

    @Convert(converter = StringListConverter.class)
    @Column(name = "suchwoerter")
    private List<String> suchwoerter = new ArrayList<>();

    @Convert(converter = StringListConverter.class)
    @Column(name = "custom_suchwoerter")
    private List<String> customSuchwoerter = new ArrayList<>();

    @OneToMany(mappedBy = "messstelle", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Messquerschnitt> messquerschnitte = new ArrayList<>();

    @OneToMany(mappedBy = "messstelle", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Messfaehigkeit> messfaehigkeiten = new ArrayList<>();

    @Column(name = "lageplan_vorhanden")
    private Boolean lageplanVorhanden = false;
}
