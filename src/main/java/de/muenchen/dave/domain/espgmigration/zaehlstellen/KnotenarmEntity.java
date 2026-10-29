package de.muenchen.dave.domain.espgmigration.zaehlstellen;

import de.muenchen.dave.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Entity-Klasse für Knotenarm (Migration von Elasticsearch zu PostgreSQL).
 */
@Entity
@Table(name = "knotenarm")
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class KnotenarmEntity extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "zaehlung_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ZaehlungEntity zaehlung;

    @Column(name = "nummer", nullable = false)
    private Integer nummer;

    @Column(name = "strassenname")
    private String strassenname;

    @Column(name = "filename")
    private String filename;

}
