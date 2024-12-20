package de.muenchen.dave.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Embeddable
@Data
@EqualsAndHashCode
public class OptionsmenueSettingsKey {

    @Column(nullable = false)
    private String fahrzeugklassen;

    @Column(nullable = false)
    private String intervall;

}
