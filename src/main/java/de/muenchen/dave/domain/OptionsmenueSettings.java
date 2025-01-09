package de.muenchen.dave.domain;

import de.muenchen.dave.domain.enums.ZaehldatenIntervall;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
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
                        name = "unique_optionsmenuesettings_fahrzeugklassen_intervall",
                        columnNames = { "fahrzeugklassen", "intervall" }
                )
        }
)
@Data
@EqualsAndHashCode(callSuper = true)
public class OptionsmenueSettings extends BaseEntity {

    @Embedded
    private OptionsmenueSettingsKey fahrzeugklassenAndIntervall;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column
    private List<ZaehldatenIntervall> kraftfahrzeugverkehrChoosableIntervals;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column
    private List<ZaehldatenIntervall> schwerverkehrChoosableIntervals;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column
    private List<ZaehldatenIntervall> gueterverkehrChoosableIntervals;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column
    private List<ZaehldatenIntervall> schwerverkehrsanteilProzentChoosableIntervals;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column
    private List<ZaehldatenIntervall> gueterverkehrsanteilProzentChoosableIntervals;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column
    private List<ZaehldatenIntervall> radverkehrChoosableIntervals;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column
    private List<ZaehldatenIntervall> fussverkehrChoosableIntervals;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column
    private List<ZaehldatenIntervall> lastkraftwagenChoosableIntervals;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column
    private List<ZaehldatenIntervall> lastzuegeChoosableIntervals;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column
    private List<ZaehldatenIntervall> busseChoosableIntervals;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column
    private List<ZaehldatenIntervall> kraftraederChoosableIntervals;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column
    private List<ZaehldatenIntervall> personenkraftwagenChoosableIntervals;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column
    private List<ZaehldatenIntervall> lieferwagenChoosableIntervals;

}
