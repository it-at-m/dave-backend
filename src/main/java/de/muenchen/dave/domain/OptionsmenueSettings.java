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
    private boolean kraftfahrzeugverkehrChoosableIntervals;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column
    private boolean schwerverkehrChoosableIntervals;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column
    private boolean gueterverkehrChoosableIntervals;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column
    private boolean schwerverkehrsanteilProzentChoosableIntervals;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column
    private boolean gueterverkehrsanteilProzentChoosableIntervals;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column
    private boolean radverkehrChoosableIntervals;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column
    private boolean fussverkehrChoosableIntervals;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column
    private boolean lastkraftwagenChoosableIntervals;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column
    private boolean lastzuegeChoosableIntervals;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column
    private boolean busseChoosableIntervals;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column
    private boolean kraftraederChoosableIntervals;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column
    private boolean personenkraftwagenChoosableIntervals;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column
    private boolean lieferwagenChoosableIntervals;

}
