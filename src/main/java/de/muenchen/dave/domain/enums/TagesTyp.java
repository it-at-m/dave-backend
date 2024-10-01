package de.muenchen.dave.domain.enums;

import de.muenchen.dave.geodateneai.gen.model.MesswertRequestDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum TagesTyp {

    WERKTAG_DI_MI_DO("DTVw3 (Di,Mi,Do - außerhalb Ferien)", MesswertRequestDto.TagesTypEnum.DTV_W3),

    WERKTAG_MO_FR("DTVw5 (Mo-Fr - außerhalb Ferien)", MesswertRequestDto.TagesTypEnum.DTV_W5),

    SAMSTAG("Samstag in/außerhalb Ferien", MesswertRequestDto.TagesTypEnum.SAMSTAG),

    SONNTAG_FEIERTAG("Sonntag/Feiertag in/außerhalb Ferien", MesswertRequestDto.TagesTypEnum.SONNTAG_FEIERTAG),

    WERKTAG_FERIEN("Mo-Fr Ferien", MesswertRequestDto.TagesTypEnum.WERKTAG_FERIEN),

    MO_SO("DTV (MO - SO)", MesswertRequestDto.TagesTypEnum.DTV);

    /**
     * Die Beschreibung zum TagesTyp.
     */
    @Getter
    private final String beschreibung;

    @Getter
    private final MesswertRequestDto.TagesTypEnum messwertTyp;
}
