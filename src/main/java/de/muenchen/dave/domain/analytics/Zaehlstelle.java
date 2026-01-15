package de.muenchen.dave.domain.analytics;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.elasticsearch.core.geo.GeoPoint;

import de.muenchen.dave.domain.BaseEntity;
import de.muenchen.dave.domain.converter.StringListConverter;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
// Definition of getter, setter, ...
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Zaehlstelle extends BaseEntity {

    @Column(name = "nummer")
    private String nummer;

    @Column(name = "stadtbezirk")
    private String stadtbezirk;

    @Column(name = "stadtbezirk_nummer")
    private Integer stadtbezirkNummer;

    @Column(name = "kommentar")
    private String kommentar;

    @Embedded
    @AttributeOverrides(
        {
                @AttributeOverride(name = "lat", column = @Column(name = "latitude")),
                @AttributeOverride(name = "lon", column = @Column(name = "longitude")),
        }
    )
    private GeoPoint punkt;

    @Column(name = "letzte_zaehlung_monat_nummer")
    private Integer letzteZaehlungMonatNummer;

    @Column(name = "letzte_zaehlung_monat")
    private String letzteZaehlungMonat;

    @Column(name = "letzte_zaehlung_jahr")
    private Integer letzteZaehlungJahr;

    @Column(name = "grund_letzte_zaehlung")
    private String grundLetzteZaehlung;

    @Convert(converter = StringListConverter.class)
    @Column(name = "suchwoerter")
    private List<String> suchwoerter = new ArrayList<>();

    @Convert(converter = StringListConverter.class)
    @Column(name = "custom_suchwoerter")
    private List<String> customSuchwoerter = new ArrayList<>();

    @OneToMany(mappedBy = "zaehlstelle", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Zaehlung> zaehlungen = new ArrayList<>();

    /**
     * Steuert die Sichtbarkeit der Zählstelle im Datenportal.
     */
    @Column(name = "sichtbar_datenportal")
    private Boolean sichtbarDatenportal;
}
