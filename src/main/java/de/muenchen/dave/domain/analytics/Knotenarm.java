package de.muenchen.dave.domain.analytics;

import de.muenchen.dave.domain.BaseEntity;
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
public class Knotenarm extends BaseEntity {

    @Column(name = "nummer")
    private int nummer;

    @Column(name = "strassenname")
    private String strassenname;

    @Column(name = "filename")
    private String filename;

    @ManyToOne
    @JoinColumn(name = "zaehlung", referencedColumnName = "id")
    private Zaehlung zaehlung;
}
