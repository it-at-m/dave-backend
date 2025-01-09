package de.muenchen.dave.domain;

import de.muenchen.dave.domain.enums.Fahrzeugklasse;
import de.muenchen.dave.domain.enums.ZaehldatenIntervall;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

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
public class OptionsmenueSettings extends BaseEntity {

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Fahrzeugklasse fahrzeugklasse;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ZaehldatenIntervall intervall;

    @Column
    @JdbcTypeCode(SqlTypes.JSON)
    @Enumerated(EnumType.STRING)
    private List<ZaehldatenIntervall> kraftfahrzeugverkehrChoosableIntervals;

    @Column
    @JdbcTypeCode(SqlTypes.JSON)
    @Enumerated(EnumType.STRING)
    private List<ZaehldatenIntervall> schwerverkehrChoosableIntervals;

    @Column
    @JdbcTypeCode(SqlTypes.JSON)
    @Enumerated(EnumType.STRING)
    private List<ZaehldatenIntervall> gueterverkehrChoosableIntervals;

    @Column
    @JdbcTypeCode(SqlTypes.JSON)
    @Enumerated(EnumType.STRING)
    private List<ZaehldatenIntervall> schwerverkehrsanteilProzentChoosableIntervals;

    @Column
    @JdbcTypeCode(SqlTypes.JSON)
    @Enumerated(EnumType.STRING)
    private List<ZaehldatenIntervall> gueterverkehrsanteilProzentChoosableIntervals;

    @Column
    @JdbcTypeCode(SqlTypes.JSON)
    @Enumerated(EnumType.STRING)
    private List<ZaehldatenIntervall> radverkehrChoosableIntervals;

    @Column
    @JdbcTypeCode(SqlTypes.JSON)
    @Enumerated(EnumType.STRING)
    private List<ZaehldatenIntervall> fussverkehrChoosableIntervals;

    @Column
    @JdbcTypeCode(SqlTypes.JSON)
    @Enumerated(EnumType.STRING)
    private List<ZaehldatenIntervall> lastkraftwagenChoosableIntervals;

    @Column
    @JdbcTypeCode(SqlTypes.JSON)
    @Enumerated(EnumType.STRING)
    private List<ZaehldatenIntervall> lastzuegeChoosableIntervals;

    @Column
    @JdbcTypeCode(SqlTypes.JSON)
    @Enumerated(EnumType.STRING)
    private List<ZaehldatenIntervall> busseChoosableIntervals;

    @Column
    @JdbcTypeCode(SqlTypes.JSON)
    @Enumerated(EnumType.STRING)
    private List<ZaehldatenIntervall> kraftraederChoosableIntervals;

    @Column
    @JdbcTypeCode(SqlTypes.JSON)
    @Enumerated(EnumType.STRING)
    private List<ZaehldatenIntervall> personenkraftwagenChoosableIntervals;

    @Column
    @JdbcTypeCode(SqlTypes.JSON)
    @Enumerated(EnumType.STRING)
    private List<ZaehldatenIntervall> lieferwagenChoosableIntervals;

}
