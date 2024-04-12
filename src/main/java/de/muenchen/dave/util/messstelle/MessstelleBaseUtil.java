/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2023
 */
package de.muenchen.dave.util.messstelle;

import de.muenchen.dave.geodateneai.gen.model.ChosenTagesTypValidRequestDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MessstelleBaseUtil {

    public static String getTextOfTagesTyp(final String tagestyp) {
        String text = tagestyp;
        if (StringUtils.isNotEmpty(tagestyp)) {
            ChosenTagesTypValidRequestDto.TagesTypEnum tagesTypEnum = ChosenTagesTypValidRequestDto.TagesTypEnum.fromValue(tagestyp);
            switch (tagesTypEnum) {
            case WERKTAG_DI_MI_DO:
                text = "DTVw3 (Di,Mi,Do - außerhalb Ferien)";
                break;
            case WERKTAG_MO_FR:
                text = "DTVw5 (Mo-Fr - außerhalb Ferien)";
                break;
            case SAMSTAG:
                text = "Samstag in/außerhalb Ferien";
                break;
            case SONNTAG_FEIERTAG:
                text = "Sonntag/Feiertag in/außerhalb Ferien";
                break;
            case WERKTAG_FERIEN:
                text = "Mo-Fr Ferien";
                break;
            case MO_SO:
                text = "DTV (MO - SO)";
                break;
            }
        }
        return text;
    }
}
