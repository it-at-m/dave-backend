package de.muenchen.dave.domain.dtos.suche;

import de.muenchen.dave.domain.elasticsearch.Zaehlstelle;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SucheZaehlstelleSuggestDTO {

    @NotNull
    String text;
    @NotNull
    String id;

    /**
     * Zeigt ob Zählstelle im Datenportal sichtbar ist. {@link Zaehlstelle#getSichtbarDatenportal()}
     */
    @NotNull
    Boolean sichtbarDatenportal;

}
