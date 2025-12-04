package de.muenchen.dave.domain;

import de.muenchen.dave.domain.enums.Fahrzeugklasse;
import de.muenchen.dave.domain.enums.ZaehldatenIntervall;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "unique_optionsmenuesettings_fahrzeugklasse_intervall",
                        columnNames = { "fahrzeugklasse", "intervall" }
                )
        }
)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OptionsmenueSettings extends BaseEntity {

    @Column
    @Enumerated(EnumType.STRING)
    private Fahrzeugklasse fahrzeugklasse;

    @Column
    @Enumerated(EnumType.STRING)
    private ZaehldatenIntervall intervall;

    @Column(name = "kraftfahrzeugverkehr_choosable_intervals")
    @JdbcTypeCode(SqlTypes.JSON)
    @Enumerated(EnumType.STRING)
    private List<ZaehldatenIntervall> kraftfahrzeugverkehrChoosableIntervals;

    @Column(name = "schwerverkehr_choosable_intervals")
    @JdbcTypeCode(SqlTypes.JSON)
    @Enumerated(EnumType.STRING)
    private List<ZaehldatenIntervall> schwerverkehrChoosableIntervals;

    @Column(name = "gueterverkehr_choosable_intervals")
    @JdbcTypeCode(SqlTypes.JSON)
    @Enumerated(EnumType.STRING)
    private List<ZaehldatenIntervall> gueterverkehrChoosableIntervals;

    @Column(name = "schwerverkehrsanteil_prozent_choosable_intervals")
    @JdbcTypeCode(SqlTypes.JSON)
    @Enumerated(EnumType.STRING)
    private List<ZaehldatenIntervall> schwerverkehrsanteilProzentChoosableIntervals;

    @Column(name = "gueterverkehrsanteil_prozent_choosable_intervals")
    @JdbcTypeCode(SqlTypes.JSON)
    @Enumerated(EnumType.STRING)
    private List<ZaehldatenIntervall> gueterverkehrsanteilProzentChoosableIntervals;

    @Column(name = "radverkehr_choosable_intervals")
    @JdbcTypeCode(SqlTypes.JSON)
    @Enumerated(EnumType.STRING)
    private List<ZaehldatenIntervall> radverkehrChoosableIntervals;

    @Column(name = "fussverkehr_choosable_intervals")
    @JdbcTypeCode(SqlTypes.JSON)
    @Enumerated(EnumType.STRING)
    private List<ZaehldatenIntervall> fussverkehrChoosableIntervals;

    @Column(name = "lastkraftwagen_choosable_intervals")
    @JdbcTypeCode(SqlTypes.JSON)
    @Enumerated(EnumType.STRING)
    private List<ZaehldatenIntervall> lastkraftwagenChoosableIntervals;

    @Column(name = "lastzuege_choosable_intervals")
    @JdbcTypeCode(SqlTypes.JSON)
    @Enumerated(EnumType.STRING)
    private List<ZaehldatenIntervall> lastzuegeChoosableIntervals;

    @Column(name = "busse_choosable_intervals")
    @JdbcTypeCode(SqlTypes.JSON)
    @Enumerated(EnumType.STRING)
    private List<ZaehldatenIntervall> busseChoosableIntervals;

    @Column(name = "kraftraeder_choosable_intervals")
    @JdbcTypeCode(SqlTypes.JSON)
    @Enumerated(EnumType.STRING)
    private List<ZaehldatenIntervall> kraftraederChoosableIntervals;

    @Column(name = "personenkraftwagen_choosable_intervals")
    @JdbcTypeCode(SqlTypes.JSON)
    @Enumerated(EnumType.STRING)
    private List<ZaehldatenIntervall> personenkraftwagenChoosableIntervals;

    @Column(name = "lieferwagen_choosable_intervals")
    @JdbcTypeCode(SqlTypes.JSON)
    @Enumerated(EnumType.STRING)
    private List<ZaehldatenIntervall> lieferwagenChoosableIntervals;

}
