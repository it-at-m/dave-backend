package de.muenchen.dave.util;

import de.muenchen.dave.domain.dtos.laden.LadeZaehldatumDTO;
import de.muenchen.dave.domain.elasticsearch.PkwEinheit;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


class CalculationUtilTest {

    @Test
    public void calculateAnteilSchwerverkehrAnKfzProzent() {
        final LadeZaehldatumDTO zaehldatumDTO = new LadeZaehldatumDTO();
        zaehldatumDTO.setPkw(680);
        zaehldatumDTO.setLkw(7);
        zaehldatumDTO.setLastzuege(7);
        zaehldatumDTO.setBusse(13);
        zaehldatumDTO.setKraftraeder(15);
        zaehldatumDTO.setFahrradfahrer(114);
        zaehldatumDTO.setFussgaenger(0);

        BigDecimal result = CalculationUtil.calculateAnteilSchwerverkehrAnKfzProzent(zaehldatumDTO);
        BigDecimal expectedResult = BigDecimal.valueOf(3.7);
        assertThat(result, is(expectedResult));
        assertThat(zaehldatumDTO.getAnteilSchwerverkehrAnKfzProzent(), is(expectedResult));
    }

    @Test
    public void calculateAnteilGueterverkehrAnKfzProzent() {
        final LadeZaehldatumDTO zaehldatumDTO = new LadeZaehldatumDTO();
        zaehldatumDTO.setPkw(680);
        zaehldatumDTO.setLkw(7);
        zaehldatumDTO.setLastzuege(7);
        zaehldatumDTO.setBusse(13);
        zaehldatumDTO.setKraftraeder(15);
        zaehldatumDTO.setFahrradfahrer(114);
        zaehldatumDTO.setFussgaenger(0);

        BigDecimal result = CalculationUtil.calculateAnteilGueterverkehrAnKfzProzent(zaehldatumDTO);
        BigDecimal expectedResult = BigDecimal.valueOf(1.9);
        assertThat(result, is(expectedResult));
        assertThat(zaehldatumDTO.getAnteilGueterverkehrAnKfzProzent(), is(expectedResult));
    }

    @Test
    public void calculateAnteilSchwerverkehrAnKfzProzentNull() {
        final LadeZaehldatumDTO zaehldatumDTO = new LadeZaehldatumDTO();
        zaehldatumDTO.setFahrradfahrer(114);
        zaehldatumDTO.setFussgaenger(0);

        BigDecimal result = CalculationUtil.calculateAnteilSchwerverkehrAnKfzProzent(zaehldatumDTO);
        BigDecimal expectedResult = BigDecimal.ZERO;
        assertThat(result, is(expectedResult));
        assertThat(zaehldatumDTO.getAnteilSchwerverkehrAnKfzProzent(), is(expectedResult));
    }

    @Test
    public void calculateAnteilGueterverkehrAnKfzProzentNull() {
        final LadeZaehldatumDTO zaehldatumDTO = new LadeZaehldatumDTO();
        zaehldatumDTO.setFahrradfahrer(114);
        zaehldatumDTO.setFussgaenger(0);

        BigDecimal result = CalculationUtil.calculateAnteilGueterverkehrAnKfzProzent(zaehldatumDTO);
        BigDecimal expectedResult = BigDecimal.ZERO;
        assertThat(result, is(expectedResult));
        assertThat(zaehldatumDTO.getAnteilGueterverkehrAnKfzProzent(), is(expectedResult));
    }

    @Test
    public void getGueterverkehr() {
        final LadeZaehldatumDTO zaehldatumDTO = new LadeZaehldatumDTO();
        zaehldatumDTO.setPkw(1);
        zaehldatumDTO.setLkw(2);
        zaehldatumDTO.setLastzuege(3);
        zaehldatumDTO.setBusse(4);
        zaehldatumDTO.setKraftraeder(5);
        zaehldatumDTO.setFahrradfahrer(6);
        zaehldatumDTO.setFussgaenger(7);

        BigDecimal expectedResult = BigDecimal.valueOf(5);
        assertThat(CalculationUtil.getGueterverkehr(zaehldatumDTO), is(expectedResult));
    }

    @Test
    public void getSchwerverkehr() {
        final LadeZaehldatumDTO zaehldatumDTO = new LadeZaehldatumDTO();
        zaehldatumDTO.setPkw(1);
        zaehldatumDTO.setLkw(2);
        zaehldatumDTO.setLastzuege(3);
        zaehldatumDTO.setBusse(4);
        zaehldatumDTO.setKraftraeder(5);
        zaehldatumDTO.setFahrradfahrer(6);
        zaehldatumDTO.setFussgaenger(7);

        BigDecimal expectedResult = BigDecimal.valueOf(9);
        assertThat(CalculationUtil.getSchwerverkehr(zaehldatumDTO), is(expectedResult));
    }

    @Test
    public void getKfz() {
        final LadeZaehldatumDTO zaehldatumDTO = new LadeZaehldatumDTO();
        zaehldatumDTO.setPkw(1);
        zaehldatumDTO.setLkw(2);
        zaehldatumDTO.setLastzuege(3);
        zaehldatumDTO.setBusse(4);
        zaehldatumDTO.setKraftraeder(5);
        zaehldatumDTO.setFahrradfahrer(6);
        zaehldatumDTO.setFussgaenger(7);

        BigDecimal expectedResult = BigDecimal.valueOf(15);
        assertThat(CalculationUtil.getKfz(zaehldatumDTO), is(expectedResult));
    }

    @Test
    public void getGesamt() {
        final LadeZaehldatumDTO zaehldatumDTO = new LadeZaehldatumDTO();
        zaehldatumDTO.setPkw(1);
        zaehldatumDTO.setLkw(2);
        zaehldatumDTO.setLastzuege(3);
        zaehldatumDTO.setBusse(4);
        zaehldatumDTO.setKraftraeder(5);
        zaehldatumDTO.setFahrradfahrer(6);
        zaehldatumDTO.setFussgaenger(7);

        BigDecimal expectedResult = BigDecimal.valueOf(28);
        assertThat(CalculationUtil.getGesamt(zaehldatumDTO), is(expectedResult));
    }

    @Test
    public void nullCheck() {
        final LadeZaehldatumDTO zaehldatumDTO = new LadeZaehldatumDTO();

        BigDecimal expectedResult = BigDecimal.valueOf(0);
        assertThat(CalculationUtil.getGesamt(zaehldatumDTO), is(expectedResult));
    }

    @Test
    public void calculatePkwEinheitenOberflow() {
        final PkwEinheit pkwEinheit = new PkwEinheit();
        pkwEinheit.setPkw(BigDecimal.valueOf(Integer.MAX_VALUE));
        pkwEinheit.setLkw(BigDecimal.valueOf(Integer.MAX_VALUE));
        pkwEinheit.setLastzuege(BigDecimal.valueOf(Integer.MAX_VALUE));
        pkwEinheit.setBusse(BigDecimal.valueOf(Integer.MAX_VALUE));
        pkwEinheit.setKraftraeder(BigDecimal.valueOf(Integer.MAX_VALUE));
        pkwEinheit.setFahrradfahrer(BigDecimal.valueOf(Integer.MAX_VALUE));

        LadeZaehldatumDTO ladeZaehldatumDTO = new LadeZaehldatumDTO();
        ladeZaehldatumDTO.setPkw(Integer.MAX_VALUE);
        ladeZaehldatumDTO.setLkw(Integer.MAX_VALUE);
        ladeZaehldatumDTO.setLastzuege(Integer.MAX_VALUE);
        ladeZaehldatumDTO.setBusse(Integer.MAX_VALUE);
        ladeZaehldatumDTO.setKraftraeder(Integer.MAX_VALUE);
        ladeZaehldatumDTO.setFahrradfahrer(Integer.MAX_VALUE);
        ladeZaehldatumDTO.setFussgaenger(Integer.MAX_VALUE);

        Integer result = CalculationUtil.calculatePkwEinheiten(ladeZaehldatumDTO, pkwEinheit);
        Integer expectedResult = Integer.MAX_VALUE;
        assertThat(result, is(expectedResult));

    }

    @Test
    public void calculatePkwEinheiten() {
        final PkwEinheit pkwEinheit = new PkwEinheit();
        pkwEinheit.setPkw(BigDecimal.valueOf(1));
        pkwEinheit.setLkw(BigDecimal.valueOf(2));
        pkwEinheit.setLastzuege(BigDecimal.valueOf(3));
        pkwEinheit.setBusse(BigDecimal.valueOf(4));
        pkwEinheit.setKraftraeder(BigDecimal.valueOf(5));
        pkwEinheit.setFahrradfahrer(BigDecimal.valueOf(6));

        LadeZaehldatumDTO ladeZaehldatumDTO = new LadeZaehldatumDTO();
        ladeZaehldatumDTO.setPkw(1);
        ladeZaehldatumDTO.setLkw(1);
        ladeZaehldatumDTO.setLastzuege(1);
        ladeZaehldatumDTO.setBusse(1);
        ladeZaehldatumDTO.setKraftraeder(1);
        ladeZaehldatumDTO.setFahrradfahrer(1);
        ladeZaehldatumDTO.setFussgaenger(1);

        Integer result = CalculationUtil.calculatePkwEinheiten(ladeZaehldatumDTO, pkwEinheit);
        Integer expectedResult = 21;
        assertThat(result, is(expectedResult));

        ladeZaehldatumDTO.setFussgaenger(null);
        result = CalculationUtil.calculatePkwEinheiten(ladeZaehldatumDTO, pkwEinheit);
        expectedResult = 21;
        assertThat(result, is(expectedResult));

        ladeZaehldatumDTO.setPkw(null);
        result = CalculationUtil.calculatePkwEinheiten(ladeZaehldatumDTO, pkwEinheit);
        expectedResult = 20;
        assertThat(result, is(expectedResult));

        ladeZaehldatumDTO.setPkw(1);
        ladeZaehldatumDTO.setLkw(null);
        result = CalculationUtil.calculatePkwEinheiten(ladeZaehldatumDTO, pkwEinheit);
        expectedResult = 19;
        assertThat(result, is(expectedResult));

        ladeZaehldatumDTO.setLkw(1);
        ladeZaehldatumDTO.setLastzuege(null);
        result = CalculationUtil.calculatePkwEinheiten(ladeZaehldatumDTO, pkwEinheit);
        expectedResult = 18;
        assertThat(result, is(expectedResult));

        ladeZaehldatumDTO.setLastzuege(1);
        ladeZaehldatumDTO.setBusse(null);
        result = CalculationUtil.calculatePkwEinheiten(ladeZaehldatumDTO, pkwEinheit);
        expectedResult = 17;
        assertThat(result, is(expectedResult));

        ladeZaehldatumDTO.setBusse(1);
        ladeZaehldatumDTO.setKraftraeder(null);
        result = CalculationUtil.calculatePkwEinheiten(ladeZaehldatumDTO, pkwEinheit);
        expectedResult = 16;
        assertThat(result, is(expectedResult));

        ladeZaehldatumDTO.setKraftraeder(1);
        ladeZaehldatumDTO.setFahrradfahrer(null);
        result = CalculationUtil.calculatePkwEinheiten(ladeZaehldatumDTO, pkwEinheit);
        expectedResult = 15;
        assertThat(result, is(expectedResult));
    }

    @Test
    public void nullSafeSummation() {
        Integer value1 = null;
        Integer value2 = null;
        Integer result = CalculationUtil.nullSafeSummation(value1, value2);
        assertThat(result, IsNull.nullValue());

        value1 = null;
        value2 = 2;
        result = CalculationUtil.nullSafeSummation(value1, value2);
        assertThat(result, is(2));

        value1 = 3;
        value2 = null;
        result = CalculationUtil.nullSafeSummation(value1, value2);
        assertThat(result, is(3));

        value1 = 3;
        value2 = 2;
        result = CalculationUtil.nullSafeSummation(value1, value2);
        assertThat(result, is(5));

        BigDecimal value3 = null;
        BigDecimal value4 = null;
        BigDecimal result2 = CalculationUtil.nullSafeSummation(value3, value4);
        assertThat(result2, IsNull.nullValue());

        value3 = null;
        value4 = BigDecimal.valueOf(2);
        result2 = CalculationUtil.nullSafeSummation(value3, value4);
        assertThat(result2, is(BigDecimal.valueOf(2)));

        value3 = BigDecimal.valueOf(3);
        value4 = null;
        result2 = CalculationUtil.nullSafeSummation(value3, value4);
        assertThat(result2, is(BigDecimal.valueOf(3)));

        value3 = BigDecimal.valueOf(3);
        value4 = BigDecimal.valueOf(2);
        result2 = CalculationUtil.nullSafeSummation(value3, value4);
        assertThat(result2, is(BigDecimal.valueOf(5)));
    }

}