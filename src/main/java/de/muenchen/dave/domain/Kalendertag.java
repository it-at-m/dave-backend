package de.muenchen.dave.domain;

import de.muenchen.dave.domain.enums.TagesTyp;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(
        indexes = {
                @Index(
                        name = "index_kalendertag_datum ",
                        columnList = "datum"
                ),
                @Index(
                        name = "index_kalendertag_next_start_date ",
                        columnList = "next_start_date_to_load_unauffaellige_tage"
                ),
                @Index(
                        name = "index_kalendertag_id ",
                        columnList = "unauffaelliger_tag"
                )
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "unique_kalendertag_datum ",
                        columnNames = { "datum" }
                ),
                @UniqueConstraint(
                        name = "unique_kalendertag_next_start_date ",
                        columnNames = { "next_start_date_to_load_unauffaellige_tage" }
                )
        }
)
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Kalendertag extends BaseEntity {

    @Column(nullable = false, unique = true)
    private LocalDate datum;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TagesTyp tagestyp;

    @Column(name = "next_start_date_to_load_unauffaellige_tage", unique = true)
    private Boolean nextStartDateToLoadUnauffaelligeTage;
}
