package de.muenchen.dave.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;


@Entity
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Table(indexes = {
        @Index(
                name = "index_active",
                columnList = "active"
        ),
        @Index(
                name = "index_default_faktor",
                columnList = "default_faktor"
        )
})
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
