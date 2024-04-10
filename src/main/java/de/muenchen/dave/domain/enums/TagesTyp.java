package de.muenchen.dave.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum TagesTyp {
    WERKTAG_DI_MI_DO("Di, Mi, Do, außerhalb Ferienzeiten"), WERKTAG_MO_FR("Mo oder Fr außerhalb Ferienzeiten"), SAMSTAG(
            "außerhalb/innerhalb Ferienzeiten"), SONNTAG_FEIERTAG("außerhalb/innerhalb Ferienzeiten"), WERKTAG_FERIEN("Werktag Ferien"),

    MO_SO("Alle Tage der Woche");

    /**
     * Die Beschreibung zum TagesTyp.
     */
    @Getter
    private final String beschreibung;
}