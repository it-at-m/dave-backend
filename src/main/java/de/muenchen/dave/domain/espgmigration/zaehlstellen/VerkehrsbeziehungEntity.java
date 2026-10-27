package de.muenchen.dave.domain.espgmigration.zaehlstellen;

import de.muenchen.dave.domain.enums.Himmelsrichtung;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Entity-Klasse für Verkehrsbeziehung (Migration von Elasticsearch zu PostgreSQL).
 */
@Entity
@Table(name = "verkehrsbeziehung")
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class VerkehrsbeziehungEntity extends MappedBewegungsbeziehung {

    @Column(name = "is_kreuzung")
    private Boolean isKreuzung;

    @Column(name = "von")
    private Integer von;

    @Column(name = "nach")
    private Integer nach;

    @Column(name = "knotenarm")
    private Integer knotenarm;

    @Column(name = "hinein")
    private Boolean hinein;

    @Column(name = "heraus")
    private Boolean heraus;

    @Column(name = "vorbei")
    private Boolean vorbei;

    @Column(name = "strassenseite")
    @Enumerated(EnumType.STRING)
    private Himmelsrichtung strassenseite;

    @Column(name = "vonknotvonstrnr")
    private String vonknotvonstrnr;

    @Column(name = "nachknotvonstrnr")
    private String nachknotvonstrnr;

    @Column(name = "von_strnr")
    private String vonStrnr;

    @Column(name = "vonknotennachstrnr")
    private String vonknotennachstrnr;

    @Column(name = "nachknotnachstrnr")
    private String nachknotnachstrnr;

    @Column(name = "nach_strnr")
    private String nachStrnr;

}
