package de.muenchen.dave.domain;

import de.muenchen.dave.domain.enums.TagesTyp;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;

@Entity
@Table(
        indexes = {
                @Index(
                        name = "index_kalendertag_datum ",
                        columnList = "datum"
                )
        },
        uniqueConstraints = { @UniqueConstraint(
                name = "unique_kalendertag_datum ",
                columnNames = { "datum" }
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

}
