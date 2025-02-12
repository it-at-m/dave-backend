package de.muenchen.dave.domain;

import de.muenchen.dave.domain.enums.TagesTyp;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;

@Entity
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Kalendertag extends BaseEntity {

    @Column(nullable = false, unique = true)
    private LocalDate datum;

    @Column
    private String ferientyp;

    @Column
    private String feiertag;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TagesTyp tagestyp;

}
