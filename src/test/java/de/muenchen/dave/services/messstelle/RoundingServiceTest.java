/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2023
 */
package de.muenchen.dave.services.messstelle;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import de.muenchen.dave.domain.dtos.laden.messwerte.LadeMesswerteDTO;
import java.math.BigDecimal;
import java.time.LocalTime;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
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

    @Test
    void roundToNearest() {
        final LocalTime defaultTime = LocalTime.of(0, 0);
        final LadeMesswerteDTO dto = new LadeMesswerteDTO();
        dto.setSortingIndex(1);
        dto.setType("Type");
        dto.setStartUhrzeit(defaultTime);
        dto.setEndeUhrzeit(defaultTime);
        dto.setAnteilSchwerverkehrAnKfzProzent(2.0);
        dto.setAnteilGueterverkehrAnKfzProzent(2.0);

        dto.setPkw(111);
        dto.setLkw(89);
        dto.setLfw(49);
        dto.setLastzuege(150);
        dto.setBusse(null);
        dto.setKraftraeder(100);
        dto.setFahrradfahrer(20);
        dto.setFussgaenger(15);
        dto.setKfz(951);
        dto.setSchwerverkehr(222);
        dto.setGueterverkehr(351);

        final LadeMesswerteDTO expected = new LadeMesswerteDTO();
        expected.setSortingIndex(1);
        expected.setType("Type");
        expected.setStartUhrzeit(defaultTime);
        expected.setEndeUhrzeit(defaultTime);
        expected.setAnteilSchwerverkehrAnKfzProzent(2.0);
        expected.setAnteilGueterverkehrAnKfzProzent(2.0);

        expected.setPkw(100);
        expected.setLkw(100);
        expected.setLfw(0);
        expected.setLastzuege(200);
        expected.setBusse(null);
        expected.setKraftraeder(100);
        expected.setFahrradfahrer(0);
        expected.setFussgaenger(0);
        expected.setKfz(1000);
        expected.setSchwerverkehr(200);
        expected.setGueterverkehr(400);

        serviceToTest.roundToNearest(dto, 100);

        Assertions.assertThat(dto)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }
}
