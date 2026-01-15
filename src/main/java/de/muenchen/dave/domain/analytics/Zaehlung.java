package de.muenchen.dave.domain.analytics;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

import de.muenchen.dave.domain.BaseEntity;
import de.muenchen.dave.domain.PkwEinheit;
import de.muenchen.dave.domain.converter.FahrzeugListConverter;
import de.muenchen.dave.domain.converter.StringListConverter;
import de.muenchen.dave.domain.enums.Fahrzeug;
import de.muenchen.dave.domain.enums.Status;
import de.muenchen.dave.domain.enums.Zaehlart;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
public class Zaehlung extends BaseEntity {

    @Column(nullable = false, name = "datum")
    LocalDate datum;

    @Column(name = "jahr")
    Integer jahr;

    @Column(name = "monat")
    String monat;

    @Column(name = "jahreszeit")
    String jahreszeit;

    @Enumerated(EnumType.STRING)
    @Column(name = "zaehlart")
    private Zaehlart zaehlart;

    @Embedded
    @AttributeOverrides(
        {
                @AttributeOverride(name = "lat", column = @Column(name = "latitude")),
                @AttributeOverride(name = "lon", column = @Column(name = "longitude")),
        }
    )
    GeoPoint punkt;

    /**
     * Wochenende, Wochentag, Feiertag
     */
    @Column(name = "tages_typ")
    private String tagesTyp;

    @Column(name = "projekt_nummer")
    private String projektNummer;

    @Column(name = "projekt_name")
    private String projektName;

    @Column(name = "kreuzungsname")
    private String kreuzungsname;

    @Column(name = "sonderzaehlung")
    private Boolean sonderzaehlung;

    @Column(name = "kreisverkehr")
    private Boolean kreisverkehr;

    @Convert(converter = FahrzeugListConverter.class)
    @Column(name = "kategorien")
    private List<Fahrzeug> kategorien;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "zaehlsituation")
    private String zaehlsituation;

    @Column(name = "zaehlsituation_erweitert")
    private String zaehlsituationErweitert;

    @Column(name = "zaehlintervall")
    private int zaehlIntervall;

    @Column(name = "wetter")
    private String wetter;

    /**
     * 2x4h, 16h, 24h
     */
    @Column(name = "zaehldauer")
    private String zaehldauer;

    @Column(name = "quelle")
    private String quelle;

    @Column(name = "kommentar")
    private String kommentar;

    /**
     * Ferien, Schule
     */
    @Column(name = "schul_zeiten")
    private String schulZeiten;

    @Convert(converter = StringListConverter.class)
    @Column(name = "suchwoerter")
    private List<String> suchwoerter = new ArrayList<>();

    @Convert(converter = StringListConverter.class)
    @Column(name = "custom_suchwoerter")
    private List<String> customSuchwoerter = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "zaehlstelle", referencedColumnName = "id")
    private Zaehlstelle zaehlstelle;

    @ManyToOne(cascade = CascadeType.REFRESH, optional = true)
    @JoinColumn(name = "pkw_einheit")
    private PkwEinheit pkwEinheit;

    @Convert(converter = StringListConverter.class)
    @Column(name = "geographie")
    private List<String> geographie = new ArrayList<>();

    @OneToMany(mappedBy = "zaehlung", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Knotenarm> knotenarme;

    @OneToMany(mappedBy = "zaehlung", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Fahrbeziehung> fahrbeziehungen;

    @Column(name = "unread_messages_mobilitaetsreferat")
    private Boolean unreadMessagesMobilitaetsreferat;

    @Column(name = "unread_messages_dienstleister")
    private Boolean unreadMessagesDienstleister;

    @Column(name = "dienstleisterkennung")
    private String dienstleisterkennung;

}
