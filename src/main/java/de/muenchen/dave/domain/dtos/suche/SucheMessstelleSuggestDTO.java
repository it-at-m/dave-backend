package de.muenchen.dave.domain.dtos.suche;

import de.muenchen.dave.domain.elasticsearch.Zaehlstelle;
import lombok.Data;

@Data
public class SucheMessstelleSuggestDTO {

    String text;

    String id;

    /**
     * Zeigt ob Zählstelle im Datenportal sichtbar ist. {@link Zaehlstelle#getSichtbarDatenportal()}
     */
    Boolean sichtbarDatenportal;

}
