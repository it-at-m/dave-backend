package de.muenchen.dave.domain.dtos.laden;

import de.muenchen.dave.domain.enums.Zeitblock;
import java.io.Serializable;
import java.util.Set;
import lombok.Data;

/**
 * Klasse stellt die möglichen Zeitblöcke und Stunden zur Verfügung welche ausgewählt werden können.
 */
@Data
public class ZeitauswahlDTO implements Serializable {

    /**
     * Die möglichen Zeitblöcke
     */
    Set<Zeitblock> blocks;

    /**
     * Die möglichen Stunden
     */
    Set<Zeitblock> hours;

}
