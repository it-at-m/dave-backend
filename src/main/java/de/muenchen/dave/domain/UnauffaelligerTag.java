package de.muenchen.dave.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Table(
        indexes = {
                @Index(
                        name = "index_unauffaelliger_tag_mst_id",
                        columnList = "mst_id"
                )
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "unique_unauffaelliger_tag_mst_id_kalendertag_id",
                        columnNames = { "mst_id", "kalendertag_id" }
                )
        }
)
public class UnauffaelligerTag extends BaseEntity {

    @Column(name = "mst_id", nullable = false)
    private Integer mstId;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "kalendertag_id")
    private Kalendertag kalendertag;
}
