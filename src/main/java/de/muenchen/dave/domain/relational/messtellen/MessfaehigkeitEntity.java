package de.muenchen.dave.domain.relational.messtellen;

import de.muenchen.dave.domain.BaseEntity;
import de.muenchen.dave.domain.enums.Fahrzeugklasse;
import de.muenchen.dave.domain.enums.ZaehldatenIntervall;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Entity-Klasse für Messfähigkeit (Migration von Elasticsearch zu PostgreSQL).
 */
@Entity
@Table(name = "messfaehigkeit")
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class MessfaehigkeitEntity extends BaseEntity {

    @Column(name = "gueltig_ab")
    @JdbcTypeCode(SqlTypes.DATE)
    private LocalDate gueltigAb;

    @Column(name = "gueltig_bis")
    @JdbcTypeCode(SqlTypes.DATE)
    private LocalDate gueltigBis;

    @Column(name = "fahrzeugklasse")
    @Enumerated(EnumType.STRING)
    private Fahrzeugklasse fahrzeugklasse;

    @Column(name = "intervall")
    @Enumerated(EnumType.STRING)
    private ZaehldatenIntervall intervall;

}
