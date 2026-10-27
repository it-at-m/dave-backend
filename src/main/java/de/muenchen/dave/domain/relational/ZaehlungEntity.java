package de.muenchen.dave.domain.relational;

import de.muenchen.dave.domain.BaseEntity;
import de.muenchen.dave.domain.enums.Fahrzeug;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "zaehlung")
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ZaehlungEntity extends BaseEntity {

    private LocalDate datum;
    private String jahr;
    private String monat;
    private String jahreszeit;
    private String zaehlart;
    private Double latitude;
    private Double longitude;

    @Column(name = "tages_typ")
    private String tagesTyp;

    @Column(name = "projekt_nummer")
    private String projektNummer;

    @Column(name = "projekt_name")
    private String projektName;

    private String kreuzungsname;
    private Boolean sonderzaehlung;
    private Boolean kreisverkehr;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<Fahrzeug> kategorien;

    private String status;

    @Column(name = "zaehlsituation")
    private String zaehlsituation;

    @Column(name = "zaehlsituation_erweitert")
    private String zaehlsituationErweitert;

    @Column(name = "zaehl_intervall")
    private int zaehlIntervall;

    private String wetter;

    @Column(name = "zaehldauer")
    private String zaehldauer;

    private String quelle;
    private String kommentar;

    @Column(name = "schul_zeiten")
    private String schulZeiten;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> suchwoerter;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> customSuchwoerter;

    @JdbcTypeCode(SqlTypes.JSON)
    private Object pkwEinheit;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> geographie;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<Object> knotenarme;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<Object> laengsverkehr;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<Object> querungsverkehr;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<Object> verkehrsbeziehungen;

    @Column(name = "unread_messages_mobilitaetsreferat")
    private Boolean unreadMessagesMobilitaetsreferat;

    @Column(name = "unread_messages_dienstleister")
    private Boolean unreadMessagesDienstleister;

    @Column(name = "dienstleisterkennung")
    private String dienstleisterkennung;

    @ManyToOne
    @JoinColumn(name = "zaehlstelle_id", nullable = false)
    private ZaehlstelleEntity zaehlstelle;
}
