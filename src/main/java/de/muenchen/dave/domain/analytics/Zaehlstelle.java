package de.muenchen.dave.domain.analytics;

import de.muenchen.dave.domain.BaseEntity;
import de.muenchen.dave.domain.converter.StringListConverter;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;
import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

@Entity
// Definition of getter, setter, ...
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Zaehlstelle extends BaseEntity {

    @Column(name = "nummer")
    String nummer;

    @Column(name = "stadtbezirk")
    String stadtbezirk;

    @Column(name = "stadtbezirk_nummer")
    Integer stadtbezirkNummer;

    @Column(name = "kommentar")
    String kommentar;

    @Embedded
    @AttributeOverrides(
        {
                @AttributeOverride(name = "lat", column = @Column(name = "latitude")),
                @AttributeOverride(name = "lon", column = @Column(name = "longitude")),
        }
    )
    GeoPoint punkt;

    @Column(name = "letzte_zaehlung_monat_nummer")
    Integer letzteZaehlungMonatNummer;

    @Column(name = "letzte_zaehlung_monat")
    String letzteZaehlungMonat;

    @Column(name = "letzte_zaehlung_jahr")
    Integer letzteZaehlungJahr;

    @Column(name = "grund_letzte_zaehlung")
    String grundLetzteZaehlung;

    @Convert(converter = StringListConverter.class)
    @Column(name = "suchwoerter")
    List<String> suchwoerter;

    @Convert(converter = StringListConverter.class)
    @Column(name = "custom_suchwoerter")
    List<String> customSuchwoerter;

    @Transient
    List<Zaehlung> zaehlungen = new ArrayList<>();

    /**
     * Steuert die Sichtbarkeit der Zählstelle im Datenportal.
     */
    @Column(name = "sichtbar_datenportal")
    Boolean sichtbarDatenportal;
}
