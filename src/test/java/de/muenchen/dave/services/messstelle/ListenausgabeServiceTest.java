package de.muenchen.dave.services.messstelle;

import de.muenchen.dave.domain.dtos.laden.messwerte.LadeMesswerteDTO;
import de.muenchen.dave.domain.dtos.messstelle.MessstelleOptionsDTO;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ListenausgabeServiceTest {

    private ListenausgabeService listenausgabeService;

    @Mock
    private SpitzenstundeService spitzenstundeService;


    @BeforeEach
    public void beforeEach() {
        listenausgabeService = new ListenausgabeService(new LadeMesswerteMapperImpl(), spitzenstundeService);
        Mockito.reset(spitzenstundeService);
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

        var result = listenausgabeService.calculateSumOfIntervalsAndAddBlockSpecificDataToResult(List.of(interval0, interval1, interval2, interval3, interval4, interval5), Zeitblock.ZB_10_11);

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
        final var result  = listenausgabeService.getIntervalsWithinZeitblock(intervals, Zeitblock.ZB_10_11);

        Assertions.assertThat(result)
                .isNotNull()
                .isNotEmpty()
                .isEqualTo(List.of( interval1, interval2, interval3, interval4));
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
        final var result  = listenausgabeService.getIntervalsWithinRange(intervals, LocalTime.of(10,0,0), LocalTime.of(11,0,0));

        Assertions.assertThat(result)
                .isNotNull()
                .isNotEmpty()
                .isEqualTo(List.of( interval1, interval2, interval3, interval4));

    }

}