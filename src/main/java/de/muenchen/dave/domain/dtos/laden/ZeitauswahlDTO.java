package de.muenchen.dave.domain.dtos.laden;

import de.muenchen.dave.domain.enums.Zeitblock;
import java.io.Serializable;
import java.util.Set;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Klasse stellt die möglichen Zeitblöcke und Stunden zur Verfügung welche ausgewählt werden können.
 */
@Data
public class ZeitauswahlDTO implements Serializable {

    /**
     * Die möglichen Zeitblöcke
     */
    @NotNull
    Set<Zeitblock> blocks;

    /**
     * Die möglichen Stunden
     */
    @NotNull
    Set<Zeitblock> hours;

}
