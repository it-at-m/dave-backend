package de.muenchen.dave.domain.elasticsearch;

import de.muenchen.dave.domain.enums.Himmelsrichtung;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Querungsverkehr extends Bewegungsbeziehung {

    private Himmelsrichtung richtung;

}
