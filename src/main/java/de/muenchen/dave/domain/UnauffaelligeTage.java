package de.muenchen.dave.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;

@Entity
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Table(
        indexes = {
                @Index(
                        name = "index_unauffaellige_tage_mst_id_datum",
                        columnList = "mst_id, datum"
                )
        },
        uniqueConstraints = { @UniqueConstraint(
                name = "unique_unauffaellige_tage_mst_id_datum",
                columnNames = { "mst_id", "datum" }
        )
        }
)
public class UnauffaelligeTage extends BaseEntity {

    @Column(nullable = false)
    private Integer mstId;

    @Column(nullable = false)
    private LocalDate datum;
}
