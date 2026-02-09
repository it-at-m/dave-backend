package de.muenchen.dave.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import de.muenchen.dave.domain.enums.TypeZeitintervall;
import de.muenchen.dave.util.dataimport.ZeitintervallSortingIndexUtil;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Getter
@Setter
@Builder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Table(
        indexes = {
                @Index(name = "index_zaehlung", columnList = "zaehlung_id"),
                @Index(name = "index_zeitintervall_bewegungsbeziehung_id", columnList = "bewegungsbeziehung_id"),
                @Index(name = "index_zeitintervall_combined_1", columnList = "zaehlung_id, type, verkehrsbeziehung_von, verkehrsbeziehung_nach"),
                @Index(name = "index_zeitintervall_combined_2", columnList = "zaehlung_id, startuhrzeit, endeuhrzeit, verkehrsbeziehung_von, type"),
                @Index(
                        name = "index_zeitintervall_combined_3",
                        columnList = "zaehlung_id, startuhrzeit, endeuhrzeit, verkehrsbeziehung_von, verkehrsbeziehung_nach, verkehrsbeziehung_fahrbewegungkreisverkehr, type"
                )
        }
)
public class Zeitintervall extends BaseEntity {

    @Column(name = "zaehlung_id", nullable = false)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID zaehlungId;

    @Column(name = "bewegungsbeziehung_id")
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID bewegungsbeziehungId;

    @Column(name = "startuhrzeit")
    @NotNull
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime startUhrzeit;

    @Column(name = "endeuhrzeit")
    @NotNull
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime endeUhrzeit;

    /**
     * Der Index ist erforderlich um bei der Datenextraktion die korrekte Reihenfolge zu erhalten. Der
     * Index wird vor der Persistierung mit der Klasse
     * {@link ZeitintervallSortingIndexUtil} ermittelt.
     */
    @Column(name = "sortingindex")
    private Integer sortingIndex;

    @Column(name = "pkw")
    private Integer pkw;

    @Column(name = "lkw")
    private Integer lkw;

    @Column(name = "lastzuege")
    private Integer lastzuege;

    @Column(name = "busse")
    private Integer busse;

    @Column(name = "kraftraeder")
    private Integer kraftraeder;

    @Column(name = "fahrradfahrer")
    private Integer fahrradfahrer;

    @Column(name = "fussgaenger")
    private Integer fussgaenger;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private TypeZeitintervall type;

    @Embedded
    @AttributeOverrides(
        {
                @AttributeOverride(name = "faktorKfz", column = @Column(name = "hochrechnung_faktorkfz")),
                @AttributeOverride(name = "faktorSv", column = @Column(name = "hochrechnung_faktorsv")),
                @AttributeOverride(name = "faktorGv", column = @Column(name = "hochrechnung_faktorgv")),
                @AttributeOverride(name = "hochrechnungKfz", column = @Column(name = "hochrechnung_hochrechnungkfz")),
                @AttributeOverride(name = "hochrechnungSv", column = @Column(name = "hochrechnung_hochrechnungsv")),
                @AttributeOverride(name = "hochrechnungGv", column = @Column(name = "hochrechnung_hochrechnunggv"))
        }
    )
    private Hochrechnung hochrechnung;

    @Embedded
    @AttributeOverrides(
        {
                @AttributeOverride(name = "knotenarm", column = @Column(name = "querungsverkehr_knotenarm")),
                @AttributeOverride(name = "richtung", column = @Column(name = "querungsverkehr_richtung"))
        }
    )
    private Querungsverkehr querungsverkehr;

    @Embedded
    @AttributeOverrides(
        {
                @AttributeOverride(name = "knotenarm", column = @Column(name = "laengsverkehr_knotenarm")),
                @AttributeOverride(name = "richtung", column = @Column(name = "laengsverkehr_richtung")),
                @AttributeOverride(name = "strassenseite", column = @Column(name = "laengsverkehr_strassenseite"))
        }
    )
    private Laengsverkehr laengsverkehr;

    @Embedded
    @AttributeOverrides(
        {
                @AttributeOverride(name = "von", column = @Column(name = "verkehrsbeziehung_von")),
                @AttributeOverride(name = "nach", column = @Column(name = "verkehrsbeziehung_nach")),
                @AttributeOverride(name = "fahrbewegungKreisverkehr", column = @Column(name = "verkehrsbeziehung_fahrbewegungkreisverkehr"))
        }
    )
    private Verkehrsbeziehung verkehrsbeziehung;

}
