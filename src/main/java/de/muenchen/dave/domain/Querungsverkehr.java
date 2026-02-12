package de.muenchen.dave.domain;

import de.muenchen.dave.domain.enums.Himmelsrichtung;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

@Embeddable
@Data
public class Querungsverkehr {

    @Column(name = "richtung")
    @Enumerated(EnumType.STRING)
    private Himmelsrichtung richtung;

}
