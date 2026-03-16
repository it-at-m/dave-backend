package de.muenchen.dave.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import de.muenchen.dave.domain.dtos.laden.LadeZaehldatumDTO;
import de.muenchen.dave.domain.enums.TypeZeitintervall;
import de.muenchen.dave.util.dataimport.ZeitintervallSortingIndexUtil;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.ColumnResult;
import jakarta.persistence.ConstructorResult;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.NamedNativeQuery;
import jakarta.persistence.SqlResultSetMapping;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

@NamedNativeQuery(name = "Zeitintervall.findWeekdayAverageByZaehlungIdZIAsc",
                  query = "select \n" + //
                        "\tround(sum(pkw)/count(startuhrzeit::time)) as pkw, \n" + //
                        "\tround(sum(lkw)/count(startuhrzeit::time)) as lkw,\n" + //
                        "\tround(sum (lastzuege)/count(startuhrzeit::time)) as lastzuege,\n" + //
                        "\tround(sum(busse)/count(startuhrzeit::time)) as busse,\n" + //
                        "\tround(sum(kraftraeder)/count(startuhrzeit::time)) as kraftraeder,\n" + //
                        "\tround(sum(fahrradfahrer)/count(startuhrzeit::time)) as fahrradfahrer,\n" + //
                        "\tround(sum(fussgaenger)/count(startuhrzeit::time)) as fussgaenger, \n" + //
                        "\tCURRENT_DATE + startuhrzeit::time as startUhrzeit, \n" + //
                        "\tCURRENT_DATE + endeuhrzeit::time as endeUhrzeit \n" + //
                        "from (\n" + //
                        "select \n" + //
                        "\tsum(pkw) as pkw, \n" + //
                        "\tsum(lkw) as lkw,\n" + //
                        "\tsum (lastzuege) as lastzuege,\n" + //
                        "\tsum(busse) as busse,\n" + //
                        "\tsum(kraftraeder) as kraftraeder,\n" + //
                        "\tsum(fahrradfahrer) as fahrradfahrer,\n" + //
                        "\tsum(fussgaenger) as fussgaenger, \n" + //
                        "\tstartuhrzeit, \n" + //
                        "\tendeuhrzeit\n" + //
                        "FROM public.zeitintervall \n" + //
                        "where startuhrzeit between :start and :ende and EXTRACT(DOW FROM startuhrzeit) IN (1, 2, 3, 4, 5) \n" + //
                        "and zaehlung_id = :zaehlungId group by startuhrzeit, endeuhrzeit)\n" + //
                        "group by startuhrzeit::time, endeuhrzeit::time order by startuhrzeit::time ASC",
                  resultSetMapping = "Mapping.Zeitintervall")
@SqlResultSetMapping(name = "Mapping.Zeitintervall",
                     classes = @ConstructorResult(targetClass = Zeitintervall.class,
                                                  columns = {
                                                        @ColumnResult(name = "pkw", type = Integer.class),
                                                        @ColumnResult(name = "lkw", type = Integer.class),
                                                        @ColumnResult(name = "lastzuege", type = Integer.class),
                                                        @ColumnResult(name = "busse", type = Integer.class),
                                                        @ColumnResult(name = "kraftraeder", type = Integer.class),
                                                        @ColumnResult(name = "fahrradfahrer", type = Integer.class),
                                                        @ColumnResult(name = "fussgaenger", type = Integer.class),
                                                        @ColumnResult(name = "startUhrzeit", type = LocalDateTime.class),
                                                        @ColumnResult(name = "endeUhrzeit", type = LocalDateTime.class)
                                                }))
@NamedNativeQuery(name = "Zeitintervall.findWeekdayAverageByZaehlungIdOrderBySortingIndexAsc",
                  query = "select \n" + //
                        "\tround(sum(pkw)/count(startuhrzeit::time)) as pkw, \n" + //
                        "\tround(sum(lkw)/count(startuhrzeit::time)) as lkw,\n" + //
                        "\tround(sum (lastzuege)/count(startuhrzeit::time)) as lastzuege,\n" + //
                        "\tround(sum(busse)/count(startuhrzeit::time)) as busse,\n" + //
                        "\tround(sum(kraftraeder)/count(startuhrzeit::time)) as kraftraeder,\n" + //
                        "\tround(sum(fahrradfahrer)/count(startuhrzeit::time)) as fahrradfahrer,\n" + //
                        "\tround(sum(fussgaenger)/count(startuhrzeit::time)) as fussgaenger, \n" + //
                        "\tstartuhrzeit::time, \n" + //
                        "\tendeuhrzeit::time \n" + //
                        "from (\n" + //
                        "select \n" + //
                        "\tsum(pkw) as pkw, \n" + //
                        "\tsum(lkw) as lkw,\n" + //
                        "\tsum (lastzuege) as lastzuege,\n" + //
                        "\tsum(busse) as busse,\n" + //
                        "\tsum(kraftraeder) as kraftraeder,\n" + //
                        "\tsum(fahrradfahrer) as fahrradfahrer,\n" + //
                        "\tsum(fussgaenger) as fussgaenger, \n" + //
                        "\tstartuhrzeit, \n" + //
                        "\tendeuhrzeit\n" + //
                        "FROM public.zeitintervall \n" + //
                        "where startuhrzeit between :start and :ende and EXTRACT(DOW FROM startuhrzeit) IN (1, 2, 3, 4, 5) \n" + //
                        "and zaehlung_id = :zaehlungId group by startuhrzeit, endeuhrzeit)\n" + //
                        "group by startuhrzeit::time, endeuhrzeit::time order by startuhrzeit::time ASC",
                  resultSetMapping = "Mapping.LadeZaehldatumDTO")
@SqlResultSetMapping(name = "Mapping.LadeZaehldatumDTO",
                     classes = @ConstructorResult(targetClass = LadeZaehldatumDTO.class,
                                                  columns = {
                                                        @ColumnResult(name = "pkw", type = Integer.class),
                                                        @ColumnResult(name = "lkw", type = Integer.class),
                                                        @ColumnResult(name = "lastzuege", type = Integer.class),
                                                        @ColumnResult(name = "busse", type = Integer.class),
                                                        @ColumnResult(name = "kraftraeder", type = Integer.class),
                                                        @ColumnResult(name = "fahrradfahrer", type = Integer.class),
                                                        @ColumnResult(name = "fussgaenger", type = Integer.class),
                                                        @ColumnResult(name = "startuhrzeit", type = LocalTime.class),
                                                        @ColumnResult(name = "endeuhrzeit", type = LocalTime.class)
                                                }))
 @Entity
// Definition of getter, setter, ...
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
                @Index(name = "index_fahrbeziehungid", columnList = "fahrbeziehung_id"),
                @Index(name = "index_combined_1", columnList = "zaehlung_id, type, fahrbeziehung_von, fahrbeziehung_nach"),
                @Index(name = "index_combined_2", columnList = "zaehlung_id, startuhrzeit, endeuhrzeit, fahrbeziehung_von, type"),
                @Index(
                        name = "index_combined_3",
                        columnList = "zaehlung_id, startuhrzeit, endeuhrzeit, fahrbeziehung_von, fahrbeziehung_nach, fahrbeziehung_fahrbewegungkreisverkehr, type"
                )
        }
)
public class Zeitintervall extends BaseEntity {

        public Zeitintervall(UUID zaehlungId, Integer pkw, Integer lkw, Integer lastzuege, Integer busse, Integer kraftraeder,
                Integer fahrradfahrer, Integer fussgaenger, LocalDateTime startUhrzeit, LocalDateTime endeUhrzeit) {
            this.zaehlungId = zaehlungId;
            this.pkw = pkw;
            this.lkw = lkw;
            this.lastzuege = lastzuege;
            this.busse = busse;
            this.kraftraeder = kraftraeder;
            this.fahrradfahrer = fahrradfahrer;
            this.fussgaenger = fussgaenger;
            this.startUhrzeit = startUhrzeit;
            this.endeUhrzeit = endeUhrzeit;
        }

    @Column(name = "zaehlung_id", nullable = false)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID zaehlungId;

    @Column(name = "fahrbeziehung_id")
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID fahrbeziehungId;

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
                @AttributeOverride(name = "von", column = @Column(name = "fahrbeziehung_von")),
                @AttributeOverride(name = "nach", column = @Column(name = "fahrbeziehung_nach")),
                @AttributeOverride(name = "fahrbewegungKreisverkehr", column = @Column(name = "fahrbeziehung_fahrbewegungkreisverkehr"))
        }
    )
    private Fahrbeziehung fahrbeziehung;

}
