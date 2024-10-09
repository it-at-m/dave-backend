package de.muenchen.dave.services.messstelle;

import de.muenchen.dave.domain.dtos.laden.messwerte.LadeMesswerteDTO;
import de.muenchen.dave.domain.dtos.laden.messwerte.LadeMesswerteListenausgabeDTO;
import de.muenchen.dave.domain.dtos.messstelle.MessstelleOptionsDTO;
import de.muenchen.dave.domain.enums.ZaehldatenIntervall;
import de.muenchen.dave.domain.enums.Zeitblock;
import de.muenchen.dave.domain.mapper.LadeMesswerteMapperImpl;
import de.muenchen.dave.geodateneai.gen.model.IntervalDto;
import de.muenchen.dave.util.messstelle.MesswerteSortingIndexUtil;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ListenausgabeServiceTest {

    private ListenausgabeService listenausgabeService;

    @BeforeEach
    public void beforeEach() {
        listenausgabeService = new ListenausgabeService(new LadeMesswerteMapperImpl(), new SpitzenstundeService());
    }

    @Test
    void ladeListenausgabe() {
        int index = 0;

        final IntervalDto interval0 = new IntervalDto();
        interval0.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 0)));
        interval0.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 30)));
        interval0.setAnzahlLfw(BigDecimal.valueOf(1 + index));
        interval0.setAnzahlKrad(BigDecimal.valueOf(2 + index));
        interval0.setAnzahlLkw(BigDecimal.valueOf(3 + index));
        interval0.setAnzahlBus(BigDecimal.valueOf(4 + index));
        interval0.setAnzahlRad(BigDecimal.valueOf(5 + index));
        interval0.setSummeAllePkw(BigDecimal.valueOf(6 + index));
        interval0.setSummeLastzug(BigDecimal.valueOf(7 + index));
        interval0.setSummeGueterverkehr(BigDecimal.valueOf(8 + index));
        interval0.setSummeSchwerverkehr(BigDecimal.valueOf(9 + index));
        interval0.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(10 + index));

        final IntervalDto interval1 = new IntervalDto();
        index++;
        interval1.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 30)));
        interval1.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(1, 0)));
        interval1.setAnzahlLfw(BigDecimal.valueOf(1 + index));
        interval1.setAnzahlKrad(BigDecimal.valueOf(2 + index));
        interval1.setAnzahlLkw(BigDecimal.valueOf(3 + index));
        interval1.setAnzahlBus(BigDecimal.valueOf(4 + index));
        interval1.setAnzahlRad(BigDecimal.valueOf(5 + index));
        interval1.setSummeAllePkw(BigDecimal.valueOf(6 + index));
        interval1.setSummeLastzug(BigDecimal.valueOf(7 + index));
        interval1.setSummeGueterverkehr(BigDecimal.valueOf(8 + index));
        interval1.setSummeSchwerverkehr(BigDecimal.valueOf(9 + index));
        interval1.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(10 + index));

        final IntervalDto interval2 = new IntervalDto();
        index++;
        interval2.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(1, 0)));
        interval2.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(1, 30)));
        interval2.setAnzahlLfw(BigDecimal.valueOf(1 + index));
        interval2.setAnzahlKrad(BigDecimal.valueOf(2 + index));
        interval2.setAnzahlLkw(BigDecimal.valueOf(3 + index));
        interval2.setAnzahlBus(BigDecimal.valueOf(4 + index));
        interval2.setAnzahlRad(BigDecimal.valueOf(5 + index));
        interval2.setSummeAllePkw(BigDecimal.valueOf(6 + index));
        interval2.setSummeLastzug(BigDecimal.valueOf(7 + index));
        interval2.setSummeGueterverkehr(BigDecimal.valueOf(8 + index));
        interval2.setSummeSchwerverkehr(BigDecimal.valueOf(9 + index));
        interval2.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(10 + index));

        final IntervalDto interval3 = new IntervalDto();
        index++;
        interval3.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(1, 30)));
        interval3.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(2, 0)));
        interval3.setAnzahlLfw(BigDecimal.valueOf(1 + index));
        interval3.setAnzahlKrad(BigDecimal.valueOf(2 + index));
        interval3.setAnzahlLkw(BigDecimal.valueOf(3 + index));
        interval3.setAnzahlBus(BigDecimal.valueOf(4 + index));
        interval3.setAnzahlRad(BigDecimal.valueOf(5 + index));
        interval3.setSummeAllePkw(BigDecimal.valueOf(6 + index));
        interval3.setSummeLastzug(BigDecimal.valueOf(7 + index));
        interval3.setSummeGueterverkehr(BigDecimal.valueOf(8 + index));
        interval3.setSummeSchwerverkehr(BigDecimal.valueOf(9 + index));
        interval3.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(10 + index));

        final IntervalDto interval4 = new IntervalDto();
        index++;
        interval4.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(2, 0)));
        interval4.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(2, 30)));
        interval4.setAnzahlLfw(BigDecimal.valueOf(1 + index));
        interval4.setAnzahlKrad(BigDecimal.valueOf(2 + index));
        interval4.setAnzahlLkw(BigDecimal.valueOf(3 + index));
        interval4.setAnzahlBus(BigDecimal.valueOf(4 + index));
        interval4.setAnzahlRad(BigDecimal.valueOf(5 + index));
        interval4.setSummeAllePkw(BigDecimal.valueOf(6 + index));
        interval4.setSummeLastzug(BigDecimal.valueOf(7 + index));
        interval4.setSummeGueterverkehr(BigDecimal.valueOf(8 + index));
        interval4.setSummeSchwerverkehr(BigDecimal.valueOf(9 + index));
        interval4.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(10 + index));

        final IntervalDto interval5 = new IntervalDto();
        index++;
        interval5.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(2, 30)));
        interval5.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(3, 0)));
        interval5.setAnzahlLfw(BigDecimal.valueOf(1 + index));
        interval5.setAnzahlKrad(BigDecimal.valueOf(2 + index));
        interval5.setAnzahlLkw(BigDecimal.valueOf(3 + index));
        interval5.setAnzahlBus(BigDecimal.valueOf(4 + index));
        interval5.setAnzahlRad(BigDecimal.valueOf(5 + index));
        interval5.setSummeAllePkw(BigDecimal.valueOf(6 + index));
        interval5.setSummeLastzug(BigDecimal.valueOf(7 + index));
        interval5.setSummeGueterverkehr(BigDecimal.valueOf(8 + index));
        interval5.setSummeSchwerverkehr(BigDecimal.valueOf(9 + index));
        interval5.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(10 + index));

        final IntervalDto interval6 = new IntervalDto();
        interval6.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(3, 0)));
        interval6.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(3, 30)));
        interval6.setAnzahlLfw(BigDecimal.valueOf(0));
        interval6.setAnzahlKrad(BigDecimal.valueOf(0));
        interval6.setAnzahlLkw(BigDecimal.valueOf(0));
        interval6.setAnzahlBus(BigDecimal.valueOf(0));
        interval6.setAnzahlRad(BigDecimal.valueOf(0));
        interval6.setSummeAllePkw(BigDecimal.valueOf(0));
        interval6.setSummeLastzug(BigDecimal.valueOf(0));
        interval6.setSummeGueterverkehr(BigDecimal.valueOf(0));
        interval6.setSummeSchwerverkehr(BigDecimal.valueOf(0));
        interval6.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(0));

        final var intervals = List.of(interval0, interval1, interval2, interval3, interval4, interval5, interval6);
        final var options = new MessstelleOptionsDTO();
        options.setZeitauswahl("Spitzenstunde KFZ");
        options.setIntervall(ZaehldatenIntervall.STUNDE_HALB);
        final var result = listenausgabeService.ladeListenausgabe(intervals, true, options);

        final var expected = new LadeMesswerteListenausgabeDTO();

        Assertions.assertThat(result)
                .isNotNull()
                .isEqualTo(expected);
    }

    @Test
    void calculateSumOfIntervalsPerHour() {
        final var interval0 = new IntervalDto();
        interval0.setAnzahlLkw(BigDecimal.valueOf(1));
        interval0.setDatumUhrzeitVon(LocalDateTime.of(2024, 1, 5, 9, 45, 0));
        interval0.setDatumUhrzeitBis(LocalDateTime.of(2024, 1, 5, 10, 0, 0));
        final var interval1 = new IntervalDto();
        interval1.setAnzahlLkw(BigDecimal.valueOf(2));
        interval1.setDatumUhrzeitVon(LocalDateTime.of(2024, 1, 5, 10, 0, 0));
        interval1.setDatumUhrzeitBis(LocalDateTime.of(2024, 1, 5, 10, 15, 0));
        final var interval2 = new IntervalDto();
        interval2.setAnzahlLkw(BigDecimal.valueOf(3));
        interval2.setDatumUhrzeitVon(LocalDateTime.of(2024, 1, 5, 10, 15, 0));
        interval2.setDatumUhrzeitBis(LocalDateTime.of(2024, 1, 5, 10, 30, 0));
        final var interval3 = new IntervalDto();
        interval3.setAnzahlLkw(BigDecimal.valueOf(4));
        interval3.setDatumUhrzeitVon(LocalDateTime.of(2024, 1, 5, 10, 30, 0));
        interval3.setDatumUhrzeitBis(LocalDateTime.of(2024, 1, 5, 10, 45, 0));
        final var interval4 = new IntervalDto();
        interval4.setAnzahlLkw(BigDecimal.valueOf(5));
        interval4.setDatumUhrzeitVon(LocalDateTime.of(2024, 1, 5, 10, 45, 0));
        interval4.setDatumUhrzeitBis(LocalDateTime.of(2024, 1, 5, 11, 0, 0));
        final var interval5 = new IntervalDto();
        interval5.setAnzahlLkw(BigDecimal.valueOf(6));
        interval5.setDatumUhrzeitVon(LocalDateTime.of(2024, 1, 5, 11, 0, 0));
        interval5.setDatumUhrzeitBis(LocalDateTime.of(2024, 1, 5, 11, 15, 0));

        var result = listenausgabeService.calculateSumOfIntervalsPerHour(List.of(interval0, interval1, interval2, interval3, interval4, interval5));

        final var expected0 = new LadeMesswerteDTO();
        expected0.setType("Stunde");
        expected0.setStartUhrzeit(Zeitblock.ZB_09_10.getStart().toLocalTime());
        expected0.setEndeUhrzeit(Zeitblock.ZB_09_10.getEnd().toLocalTime());
        expected0.setSortingIndex(21040040);
        expected0.setPkw(0);
        expected0.setLkw(1);
        expected0.setLfw(0);
        expected0.setLastzuege(0);
        expected0.setBusse(0);
        expected0.setKraftraeder(0);
        expected0.setFahrradfahrer(0);
        expected0.setKfz(0);
        expected0.setSchwerverkehr(0);
        expected0.setGueterverkehr(0);
        expected0.setAnteilSchwerverkehrAnKfzProzent(0D);
        expected0.setAnteilGueterverkehrAnKfzProzent(0D);

        final var expected1 = new LadeMesswerteDTO();
        expected1.setType("Stunde");
        expected1.setStartUhrzeit(Zeitblock.ZB_10_11.getStart().toLocalTime());
        expected1.setEndeUhrzeit(Zeitblock.ZB_10_11.getEnd().toLocalTime());
        expected1.setSortingIndex(31044044);
        expected1.setPkw(0);
        expected1.setLkw(14);
        expected1.setLfw(0);
        expected1.setLastzuege(0);
        expected1.setBusse(0);
        expected1.setKraftraeder(0);
        expected1.setFahrradfahrer(0);
        expected1.setKfz(0);
        expected1.setSchwerverkehr(0);
        expected1.setGueterverkehr(0);
        expected1.setAnteilSchwerverkehrAnKfzProzent(0D);
        expected1.setAnteilGueterverkehrAnKfzProzent(0D);

        final var expected2 = new LadeMesswerteDTO();
        expected2.setType("Stunde");
        expected2.setStartUhrzeit(Zeitblock.ZB_11_12.getStart().toLocalTime());
        expected2.setEndeUhrzeit(Zeitblock.ZB_11_12.getEnd().toLocalTime());
        expected2.setSortingIndex(31048048);
        expected2.setPkw(0);
        expected2.setLkw(6);
        expected2.setLfw(0);
        expected2.setLastzuege(0);
        expected2.setBusse(0);
        expected2.setKraftraeder(0);
        expected2.setFahrradfahrer(0);
        expected2.setKfz(0);
        expected2.setSchwerverkehr(0);
        expected2.setGueterverkehr(0);
        expected2.setAnteilSchwerverkehrAnKfzProzent(0D);
        expected2.setAnteilGueterverkehrAnKfzProzent(0D);

        Assertions.assertThat(result)
                .isNotNull()
                .isNotEmpty()
                .isEqualTo(List.of(expected0, expected1, expected2));
    }

    @Test
    void calculateSumOfIntervalsAndAddBlockSpecificDataToResult() {
        final var interval0 = new IntervalDto();
        interval0.setAnzahlLkw(BigDecimal.valueOf(1));
        interval0.setDatumUhrzeitVon(LocalDateTime.of(2024, 1, 5, 9, 45, 0));
        interval0.setDatumUhrzeitBis(LocalDateTime.of(2024, 1, 5, 10, 0, 0));
        final var interval1 = new IntervalDto();
        interval1.setAnzahlLkw(BigDecimal.valueOf(2));
        interval1.setDatumUhrzeitVon(LocalDateTime.of(2024, 1, 5, 10, 0, 0));
        interval1.setDatumUhrzeitBis(LocalDateTime.of(2024, 1, 5, 10, 15, 0));
        final var interval2 = new IntervalDto();
        interval2.setAnzahlLkw(BigDecimal.valueOf(3));
        interval2.setDatumUhrzeitVon(LocalDateTime.of(2024, 1, 5, 10, 15, 0));
        interval2.setDatumUhrzeitBis(LocalDateTime.of(2024, 1, 5, 10, 30, 0));
        final var interval3 = new IntervalDto();
        interval3.setAnzahlLkw(BigDecimal.valueOf(4));
        interval3.setDatumUhrzeitVon(LocalDateTime.of(2024, 1, 5, 10, 30, 0));
        interval3.setDatumUhrzeitBis(LocalDateTime.of(2024, 1, 5, 10, 45, 0));
        final var interval4 = new IntervalDto();
        interval4.setAnzahlLkw(BigDecimal.valueOf(5));
        interval4.setDatumUhrzeitVon(LocalDateTime.of(2024, 1, 5, 10, 45, 0));
        interval4.setDatumUhrzeitBis(LocalDateTime.of(2024, 1, 5, 11, 0, 0));
        final var interval5 = new IntervalDto();
        interval5.setAnzahlLkw(BigDecimal.valueOf(6));
        interval5.setDatumUhrzeitVon(LocalDateTime.of(2024, 1, 5, 11, 0, 0));
        interval5.setDatumUhrzeitBis(LocalDateTime.of(2024, 1, 5, 11, 15, 0));

        var result = listenausgabeService.calculateSumOfIntervalsAndAddBlockSpecificDataToResult(
                List.of(interval0, interval1, interval2, interval3, interval4, interval5), Zeitblock.ZB_10_11);

        final var expected = new LadeMesswerteDTO();
        expected.setType("Block");
        expected.setStartUhrzeit(Zeitblock.ZB_10_11.getStart().toLocalTime());
        expected.setEndeUhrzeit(Zeitblock.ZB_10_11.getEnd().toLocalTime());
        expected.setSortingIndex(35000000);
        expected.setPkw(0);
        expected.setLkw(21);
        expected.setLfw(0);
        expected.setLastzuege(0);
        expected.setBusse(0);
        expected.setKraftraeder(0);
        expected.setFahrradfahrer(0);
        expected.setKfz(0);
        expected.setSchwerverkehr(0);
        expected.setGueterverkehr(0);
        expected.setAnteilSchwerverkehrAnKfzProzent(0D);
        expected.setAnteilGueterverkehrAnKfzProzent(0D);

        Assertions.assertThat(result)
                .isNotNull()
                .isEqualTo(expected);
    }

    @Test
    void calculateTagessumme() {
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

        final var options = new MessstelleOptionsDTO();
        options.setZeitblock(Zeitblock.ZB_00_24);

        final var result = listenausgabeService.calculateTagessumme(List.of(interval1, interval2), options);

        final var expected = new LadeMesswerteDTO();
        expected.setType("Gesamt");
        expected.setStartUhrzeit(Zeitblock.ZB_00_24.getStart().toLocalTime());
        expected.setEndeUhrzeit(Zeitblock.ZB_00_24.getEnd().toLocalTime());
        expected.setSortingIndex(MesswerteSortingIndexUtil.SORTING_INDEX_GESAMT_DAY);
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
        expected.setAnteilSchwerverkehrAnKfzProzent(90D);
        expected.setAnteilGueterverkehrAnKfzProzent(80D);

        Assertions.assertThat(result)
                .isNotNull()
                .isEqualTo(expected);
    }

    @Test
    void getIntervalsWithinZeitblock() {
        final var interval0 = new IntervalDto();
        interval0.setDatumUhrzeitVon(LocalDateTime.of(2024, 1, 5, 9, 45, 0));
        interval0.setDatumUhrzeitBis(LocalDateTime.of(2024, 1, 5, 10, 0, 0));
        final var interval1 = new IntervalDto();
        interval1.setDatumUhrzeitVon(LocalDateTime.of(2024, 1, 5, 10, 0, 0));
        interval1.setDatumUhrzeitBis(LocalDateTime.of(2024, 1, 5, 10, 15, 0));
        final var interval2 = new IntervalDto();
        interval2.setDatumUhrzeitVon(LocalDateTime.of(2024, 1, 5, 10, 15, 0));
        interval2.setDatumUhrzeitBis(LocalDateTime.of(2024, 1, 5, 10, 30, 0));
        final var interval3 = new IntervalDto();
        interval3.setDatumUhrzeitVon(LocalDateTime.of(2024, 1, 5, 10, 30, 0));
        interval3.setDatumUhrzeitBis(LocalDateTime.of(2024, 1, 5, 10, 45, 0));
        final var interval4 = new IntervalDto();
        interval4.setDatumUhrzeitVon(LocalDateTime.of(2024, 1, 5, 10, 45, 0));
        interval4.setDatumUhrzeitBis(LocalDateTime.of(2024, 1, 5, 11, 0, 0));
        final var interval5 = new IntervalDto();
        interval5.setDatumUhrzeitVon(LocalDateTime.of(2024, 1, 5, 11, 0, 0));
        interval5.setDatumUhrzeitBis(LocalDateTime.of(2024, 1, 5, 11, 15, 0));

        final var intervals = List.of(interval0, interval1, interval2, interval3, interval4, interval5);
        final var result = listenausgabeService.getIntervalsWithinZeitblock(intervals, Zeitblock.ZB_10_11);

        Assertions.assertThat(result)
                .isNotNull()
                .isNotEmpty()
                .isEqualTo(List.of(interval1, interval2, interval3, interval4));
    }

    @Test
    void getIntervalsWithinRange() {
        final var interval0 = new IntervalDto();
        interval0.setDatumUhrzeitVon(LocalDateTime.of(2024, 1, 5, 9, 45, 0));
        interval0.setDatumUhrzeitBis(LocalDateTime.of(2024, 1, 5, 10, 0, 0));
        final var interval1 = new IntervalDto();
        interval1.setDatumUhrzeitVon(LocalDateTime.of(2024, 1, 5, 10, 0, 0));
        interval1.setDatumUhrzeitBis(LocalDateTime.of(2024, 1, 5, 10, 15, 0));
        final var interval2 = new IntervalDto();
        interval2.setDatumUhrzeitVon(LocalDateTime.of(2024, 1, 5, 10, 15, 0));
        interval2.setDatumUhrzeitBis(LocalDateTime.of(2024, 1, 5, 10, 30, 0));
        final var interval3 = new IntervalDto();
        interval3.setDatumUhrzeitVon(LocalDateTime.of(2024, 1, 5, 10, 30, 0));
        interval3.setDatumUhrzeitBis(LocalDateTime.of(2024, 1, 5, 10, 45, 0));
        final var interval4 = new IntervalDto();
        interval4.setDatumUhrzeitVon(LocalDateTime.of(2024, 1, 5, 10, 45, 0));
        interval4.setDatumUhrzeitBis(LocalDateTime.of(2024, 1, 5, 11, 0, 0));
        final var interval5 = new IntervalDto();
        interval5.setDatumUhrzeitVon(LocalDateTime.of(2024, 1, 5, 11, 0, 0));
        interval5.setDatumUhrzeitBis(LocalDateTime.of(2024, 1, 5, 11, 15, 0));

        final var intervals = List.of(interval0, interval1, interval2, interval3, interval4, interval5);
        final var result = listenausgabeService.getIntervalsWithinRange(intervals, LocalTime.of(10, 0, 0), LocalTime.of(11, 0, 0));

        Assertions.assertThat(result)
                .isNotNull()
                .isNotEmpty()
                .isEqualTo(List.of(interval1, interval2, interval3, interval4));

    }

}
