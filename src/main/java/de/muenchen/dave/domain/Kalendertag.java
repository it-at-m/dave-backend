package de.muenchen.dave.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
    private Integer tagestyp;

}
