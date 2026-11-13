package de.muenchen.dave.domain.dtos.suche;

import de.muenchen.dave.domain.elasticsearch.Zaehlstelle;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SucheZaehlungSuggestDTO {

    @NotNull
    String text;
    @NotNull
    String id;
    @NotNull
    String zaehlstelleId;

    /**
     * Zeigt ob Zählstelle der Zählung im Datenportal sichtbar ist.
     * {@link Zaehlstelle#getSichtbarDatenportal()}
     */
    @NotNull
    Boolean sichtbarDatenportal;

}
