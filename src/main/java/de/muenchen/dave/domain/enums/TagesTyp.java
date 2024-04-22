package de.muenchen.dave.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum TagesTyp {
    WERKTAG_DI_MI_DO("DTVw3 (Di,Mi,Do - außerhalb Ferien)"), WERKTAG_MO_FR("DTVw5 (Mo-Fr - außerhalb Ferien)"), SAMSTAG(
            "Samstag in/außerhalb Ferien"), SONNTAG_FEIERTAG("Sonntag/Feiertag in/außerhalb Ferien"), WERKTAG_FERIEN("Mo-Fr Ferien"),

    MO_SO("DTV (MO - SO)");

    /**
     * Die Beschreibung zum TagesTyp.
     */
    @Getter
    private final String beschreibung;
}
