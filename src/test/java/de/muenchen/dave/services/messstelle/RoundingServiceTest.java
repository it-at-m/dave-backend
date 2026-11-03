package de.muenchen.dave.services.messstelle;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.math.BigDecimal;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class RoundingServiceTest {

    private final RoundingService serviceToTest = new RoundingService();

    @Test
    void roundIfNotNullOrZero() {
        final int nearestValueToRound = 100;
        int valueToRoundInt = 49;
        Integer resultInt = serviceToTest.roundIfNotNullOrZero(valueToRoundInt, nearestValueToRound);
        assertThat(resultInt, is(0));

        valueToRoundInt = 50;
        resultInt = serviceToTest.roundIfNotNullOrZero(valueToRoundInt, nearestValueToRound);
        assertThat(resultInt, is(100));

        valueToRoundInt = 149;
        resultInt = serviceToTest.roundIfNotNullOrZero(valueToRoundInt, nearestValueToRound);
        assertThat(resultInt, is(100));

        valueToRoundInt = 150;
        resultInt = serviceToTest.roundIfNotNullOrZero(valueToRoundInt, nearestValueToRound);
        assertThat(resultInt, is(200));

        BigDecimal valueToRoundBd = BigDecimal.valueOf(49);
        BigDecimal resultBd = serviceToTest.roundIfNotNullOrZero(valueToRoundBd, nearestValueToRound);
        assertThat(resultBd, is(BigDecimal.ZERO));

        valueToRoundBd = BigDecimal.valueOf(50);
        resultBd = serviceToTest.roundIfNotNullOrZero(valueToRoundBd, nearestValueToRound);
        assertThat(resultBd, is(BigDecimal.valueOf(100)));

        valueToRoundBd = BigDecimal.valueOf(149);
        resultBd = serviceToTest.roundIfNotNullOrZero(valueToRoundBd, nearestValueToRound);
        assertThat(resultBd, is(BigDecimal.valueOf(100)));

        valueToRoundBd = BigDecimal.valueOf(150);
        resultBd = serviceToTest.roundIfNotNullOrZero(valueToRoundBd, nearestValueToRound);
        assertThat(resultBd, is(BigDecimal.valueOf(200)));
    }
}
