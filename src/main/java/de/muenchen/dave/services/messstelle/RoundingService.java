/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2023
 */
package de.muenchen.dave.services.messstelle;

import de.muenchen.dave.domain.dtos.laden.messwerte.LadeMesswerteDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class RoundingService {

    private static final Integer ROUND_TO_HUNDRED = 100;

    // TODO Tests schreiben

    public void roundToNearestHundred(final List<LadeMesswerteDTO> messwerte) {
        messwerte.forEach(ladeMesswerteDTO -> roundToNearest(ladeMesswerteDTO, ROUND_TO_HUNDRED));
    }

   public void roundToNearest(final List<LadeMesswerteDTO> messwerte, final int nearestValueToRound) {
       messwerte.forEach(ladeMesswerteDTO -> roundToNearest(ladeMesswerteDTO, nearestValueToRound));
   }

    public void roundToNearest(final LadeMesswerteDTO messwert, final int nearestValueToRound) {
        messwert.setPkw(roundIfNotNullOrZero(messwert.getPkw(), nearestValueToRound));
        messwert.setLkw(roundIfNotNullOrZero(messwert.getLkw(), nearestValueToRound));
        messwert.setLfw(roundIfNotNullOrZero(messwert.getLfw(), nearestValueToRound));
        messwert.setLastzuege(roundIfNotNullOrZero(messwert.getLastzuege(), nearestValueToRound));
        messwert.setBusse(roundIfNotNullOrZero(messwert.getBusse(), nearestValueToRound));
        messwert.setKraftraeder(roundIfNotNullOrZero(messwert.getKraftraeder(), nearestValueToRound));
        messwert.setFahrradfahrer(roundIfNotNullOrZero(messwert.getFahrradfahrer(), nearestValueToRound));
        messwert.setFussgaenger(roundIfNotNullOrZero(messwert.getFussgaenger(), nearestValueToRound));
        messwert.setKfz(roundIfNotNullOrZero(messwert.getKfz(), nearestValueToRound));
        messwert.setSchwerverkehr(roundIfNotNullOrZero(messwert.getSchwerverkehr(), nearestValueToRound));
        messwert.setGueterverkehr(roundIfNotNullOrZero(messwert.getGueterverkehr(), nearestValueToRound));
    }

    protected Integer roundIfNotNullOrZero(final Integer toRound, final int nearestValueToRound) {
        final Integer roundedValue;
        if (ObjectUtils.isNotEmpty(toRound)) {
            roundedValue = roundIfNotNullOrZero(BigDecimal.valueOf(toRound), nearestValueToRound).intValue();
        } else {
            roundedValue = toRound;
        }
        return roundedValue;
    }

    protected BigDecimal roundIfNotNullOrZero(final BigDecimal toRound, final int nearestValueToRound) {
        final BigDecimal roundedValue;
        if (ObjectUtils.isNotEmpty(toRound) && !toRound.equals(BigDecimal.ZERO)) {
            roundedValue = toRound
                    .divide(BigDecimal.valueOf(nearestValueToRound))
                    .setScale(0, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(nearestValueToRound));
        } else {
            roundedValue = toRound;
        }
        return roundedValue;
    }

}
