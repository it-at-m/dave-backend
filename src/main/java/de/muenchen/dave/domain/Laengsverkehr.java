package de.muenchen.dave.domain;

import de.muenchen.dave.domain.enums.Bewegungsrichtung;
import de.muenchen.dave.domain.enums.Himmelsrichtung;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

@Embeddable
@Data
public class Laengsverkehr {

    @Column(name = "richtung")
    @Enumerated(EnumType.STRING)
    private Bewegungsrichtung richtung;

    @Column(name = "strassenseite")
    @Enumerated(EnumType.STRING)
    private Himmelsrichtung strassenseite;

}
