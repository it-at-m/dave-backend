package de.muenchen.dave.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum MessstelleStatus {
    IN_PLANUNG("in Planung / in Entwurf"), IN_BESTAND("in Bestand / in Betrieb"), AUSSER_BETRIEB(
            "außer Betrieb"), ABGEBAUT("abgebaut"), UNBEKANNT("unbekannt");

    /**
     * Die Beschreibung zum Status.
     */
    @Getter
    private final String beschreibung;
}
