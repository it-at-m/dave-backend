package de.muenchen.dave.util.messstelle;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import de.muenchen.dave.domain.dtos.laden.messwerte.LadeMesswerteDTO;
import de.muenchen.dave.domain.enums.Zeitblock;
import de.muenchen.dave.geodateneai.gen.model.IntervalDto;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
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

        Assertions.assertThat(MesswerteBaseUtil.calculateAnteilProzent(null, 40))
                .isNotNull()
                .isEqualTo(0);

        Assertions.assertThat(MesswerteBaseUtil.calculateAnteilProzent(0, 40))
                .isNotNull()
                .isEqualTo(0);

        Assertions.assertThat(MesswerteBaseUtil.calculateAnteilProzent(1, 0))
                .isNotNull()
                .isEqualTo(0D);

        Assertions.assertThat(MesswerteBaseUtil.calculateAnteilProzent(1, null))
                .isNotNull()
                .isEqualTo(0D);
    }

    @Test
    void isDateRange() {
        List<LocalDate> zeitraum = new ArrayList<>();
        var result = MesswerteBaseUtil.isDateRange(zeitraum);
        Assertions.assertThat(result).isFalse();

        zeitraum.add(LocalDate.of(2020, 1, 1));
        result = MesswerteBaseUtil.isDateRange(zeitraum);
        Assertions.assertThat(result).isFalse();

        zeitraum.add(LocalDate.of(2020, 1, 1));
        result = MesswerteBaseUtil.isDateRange(zeitraum);
        Assertions.assertThat(result).isFalse();

        zeitraum.add(LocalDate.of(2020, 1, 1));
        result = MesswerteBaseUtil.isDateRange(zeitraum);
        Assertions.assertThat(result).isFalse();

        zeitraum.clear();
        zeitraum.add(LocalDate.of(2020, 1, 1));
        zeitraum.add(LocalDate.of(2024, 1, 1));
        result = MesswerteBaseUtil.isDateRange(zeitraum);
        Assertions.assertThat(result).isTrue();

        zeitraum.clear();
        zeitraum.add(LocalDate.of(2024, 1, 1));
        zeitraum.add(LocalDate.of(2022, 1, 1));
        result = MesswerteBaseUtil.isDateRange(zeitraum);
        Assertions.assertThat(result).isTrue();
    }

    @Test
    void sumIntervalsAndAdaptDatumUhrzeitVonAndBisAndReturnNewInterval() {
        var interval1 = new IntervalDto();
        var interval2 = new IntervalDto();
        var result = MesswerteBaseUtil.sumCountingValuesOfIntervalsAndReturnNewInterval(interval1, interval2);
        var expected = new IntervalDto();

        assertThat(result, is(expected));

        interval1 = new IntervalDto();
        interval1.setMqId(null);
        interval1.setTagesTyp(null);
        interval1.setDatumUhrzeitVon(LocalDateTime.now());
        interval1.setDatumUhrzeitBis(LocalDateTime.now().plusMinutes(30));
        interval1.setAnzahlLfw(BigDecimal.valueOf(3));
        interval1.setAnzahlKrad(BigDecimal.valueOf(4));
        interval1.setAnzahlLkw(BigDecimal.valueOf(5));
        interval1.setAnzahlBus(BigDecimal.valueOf(8));
        interval1.setAnzahlRad(BigDecimal.valueOf(10));
        interval1.setSummeAllePkw(BigDecimal.valueOf(11));
        interval1.setSummeLastzug(BigDecimal.valueOf(12));
        interval1.setSummeGueterverkehr(BigDecimal.valueOf(13));
        interval1.setSummeSchwerverkehr(BigDecimal.valueOf(14));
        interval1.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(15));

        interval2 = new IntervalDto();
        interval2.setMqId(99);
        interval2.setTagesTyp(IntervalDto.TagesTypEnum.DTV_W5);
        interval2.setDatumUhrzeitVon(interval1.getDatumUhrzeitVon().plusMinutes(2));
        interval2.setDatumUhrzeitBis(interval1.getDatumUhrzeitBis().plusMinutes(2));
        interval2.setAnzahlLfw(BigDecimal.valueOf(3));
        interval2.setAnzahlKrad(BigDecimal.valueOf(4));
        interval2.setAnzahlLkw(BigDecimal.valueOf(5));
        interval2.setAnzahlBus(BigDecimal.valueOf(8));
        interval2.setAnzahlRad(BigDecimal.valueOf(10));
        interval2.setSummeAllePkw(BigDecimal.valueOf(11));
        interval2.setSummeLastzug(BigDecimal.valueOf(12));
        interval2.setSummeGueterverkehr(BigDecimal.valueOf(13));
        interval2.setSummeSchwerverkehr(BigDecimal.valueOf(14));
        interval2.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(15));

        result = MesswerteBaseUtil.sumIntervalsAndAdaptDatumUhrzeitVonAndBisAndReturnNewInterval(interval1, interval2);
        expected = new IntervalDto();
        expected.setMqId(99);
        expected.setTagesTyp(IntervalDto.TagesTypEnum.DTV_W5);
        expected.setDatumUhrzeitVon(interval1.getDatumUhrzeitVon());
        expected.setDatumUhrzeitBis(interval2.getDatumUhrzeitBis());
        expected.setAnzahlLfw(BigDecimal.valueOf(6));
        expected.setAnzahlKrad(BigDecimal.valueOf(8));
        expected.setAnzahlLkw(BigDecimal.valueOf(10));
        expected.setAnzahlBus(BigDecimal.valueOf(16));
        expected.setAnzahlRad(BigDecimal.valueOf(20));
        expected.setSummeAllePkw(BigDecimal.valueOf(22));
        expected.setSummeLastzug(BigDecimal.valueOf(24));
        expected.setSummeGueterverkehr(BigDecimal.valueOf(26));
        expected.setSummeSchwerverkehr(BigDecimal.valueOf(28));
        expected.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(30));

        assertThat(result, is(expected));
    }

    @Test
    void sumCountingValuesOfIntervalsAndReturnNewInterval() {
        var interval1 = new IntervalDto();
        var interval2 = new IntervalDto();
        var result = MesswerteBaseUtil.sumCountingValuesOfIntervalsAndReturnNewInterval(interval1, interval2);
        var expected = new IntervalDto();

        assertThat(result, is(expected));

        interval1 = new IntervalDto();
        interval1.setAnzahlLfw(BigDecimal.valueOf(3));
        interval1.setAnzahlKrad(BigDecimal.valueOf(4));
        interval1.setAnzahlLkw(BigDecimal.valueOf(5));
        interval1.setAnzahlBus(BigDecimal.valueOf(8));
        interval1.setAnzahlRad(BigDecimal.valueOf(10));
        interval1.setSummeAllePkw(BigDecimal.valueOf(11));
        interval1.setSummeLastzug(BigDecimal.valueOf(12));
        interval1.setSummeGueterverkehr(BigDecimal.valueOf(13));
        interval1.setSummeSchwerverkehr(BigDecimal.valueOf(14));
        interval1.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(15));

        interval2 = new IntervalDto();
        interval2.setAnzahlLfw(BigDecimal.valueOf(3));
        interval2.setAnzahlKrad(BigDecimal.valueOf(4));
        interval2.setAnzahlLkw(BigDecimal.valueOf(5));
        interval2.setAnzahlBus(BigDecimal.valueOf(8));
        interval2.setAnzahlRad(BigDecimal.valueOf(10));
        interval2.setSummeAllePkw(BigDecimal.valueOf(11));
        interval2.setSummeLastzug(BigDecimal.valueOf(12));
        interval2.setSummeGueterverkehr(BigDecimal.valueOf(13));
        interval2.setSummeSchwerverkehr(BigDecimal.valueOf(14));
        interval2.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(15));

        result = MesswerteBaseUtil.sumCountingValuesOfIntervalsAndReturnNewInterval(interval1, interval2);
        expected = new IntervalDto();
        expected.setAnzahlLfw(BigDecimal.valueOf(6));
        expected.setAnzahlKrad(BigDecimal.valueOf(8));
        expected.setAnzahlLkw(BigDecimal.valueOf(10));
        expected.setAnzahlBus(BigDecimal.valueOf(16));
        expected.setAnzahlRad(BigDecimal.valueOf(20));
        expected.setSummeAllePkw(BigDecimal.valueOf(22));
        expected.setSummeLastzug(BigDecimal.valueOf(24));
        expected.setSummeGueterverkehr(BigDecimal.valueOf(26));
        expected.setSummeSchwerverkehr(BigDecimal.valueOf(28));
        expected.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(30));

        assertThat(result, is(expected));
    }

    @Test
    void sumValuesIfAnyNotNullOrReturnNullBigDecimal() {
        var value1 = BigDecimal.valueOf(1);
        var value2 = BigDecimal.valueOf(2);
        var value3 = BigDecimal.valueOf(3);
        var result = MesswerteBaseUtil.sumValuesIfAnyNotNullOrReturnNull(value1, value2, value3);
        assertThat(result, is(BigDecimal.valueOf(6)));

        value1 = BigDecimal.valueOf(1);
        value2 = null;
        value3 = null;
        result = MesswerteBaseUtil.sumValuesIfAnyNotNullOrReturnNull(value1, value2, value3);
        assertThat(result, is(BigDecimal.valueOf(1)));

        value1 = null;
        value2 = BigDecimal.valueOf(1);
        value3 = null;
        result = MesswerteBaseUtil.sumValuesIfAnyNotNullOrReturnNull(value1, value2, value3);
        assertThat(result, is(BigDecimal.valueOf(1)));

        value1 = null;
        value2 = null;
        value3 = BigDecimal.valueOf(1);
        result = MesswerteBaseUtil.sumValuesIfAnyNotNullOrReturnNull(value1, value2, value3);
        assertThat(result, is(BigDecimal.valueOf(1)));

        value1 = BigDecimal.valueOf(1);
        value2 = BigDecimal.valueOf(2);
        value3 = null;
        result = MesswerteBaseUtil.sumValuesIfAnyNotNullOrReturnNull(value1, value2, value3);
        assertThat(result, is(BigDecimal.valueOf(3)));

        value1 = null;
        value2 = BigDecimal.valueOf(1);
        value3 = BigDecimal.valueOf(2);
        result = MesswerteBaseUtil.sumValuesIfAnyNotNullOrReturnNull(value1, value2, value3);
        assertThat(result, is(BigDecimal.valueOf(3)));

        value1 = null;
        value2 = null;
        value3 = null;
        result = MesswerteBaseUtil.sumValuesIfAnyNotNullOrReturnNull(value1, value2, value3);
        assertThat(result, is(nullValue()));
    }

    @Test
    void getMin() {
        var result = MesswerteBaseUtil.getMin(
                LocalDateTime.of(2025, 10, 10, 10, 10, 0, 0),
                LocalDateTime.of(2025, 10, 10, 10, 10, 0, 1));
        assertThat(result, is(LocalDateTime.of(2025, 10, 10, 10, 10, 0, 0)));

        result = MesswerteBaseUtil.getMin(
                LocalDateTime.of(2025, 10, 10, 10, 10, 0, 1),
                LocalDateTime.of(2025, 10, 10, 10, 10, 0, 0));
        assertThat(result, is(LocalDateTime.of(2025, 10, 10, 10, 10, 0, 0)));

        result = MesswerteBaseUtil.getMin(
                LocalDateTime.of(2025, 10, 10, 10, 10, 0, 1),
                null);
        assertThat(result, is(LocalDateTime.of(2025, 10, 10, 10, 10, 0, 1)));

        result = MesswerteBaseUtil.getMin(
                null,
                LocalDateTime.of(2025, 10, 10, 10, 10, 0, 1));
        assertThat(result, is(LocalDateTime.of(2025, 10, 10, 10, 10, 0, 1)));

        result = MesswerteBaseUtil.getMin(
                null,
                null);
        assertThat(result, is(nullValue()));
    }

    @Test
    void getMax() {
        var result = MesswerteBaseUtil.getMax(
                LocalDateTime.of(2025, 10, 10, 10, 10, 0, 0),
                LocalDateTime.of(2025, 10, 10, 10, 10, 0, 1));
        assertThat(result, is(LocalDateTime.of(2025, 10, 10, 10, 10, 0, 1)));

        result = MesswerteBaseUtil.getMax(
                LocalDateTime.of(2025, 10, 10, 10, 10, 0, 1),
                LocalDateTime.of(2025, 10, 10, 10, 10, 0, 0));
        assertThat(result, is(LocalDateTime.of(2025, 10, 10, 10, 10, 0, 1)));

        result = MesswerteBaseUtil.getMax(
                LocalDateTime.of(2025, 10, 10, 10, 10, 0, 1),
                null);
        assertThat(result, is(LocalDateTime.of(2025, 10, 10, 10, 10, 0, 1)));

        result = MesswerteBaseUtil.getMax(
                null,
                LocalDateTime.of(2025, 10, 10, 10, 10, 0, 1));
        assertThat(result, is(LocalDateTime.of(2025, 10, 10, 10, 10, 0, 1)));

        result = MesswerteBaseUtil.getMax(
                null,
                null);
        assertThat(result, is(nullValue()));
    }

}
