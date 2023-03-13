package de.muenchen.dave.domain.dtos.suche;

import de.muenchen.dave.domain.elasticsearch.Zaehlstelle;
import lombok.Data;


@Data
public class SucheZaehlungSuggestDTO {

    String text;

    String id;

    String zaehlstelleId;

    /**
     * Zeigt ob Zählstelle der Zählung im Datenportal sichtbar ist.
     * {@link Zaehlstelle#getSichtbarDatenportal()}
     */
    Boolean sichtbarDatenportal;

}
