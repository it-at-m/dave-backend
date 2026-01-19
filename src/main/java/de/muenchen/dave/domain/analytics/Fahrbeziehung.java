package de.muenchen.dave.domain.analytics;

import de.muenchen.dave.domain.BaseEntity;
import de.muenchen.dave.domain.Hochrechnungsfaktor;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
// Definition of getter, setter, ...
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Fahrbeziehung extends BaseEntity {

    @Column(name = "is_kreuzung")
    private Boolean isKreuzung;

    // Kreuzung
    @Column(name = "von")
    private Integer von;

    @Column(name = "nach")
    private Integer nach;

    // Kreisverkehr
    @Column(name = "knotenarm")
    private Integer knotenarm;

    @Column(name = "hinein")
    private Boolean hinein;

    @Column(name = "heraus")
    private Boolean heraus;

    @Column(name = "vorbei")
    private Boolean vorbei;

    // Knoten-Kanten-Modell
    @Column(name = "vonknotvonstrnr")
    private String vonknotvonstrnr;

    @Column(name = "nachknotvonstrnr")
    private String nachknotvonstrnr;

    @Column(name = "von_strnr")
    private String von_strnr;

    @Column(name = "vonknotennachstrnr")
    private String vonknotennachstrnr;

    @Column(name = "nachknotnachstrnr")
    private String nachknotnachstrnr;

    @Column(name = "nach_strnr")
    private String nach_strnr;

    @ManyToOne(cascade = CascadeType.REFRESH, optional = true)
    @JoinColumn(name = "hochrechnungsfaktor")
    private Hochrechnungsfaktor hochrechnungsfaktor;

    @ManyToOne
    @JoinColumn(name = "zaehlung", referencedColumnName = "id")
    private Zaehlung zaehlung;

}
