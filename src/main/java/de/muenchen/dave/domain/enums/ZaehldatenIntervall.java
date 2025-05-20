/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.domain.enums;

import de.muenchen.dave.geodateneai.gen.model.MesswertRequestDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ZaehldatenIntervall {

    STUNDE_VIERTEL(1, 15, MesswertRequestDto.IntervalInMinutesEnum._15, TypeZeitintervall.STUNDE_VIERTEL),

    STUNDE_VIERTEL_EINGESCHRAENKT(1, 15, MesswertRequestDto.IntervalInMinutesEnum._15, TypeZeitintervall.STUNDE_VIERTEL),

    STUNDE_HALB(2, 30, MesswertRequestDto.IntervalInMinutesEnum._30, TypeZeitintervall.STUNDE_HALB),

    STUNDE_KOMPLETT(4, 60, MesswertRequestDto.IntervalInMinutesEnum._60, TypeZeitintervall.STUNDE_KOMPLETT);

    private final Integer quarterPerIntervall;

    private final Integer minutesPerIntervall;

    private final MesswertRequestDto.IntervalInMinutesEnum messwertIntervalInMinutes;

    private final TypeZeitintervall typeZeitintervall;

}
