/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2023
 */
package de.muenchen.dave.util.messstelle;

import de.muenchen.dave.domain.dtos.laden.messwerte.LadeMesswerteDTO;
import de.muenchen.dave.domain.enums.Zeitblock;
import de.muenchen.dave.geodateneai.gen.model.IntervalDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class MesswerteBaseUtilTest {

    @Test
    void isIntervalWithingZeitblock() {
        var interval = new IntervalDto();
        interval.setDatumUhrzeitVon(LocalDateTime.of(2024, 1, 5, 10, 0, 0));
        interval.setDatumUhrzeitBis(LocalDateTime.of(2024, 1, 5, 10, 15, 0));
        var result = MesswerteBaseUtil.isIntervalWithingZeitblock(interval, Zeitblock.ZB_10_11);
        Assertions.assertThat(result).isTrue();

        interval.setDatumUhrzeitVon(LocalDateTime.of(2024, 1, 5, 10, 15, 0));
        interval.setDatumUhrzeitBis(LocalDateTime.of(2024, 1, 5, 10, 30, 0));
        result = MesswerteBaseUtil.isIntervalWithingZeitblock(interval, Zeitblock.ZB_10_11);
        Assertions.assertThat(result).isTrue();

        interval.setDatumUhrzeitVon(LocalDateTime.of(2024, 1, 5, 10, 45, 0));
        interval.setDatumUhrzeitBis(LocalDateTime.of(2024, 1, 5, 11, 0, 0));
        result = MesswerteBaseUtil.isIntervalWithingZeitblock(interval, Zeitblock.ZB_10_11);
        Assertions.assertThat(result).isTrue();

        interval.setDatumUhrzeitVon(LocalDateTime.of(2024, 1, 5, 9, 45, 0));
        interval.setDatumUhrzeitBis(LocalDateTime.of(2024, 1, 5, 10, 0, 0));
        result = MesswerteBaseUtil.isIntervalWithingZeitblock(interval, Zeitblock.ZB_10_11);
        Assertions.assertThat(result).isFalse();

        interval.setDatumUhrzeitVon(LocalDateTime.of(2024, 1, 5, 11, 0, 0));
        interval.setDatumUhrzeitBis(LocalDateTime.of(2024, 1, 5, 11, 15, 0));
        result = MesswerteBaseUtil.isIntervalWithingZeitblock(interval, Zeitblock.ZB_10_11);
        Assertions.assertThat(result).isFalse();

        interval.setDatumUhrzeitVon(LocalDateTime.of(2024, 1, 5, 11, 15, 0));
        interval.setDatumUhrzeitBis(LocalDateTime.of(2024, 1, 5, 11, 0, 0));
        result = MesswerteBaseUtil.isIntervalWithingZeitblock(interval, Zeitblock.ZB_10_11);
        Assertions.assertThat(result).isFalse();

        interval.setDatumUhrzeitVon(LocalDateTime.of(2024, 1, 5, 9, 45, 0));
        interval.setDatumUhrzeitBis(LocalDateTime.of(2024, 1, 5, 10, 15, 0));
        result = MesswerteBaseUtil.isIntervalWithingZeitblock(interval, Zeitblock.ZB_10_11);
        Assertions.assertThat(result).isFalse();

        interval.setDatumUhrzeitVon(LocalDateTime.of(2024, 1, 5, 10, 45, 0));
        interval.setDatumUhrzeitBis(LocalDateTime.of(2024, 1, 5, 11, 15, 0));
        result = MesswerteBaseUtil.isIntervalWithingZeitblock(interval, Zeitblock.ZB_10_11);
        Assertions.assertThat(result).isFalse();
    }

    @Test
    void isTimeWithinZeitblock() {
        var result = MesswerteBaseUtil.isTimeWithinZeitblock(LocalTime.of(10, 0, 0), Zeitblock.ZB_10_11);
        Assertions.assertThat(result).isTrue();

        result = MesswerteBaseUtil.isTimeWithinZeitblock(LocalTime.of(10, 15, 0), Zeitblock.ZB_10_11);
        Assertions.assertThat(result).isTrue();

        result = MesswerteBaseUtil.isTimeWithinZeitblock(LocalTime.of(9, 59, 59, 999999999), Zeitblock.ZB_10_11);
        Assertions.assertThat(result).isFalse();

        result = MesswerteBaseUtil.isTimeWithinZeitblock(LocalTime.of(11, 0, 0), Zeitblock.ZB_10_11);
        Assertions.assertThat(result).isTrue();

        result = MesswerteBaseUtil.isTimeWithinZeitblock(LocalTime.of(11, 0, 0, 1), Zeitblock.ZB_10_11);
        Assertions.assertThat(result).isFalse();
    }

    @Test
    void isIntervalWithinStartAndEnd() {
        var interval = new IntervalDto();
        interval.setDatumUhrzeitVon(LocalDateTime.of(2024, 1, 5, 10, 0, 0));
        interval.setDatumUhrzeitBis(LocalDateTime.of(2024, 1, 5, 10, 15, 0));
        var result = MesswerteBaseUtil.isIntervalWithinStartAndEnd(
                interval,
                LocalTime.of(10, 0, 0),
                LocalTime.of(11, 0, 0));
        Assertions.assertThat(result).isTrue();

        interval.setDatumUhrzeitVon(LocalDateTime.of(2024, 1, 5, 10, 15, 0));
        interval.setDatumUhrzeitBis(LocalDateTime.of(2024, 1, 5, 10, 30, 0));
        result = MesswerteBaseUtil.isIntervalWithinStartAndEnd(
                interval,
                LocalTime.of(10, 0, 0),
                LocalTime.of(11, 0, 0));
        Assertions.assertThat(result).isTrue();

        interval.setDatumUhrzeitVon(LocalDateTime.of(2024, 1, 5, 10, 45, 0));
        interval.setDatumUhrzeitBis(LocalDateTime.of(2024, 1, 5, 11, 0, 0));
        result = MesswerteBaseUtil.isIntervalWithinStartAndEnd(
                interval,
                LocalTime.of(10, 0, 0),
                LocalTime.of(11, 0, 0));
        Assertions.assertThat(result).isTrue();

        interval.setDatumUhrzeitVon(LocalDateTime.of(2024, 1, 5, 9, 45, 0));
        interval.setDatumUhrzeitBis(LocalDateTime.of(2024, 1, 5, 10, 0, 0));
        result = MesswerteBaseUtil.isIntervalWithinStartAndEnd(
                interval,
                LocalTime.of(10, 0, 0),
                LocalTime.of(11, 0, 0));
        Assertions.assertThat(result).isFalse();

        interval.setDatumUhrzeitVon(LocalDateTime.of(2024, 1, 5, 11, 0, 0));
        interval.setDatumUhrzeitBis(LocalDateTime.of(2024, 1, 5, 11, 15, 0));
        result = MesswerteBaseUtil.isIntervalWithinStartAndEnd(
                interval,
                LocalTime.of(10, 0, 0),
                LocalTime.of(11, 0, 0));
        Assertions.assertThat(result).isFalse();

        interval.setDatumUhrzeitVon(LocalDateTime.of(2024, 1, 5, 11, 15, 0));
        interval.setDatumUhrzeitBis(LocalDateTime.of(2024, 1, 5, 11, 0, 0));
        result = MesswerteBaseUtil.isIntervalWithinStartAndEnd(
                interval,
                LocalTime.of(10, 0, 0),
                LocalTime.of(11, 0, 0));
        Assertions.assertThat(result).isFalse();

        interval.setDatumUhrzeitVon(LocalDateTime.of(2024, 1, 5, 9, 45, 0));
        interval.setDatumUhrzeitBis(LocalDateTime.of(2024, 1, 5, 10, 15, 0));
        result = MesswerteBaseUtil.isIntervalWithinStartAndEnd(
                interval,
                LocalTime.of(10, 0, 0),
                LocalTime.of(11, 0, 0));
        Assertions.assertThat(result).isFalse();

        interval.setDatumUhrzeitVon(LocalDateTime.of(2024, 1, 5, 10, 45, 0));
        interval.setDatumUhrzeitBis(LocalDateTime.of(2024, 1, 5, 11, 15, 0));
        result = MesswerteBaseUtil.isIntervalWithinStartAndEnd(
                interval,
                LocalTime.of(10, 0, 0),
                LocalTime.of(11, 0, 0));
        Assertions.assertThat(result).isFalse();
    }

    @Test
    void isTimeWithinStartAndEnd() {
        var result = MesswerteBaseUtil.isTimeWithinStartAndEnd(
                LocalTime.of(10, 0, 0),
                LocalTime.of(10, 0, 0),
                LocalTime.of(11, 0, 0));
        Assertions.assertThat(result).isTrue();

        result = MesswerteBaseUtil.isTimeWithinStartAndEnd(
                LocalTime.of(10, 15, 0),
                LocalTime.of(10, 0, 0),
                LocalTime.of(11, 0, 0));
        Assertions.assertThat(result).isTrue();

        result = MesswerteBaseUtil.isTimeWithinStartAndEnd(
                LocalTime.of(9, 59, 59, 999999999),
                LocalTime.of(10, 0, 0),
                LocalTime.of(11, 0, 0));
        Assertions.assertThat(result).isFalse();

        result = MesswerteBaseUtil.isTimeWithinStartAndEnd(
                LocalTime.of(11, 0, 0),
                LocalTime.of(10, 0, 0),
                LocalTime.of(11, 0, 0));
        Assertions.assertThat(result).isTrue();

        result = MesswerteBaseUtil.isTimeWithinStartAndEnd(
                LocalTime.of(11, 0, 0, 1),
                LocalTime.of(10, 0, 0),
                LocalTime.of(11, 0, 0));
        Assertions.assertThat(result).isFalse();
    }

    @Test
    void calculateSum() {
        final IntervalDto interval1 = new IntervalDto();
        interval1.setAnzahlLfw(BigDecimal.valueOf(1));
        interval1.setAnzahlKrad(BigDecimal.valueOf(2));
        interval1.setAnzahlLkw(BigDecimal.valueOf(3));
        interval1.setAnzahlBus(BigDecimal.valueOf(4));
        interval1.setAnzahlRad(BigDecimal.valueOf(5));
        interval1.setSummeAllePkw(BigDecimal.valueOf(6));
        interval1.setSummeLastzug(BigDecimal.valueOf(7));
        interval1.setSummeGueterverkehr(BigDecimal.valueOf(8));
        interval1.setSummeSchwerverkehr(BigDecimal.valueOf(9));
        interval1.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(10));
        interval1.setProzentSchwerverkehr(BigDecimal.valueOf(1.1D));
        interval1.setProzentGueterverkehr(BigDecimal.valueOf(2.2D));

        final IntervalDto interval2 = new IntervalDto();
        interval2.setAnzahlLfw(BigDecimal.valueOf(1));
        interval2.setAnzahlKrad(BigDecimal.valueOf(2));
        interval2.setAnzahlLkw(BigDecimal.valueOf(3));
        interval2.setAnzahlBus(BigDecimal.valueOf(4));
        interval2.setAnzahlRad(BigDecimal.valueOf(5));
        interval2.setSummeAllePkw(BigDecimal.valueOf(6));
        interval2.setSummeLastzug(BigDecimal.valueOf(7));
        interval2.setSummeGueterverkehr(BigDecimal.valueOf(8));
        interval2.setSummeSchwerverkehr(BigDecimal.valueOf(9));
        interval2.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(10));
        interval2.setProzentSchwerverkehr(BigDecimal.valueOf(1.1D));
        interval2.setProzentGueterverkehr(BigDecimal.valueOf(2.2D));

        final LadeMesswerteDTO expected = new LadeMesswerteDTO();
        expected.setPkw(interval1.getSummeAllePkw().intValue() + interval2.getSummeAllePkw().intValue());
        expected.setLkw(interval1.getAnzahlLkw().intValue() + interval2.getAnzahlLkw().intValue());
        expected.setLfw(interval1.getAnzahlLfw().intValue() + interval2.getAnzahlLfw().intValue());
        expected.setLastzuege(interval1.getSummeLastzug().intValue() + interval2.getSummeLastzug().intValue());
        expected.setBusse(interval1.getAnzahlBus().intValue() + interval2.getAnzahlBus().intValue());
        expected.setKraftraeder(interval1.getAnzahlKrad().intValue() + interval2.getAnzahlKrad().intValue());
        expected.setFahrradfahrer(interval1.getAnzahlRad().intValue() + interval2.getAnzahlRad().intValue());
        expected.setKfz(interval1.getSummeKraftfahrzeugverkehr().intValue() + interval2.getSummeKraftfahrzeugverkehr().intValue());
        expected.setSchwerverkehr(interval1.getSummeSchwerverkehr().intValue() + interval2.getSummeSchwerverkehr().intValue());
        expected.setGueterverkehr(interval1.getSummeGueterverkehr().intValue() + interval2.getSummeGueterverkehr().intValue());
        expected.setAnteilSchwerverkehrAnKfzProzent(MesswerteBaseUtil.calculateAnteilProzent(expected.getSchwerverkehr(), expected.getKfz()));
        expected.setAnteilGueterverkehrAnKfzProzent(MesswerteBaseUtil.calculateAnteilProzent(expected.getGueterverkehr(), expected.getKfz()));

        Assertions.assertThat(MesswerteBaseUtil.calculateSum(List.of(interval1, interval2)))
                .isNotNull()
                .usingRecursiveComparison()
                .ignoringFields("sortingIndex", "type", "startUhrzeit", "endeUhrzeit", "fussgaenger")
                .isEqualTo(expected);
    }

    @Test
    void calculateAnteilProzent() {
        Assertions.assertThat(MesswerteBaseUtil.calculateAnteilProzent(1, 40))
                .isNotNull()
                .isEqualTo(2.5D);
    }

}
