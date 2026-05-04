package de.muenchen.dave.services.messstelle;

import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatumDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatumTageswertDTO;
import de.muenchen.dave.domain.dtos.laden.messwerte.LadeMesswerteDTO;
import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class RoundingService {

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

    /**
     * Diese Methode rundet die Zahlinformationen im {@link LadeZaehldatumDTO} des Parameters "toRound"
     * auf den nächsten Wert welcher im Parameter
     * "nearestValueToRound" angegeben ist.
     * <p>
     * Eine Rundung wird durchgeführt sobald {@link OptionsDTO}#getWerteHundertRunden() den Wert true
     * besitzt.
     * <p>
     * Sobald der Wert im Zehnerbereich kleiner 50 wird auf den nächsten 100er-Wert abgerundet.
     * Andernfalls wird aufgerundet.
     *
     * @param toRound Auf welchem die Rundung durchgeführt werden soll.
     * @param nearestValueToRound Der Wert auf welchen aufgerundet werden soll.
     * @param optionsDto Um auf Durchführung der Rundung zu prüfen
     * @return den gerundeten {@link LadeZaehldatumDTO}, falls
     *         {@link OptionsDTO}#getWerteHundertRunden() den Wert true besitzt. Andernfall wird das
     *         {@link LadeZaehldatumDTO} im Parameter zurückgegeben.
     */
    public static LadeZaehldatumDTO roundToNearestIfRoundingIsChoosen(final LadeZaehldatumDTO toRound,
            final int nearestValueToRound,
            final OptionsDTO optionsDto) {
        if (BooleanUtils.isTrue(optionsDto.getWerteHundertRunden())) {
            final LadeZaehldatumTageswertDTO ladeZaehldatumDTO = new LadeZaehldatumTageswertDTO();
            ladeZaehldatumDTO.setType(toRound.getType());
            ladeZaehldatumDTO.setStartUhrzeit(toRound.getStartUhrzeit());
            ladeZaehldatumDTO.setEndeUhrzeit(toRound.getEndeUhrzeit());
            ladeZaehldatumDTO.setPkw(
                    roundIfNotNullOrZero(toRound.getPkw(), nearestValueToRound));
            ladeZaehldatumDTO.setLkw(
                    roundIfNotNullOrZero(toRound.getLkw(), nearestValueToRound));
            ladeZaehldatumDTO.setLastzuege(
                    roundIfNotNullOrZero(toRound.getLastzuege(), nearestValueToRound));
            ladeZaehldatumDTO.setBusse(
                    roundIfNotNullOrZero(toRound.getBusse(), nearestValueToRound));
            ladeZaehldatumDTO.setKraftraeder(
                    roundIfNotNullOrZero(toRound.getKraftraeder(), nearestValueToRound));
            ladeZaehldatumDTO.setFahrradfahrer(
                    roundIfNotNullOrZero(toRound.getFahrradfahrer(), nearestValueToRound));
            ladeZaehldatumDTO.setFussgaenger(
                    roundIfNotNullOrZero(toRound.getFussgaenger(), nearestValueToRound));
            ladeZaehldatumDTO.setPkwEinheiten(
                    roundIfNotNullOrZero(toRound.getPkwEinheiten(), nearestValueToRound));
            ladeZaehldatumDTO.setKfz(
                    roundIfNotNullOrZero(toRound.getKfz(), nearestValueToRound));
            ladeZaehldatumDTO.setSchwerverkehr(
                    roundIfNotNullOrZero(toRound.getSchwerverkehr(), nearestValueToRound));
            ladeZaehldatumDTO.setGueterverkehr(
                    roundIfNotNullOrZero(toRound.getGueterverkehr(), nearestValueToRound));
            return ladeZaehldatumDTO;
        } else {
            return toRound;
        }
    }

    /**
     * Führt eine Rundung durch sobald der Wert im Parameter "toRound" nicht NULL oder 0 ist.
     * Andernfalls wird der übergebene Wert zurückgegeben.
     * <p>
     * Sobald der Wert im Zehnerbereich kleiner 50 ist, wird auf den nächsten 100er-Wert abgerundet.
     * Andernfalls wird aufgerundet.
     *
     * @param toRound Der Wert welcher gerundet werden soll
     * @param nearestValueToRound Der nächste Wert auf den gerundet werden soll.
     * @return den gerundeten Wert oder der übergebene Wert falls keine Rundung durchgeführt wurde.
     */
    public static Integer roundIfNotNullOrZero(final Integer toRound, final int nearestValueToRound) {
        final Integer roundedValue;
        if (ObjectUtils.isNotEmpty(toRound)) {
            roundedValue = roundIfNotNullOrZero(BigDecimal.valueOf(toRound), nearestValueToRound).intValue();
        } else {
            roundedValue = toRound;
        }
        return roundedValue;
    }

    /**
     * Führt eine Rundung durch sobald der Wert im Parameter "toRound" nicht NULL oder 0 ist.
     * Andernfalls wird der übergebene Wert zurückgegeben.
     * <p>
     * Sobald der Wert im Zehnerbereich kleiner 50 ist, wird auf den nächsten 100er-Wert abgerundet.
     * Andernfalls wird aufgerundet.
     *
     * @param toRound Der Wert welcher gerundet werden soll
     * @param nearestValueToRound Der nächste Wert auf den gerundet werden soll.
     * @return den gerundeten Wert.
     */
    public static BigDecimal roundIfNotNullOrZero(final BigDecimal toRound, final int nearestValueToRound) {
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
