package de.muenchen.dave.domain.elasticsearch;

import de.muenchen.dave.domain.enums.Bewegungsrichtung;
import de.muenchen.dave.domain.enums.Himmelsrichtung;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Laengsverkehr extends Bewegungsbeziehung {

    @NotNull
    private Integer knotenarm;
    @NotNull
    private Bewegungsrichtung richtung;
    @NotNull
    private Himmelsrichtung strassenseite;

}
