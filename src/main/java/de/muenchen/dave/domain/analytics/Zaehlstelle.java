package de.muenchen.dave.domain.analytics;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.elasticsearch.annotations.GeoPointField;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

import de.muenchen.dave.domain.BaseEntity;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;
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
    String nummer;

    @Column(name = "stadtbezirk")
    String stadtbezirk;

    @Column(name = "stadtbezirk_nummer")
    Integer stadtbezirkNummer;

    @Column(name = "kommentar")
    String kommentar;

    @Column(name = "letzte_zaehlung_monat_nummer")
    Integer letzteZaehlungMonatNummer;

    @Column(name = "letzte_zaehlung_monat")
    String letzteZaehlungMonat;

    @Column(name = "letzte_zaehlung_jahr")
    Integer letzteZaehlungJahr;

    @Column(name = "grund_letzte_zaehlung")
    String grundLetzteZaehlung;

    /**
     * Steuert die Sichtbarkeit der Zählstelle im Datenportal.
     */
    @Column(name = "sichtbar_datenportal")
    Boolean sichtbarDatenportal;

    @Transient
    GeoPoint punkt;

    @Transient
    List<Zaehlung> zaehlungen = new ArrayList<>();
}
