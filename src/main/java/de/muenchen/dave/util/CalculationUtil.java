/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.util;

import de.muenchen.dave.domain.dtos.laden.LadeZaehldatumDTO;
import de.muenchen.dave.domain.elasticsearch.PkwEinheit;
import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class CalculationUtil {

    private static final BigDecimal FACTOR_PERCENTAGE_CALCULATION = BigDecimal.valueOf(100);

    private static final Integer SCALE_DEVISION = 4;

    private static final Integer SCALE_RESULT_PERCENTAGE_CALCULATION = 1;

    private static final Integer SCALE_FOR_ROUNDING_PKW_EINHEITEN = 0;

    private static final Integer DEFAULT_VALUE_IF_NULL = 0;

    public static BigDecimal calculateAnteilSchwerverkehrAnKfzProzent(final LadeZaehldatumDTO ladeZaehldatum) {
        return calculateAnteilProzent(ladeZaehldatum.getSchwerverkehr(), ladeZaehldatum.getKfz());
    }

    public static BigDecimal calculateAnteilGueterverkehrAnKfzProzent(final LadeZaehldatumDTO ladeZaehldatum) {
        return calculateAnteilProzent(ladeZaehldatum.getGueterverkehr(), ladeZaehldatum.getKfz());
    }

    public static BigDecimal calculateAnteilProzent(final BigDecimal dividend, final BigDecimal divisor) {
        BigDecimal result = BigDecimal.valueOf(DEFAULT_VALUE_IF_NULL, 0);
        if (ObjectUtils.isNotEmpty(divisor)
                && !(divisor.compareTo(BigDecimal.ZERO) == 0)) {
            result = dividend
                    .divide(divisor, SCALE_DEVISION, RoundingMode.HALF_UP)
                    .multiply(FACTOR_PERCENTAGE_CALCULATION)
                    .setScale(SCALE_RESULT_PERCENTAGE_CALCULATION, RoundingMode.HALF_UP);
        }
        return result;
    }

    public static BigDecimal getGueterverkehr(final LadeZaehldatumDTO ladeZaehldatum) {
        final Integer result = ObjectUtils.defaultIfNull(ladeZaehldatum.getLkw(), DEFAULT_VALUE_IF_NULL)
                + ObjectUtils.defaultIfNull(ladeZaehldatum.getLastzuege(), DEFAULT_VALUE_IF_NULL);
        return new BigDecimal(result);
    }

    public static BigDecimal getSchwerverkehr(final LadeZaehldatumDTO ladeZaehldatum) {
        final Integer result = getGueterverkehr(ladeZaehldatum).intValueExact()
                + ObjectUtils.defaultIfNull(ladeZaehldatum.getBusse(), DEFAULT_VALUE_IF_NULL);
        return new BigDecimal(result);
    }

    public static BigDecimal getKfz(final LadeZaehldatumDTO ladeZaehldatum) {
        final Integer result = ObjectUtils.defaultIfNull(ladeZaehldatum.getPkw(), DEFAULT_VALUE_IF_NULL)
                + getSchwerverkehr(ladeZaehldatum).intValueExact()
                + ObjectUtils.defaultIfNull(ladeZaehldatum.getKraftraeder(), DEFAULT_VALUE_IF_NULL);
        return new BigDecimal(result);
    }

    public static BigDecimal getGesamt(final LadeZaehldatumDTO ladeZaehldatum) {
        final Integer result = getKfz(ladeZaehldatum).intValueExact()
                + ObjectUtils.defaultIfNull(ladeZaehldatum.getFahrradfahrer(), DEFAULT_VALUE_IF_NULL)
                + ObjectUtils.defaultIfNull(ladeZaehldatum.getFussgaenger(), DEFAULT_VALUE_IF_NULL);
        return new BigDecimal(result);
    }

    public static Integer calculatePkwEinheiten(final LadeZaehldatumDTO ladeZaehldatum,
            final PkwEinheit pkwEinheit) {
        log.debug("Calculate PkwEinheiten");
        Integer calculationResult;
        try {
            calculationResult = calculatePkwEinheitenForPkw(ladeZaehldatum, pkwEinheit)
                    .add(calculatePkwEinheitenForLkw(ladeZaehldatum, pkwEinheit))
                    .add(calculatePkwEinheitenForLastzuege(ladeZaehldatum, pkwEinheit))
                    .add(calculatePkwEinheitenForBusse(ladeZaehldatum, pkwEinheit))
                    .add(calculatePkwEinheitenForKraftraeder(ladeZaehldatum, pkwEinheit))
                    .add(calculatePkwEinheitenForFahrradfahrer(ladeZaehldatum, pkwEinheit))
                    .setScale(SCALE_FOR_ROUNDING_PKW_EINHEITEN, RoundingMode.HALF_UP)
                    .intValueExact();
        } catch (ArithmeticException arithmeticException) {
            log.debug("ArithmeticException due to integer overflow thrown");
            calculationResult = Integer.MAX_VALUE;
        }
        return calculationResult;
    }

    private static BigDecimal calculatePkwEinheitenForPkw(final LadeZaehldatumDTO ladeZaehldatum,
            final PkwEinheit pkwEinheit) {
        return BigDecimal.valueOf(ObjectUtils.defaultIfNull(ladeZaehldatum.getPkw(), DEFAULT_VALUE_IF_NULL))
                .multiply(ObjectUtils.defaultIfNull(pkwEinheit.getPkw(), BigDecimal.valueOf(DEFAULT_VALUE_IF_NULL)));
    }

    private static BigDecimal calculatePkwEinheitenForLkw(final LadeZaehldatumDTO ladeZaehldatum,
            final PkwEinheit pkwEinheit) {
        return BigDecimal.valueOf(ObjectUtils.defaultIfNull(ladeZaehldatum.getLkw(), DEFAULT_VALUE_IF_NULL))
                .multiply(ObjectUtils.defaultIfNull(pkwEinheit.getLkw(), BigDecimal.valueOf(DEFAULT_VALUE_IF_NULL)));
    }

    private static BigDecimal calculatePkwEinheitenForLastzuege(final LadeZaehldatumDTO ladeZaehldatum,
            final PkwEinheit pkwEinheit) {
        return BigDecimal.valueOf(ObjectUtils.defaultIfNull(ladeZaehldatum.getLastzuege(), DEFAULT_VALUE_IF_NULL))
                .multiply(ObjectUtils.defaultIfNull(pkwEinheit.getLastzuege(), BigDecimal.valueOf(DEFAULT_VALUE_IF_NULL)));
    }

    private static BigDecimal calculatePkwEinheitenForBusse(final LadeZaehldatumDTO ladeZaehldatum,
            final PkwEinheit pkwEinheit) {
        return BigDecimal.valueOf(ObjectUtils.defaultIfNull(ladeZaehldatum.getBusse(), DEFAULT_VALUE_IF_NULL))
                .multiply(ObjectUtils.defaultIfNull(pkwEinheit.getBusse(), BigDecimal.valueOf(DEFAULT_VALUE_IF_NULL)));
    }

    private static BigDecimal calculatePkwEinheitenForKraftraeder(final LadeZaehldatumDTO ladeZaehldatum,
            final PkwEinheit pkwEinheit) {
        return BigDecimal.valueOf(ObjectUtils.defaultIfNull(ladeZaehldatum.getKraftraeder(), DEFAULT_VALUE_IF_NULL))
                .multiply(ObjectUtils.defaultIfNull(pkwEinheit.getKraftraeder(), BigDecimal.valueOf(DEFAULT_VALUE_IF_NULL)));
    }

    private static BigDecimal calculatePkwEinheitenForFahrradfahrer(final LadeZaehldatumDTO ladeZaehldatum,
            final PkwEinheit pkwEinheit) {
        return BigDecimal.valueOf(ObjectUtils.defaultIfNull(ladeZaehldatum.getFahrradfahrer(), DEFAULT_VALUE_IF_NULL))
                .multiply(ObjectUtils.defaultIfNull(pkwEinheit.getFahrradfahrer(), BigDecimal.valueOf(DEFAULT_VALUE_IF_NULL)));
    }

    /**
     * @param value1 Erster Wert
     * @param value2 Zweiter Wert
     * @return null falls beide Parameter null sind ansonsten die Summe. Ist nur ein Parameter null"
     *         wird dieser als Wert "0" interpretiert.
     */
    public static Integer nullSafeSummation(final Integer value1, final Integer value2) {
        final Integer sum;
        if (ObjectUtils.isEmpty(value1) && ObjectUtils.isEmpty(value2)) {
            sum = null;
        } else {
            sum = (ObjectUtils.isEmpty(value1) ? 0 : value1)
                    + (ObjectUtils.isEmpty(value2) ? 0 : value2);
        }
        return sum;
    }

    /**
     * @param value1 Erster Wert
     * @param value2 Zweiter Wert
     * @return null falls beide Parameter null sind ansonsten die Summe. Ist nur ein Parameter null"
     *         wird dieser als Wert "0" interpretiert.
     */
    public static BigDecimal nullSafeSummation(final BigDecimal value1, final BigDecimal value2) {
        final BigDecimal sum;
        if (ObjectUtils.isEmpty(value1) && ObjectUtils.isEmpty(value2)) {
            sum = null;
        } else {
            sum = (ObjectUtils.isEmpty(value1) ? BigDecimal.ZERO : value1)
                    .add(ObjectUtils.isEmpty(value2) ? BigDecimal.ZERO : value2);
        }
        return sum;
    }

}
