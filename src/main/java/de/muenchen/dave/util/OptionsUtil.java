/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.util;

import de.muenchen.dave.domain.enums.Zeitauswahl;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class OptionsUtil {

    public static boolean isZeitauswahlSpitzenstunde(final String zeitauswahl) {
        return StringUtils.equalsIgnoreCase(zeitauswahl, Zeitauswahl.SPITZENSTUNDE_KFZ.getCapitalizedName())
                || StringUtils.equalsIgnoreCase(zeitauswahl, Zeitauswahl.SPITZENSTUNDE_RAD.getCapitalizedName())
                || StringUtils.equalsIgnoreCase(zeitauswahl, Zeitauswahl.SPITZENSTUNDE_FUSS.getCapitalizedName());
    }
}
