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
        uniqueConstraints={
        @UniqueConstraint(
                name = "unique_optionsmenuesettings_fahrzeugklassen_intervall",
                columnNames = {"fahrzeugklassen", "intervall"}
        )
})
@Data
@EqualsAndHashCode(callSuper=true)
public class OptionsmenueSettings extends BaseEntity {

    @Embedded
    private OptionsmenueSettingsKey fahrzeugklassenAndIntervall;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private List<ZaehldatenIntervall> choosableIntervals;

    @Column(nullable = false)
    private boolean kraftfahrzeugverkehr;

    @Column(nullable = false)
    private boolean schwerverkehr;

    @Column(nullable = false)
    private boolean gueterverkehr;

    @Column(nullable = false)
    private boolean schwerverkehrsanteilProzent;

    @Column(nullable = false)
    private boolean gueterverkehrsanteilProzent;

    @Column(nullable = false)
    private boolean radverkehr;

    @Column(nullable = false)
    private boolean fussverkehr;

    @Column(nullable = false)
    private boolean lastkraftwagen;

    @Column(nullable = false)
    private boolean lastzuege;

    @Column(nullable = false)
    private boolean busse;

    @Column(nullable = false)
    private boolean kraftraeder;

    @Column(nullable = false)
    private boolean personenkraftwagen;

    @Column(nullable = false)
    private boolean lieferwagen;

}
