package de.muenchen.dave.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Table(
        indexes = {
                @Index(name = "index_active", columnList = "active"),
                @Index(name = "index_default_faktor", columnList = "default_faktor")
        }
)
public class Hochrechnungsfaktor extends BaseEntity {

    @Column(name = "matrix", unique = true)
    private String matrix;

    @Column(name = "kfz")
    private Double kfz;

    @Column(name = "sv")
    private Double sv;

    @Column(name = "gv")
    private Double gv;

    @Column(name = "active")
    private boolean active;

    @Column(name = "default_faktor")
    private boolean defaultFaktor;

}
