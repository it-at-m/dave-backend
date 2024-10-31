package de.muenchen.dave.services.messstelle;

import de.muenchen.dave.domain.dtos.laden.messwerte.LadeMesswerteDTO;
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
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
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
    void ladeListenausgabeZeitauswahlSpitzenstunde() {

        final IntervalDto interval0 = new IntervalDto();
        interval0.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 0)));
        interval0.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 30)));
        interval0.setAnzahlLfw(BigDecimal.valueOf(1));
        interval0.setAnzahlKrad(BigDecimal.valueOf(2));
        interval0.setAnzahlLkw(BigDecimal.valueOf(3));
        interval0.setAnzahlBus(BigDecimal.valueOf(4));
        interval0.setAnzahlRad(BigDecimal.valueOf(5));
        interval0.setSummeAllePkw(BigDecimal.valueOf(6));
        interval0.setSummeLastzug(BigDecimal.valueOf(7));
        interval0.setSummeGueterverkehr(BigDecimal.valueOf(8));
        interval0.setSummeSchwerverkehr(BigDecimal.valueOf(9));
        interval0.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(10));

        final IntervalDto interval1 = new IntervalDto();
        interval1.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 30)));
        interval1.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(1, 0)));
        interval1.setAnzahlLfw(BigDecimal.valueOf(2));
        interval1.setAnzahlKrad(BigDecimal.valueOf(3));
        interval1.setAnzahlLkw(BigDecimal.valueOf(4));
        interval1.setAnzahlBus(BigDecimal.valueOf(5));
        interval1.setAnzahlRad(BigDecimal.valueOf(6));
        interval1.setSummeAllePkw(BigDecimal.valueOf(7));
        interval1.setSummeLastzug(BigDecimal.valueOf(8));
        interval1.setSummeGueterverkehr(BigDecimal.valueOf(9));
        interval1.setSummeSchwerverkehr(BigDecimal.valueOf(10));
        interval1.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(11));

        final IntervalDto interval2 = new IntervalDto();
        interval2.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(1, 0)));
        interval2.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(1, 30)));
        interval2.setAnzahlLfw(BigDecimal.valueOf(3));
        interval2.setAnzahlKrad(BigDecimal.valueOf(4));
        interval2.setAnzahlLkw(BigDecimal.valueOf(5));
        interval2.setAnzahlBus(BigDecimal.valueOf(6));
        interval2.setAnzahlRad(BigDecimal.valueOf(7));
        interval2.setSummeAllePkw(BigDecimal.valueOf(8));
        interval2.setSummeLastzug(BigDecimal.valueOf(9));
        interval2.setSummeGueterverkehr(BigDecimal.valueOf(10));
        interval2.setSummeSchwerverkehr(BigDecimal.valueOf(11));
        interval2.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(12));

        final IntervalDto interval3 = new IntervalDto();
        interval3.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(1, 30)));
        interval3.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(2, 0)));
        interval3.setAnzahlLfw(BigDecimal.valueOf(4));
        interval3.setAnzahlKrad(BigDecimal.valueOf(5));
        interval3.setAnzahlLkw(BigDecimal.valueOf(6));
        interval3.setAnzahlBus(BigDecimal.valueOf(7));
        interval3.setAnzahlRad(BigDecimal.valueOf(8));
        interval3.setSummeAllePkw(BigDecimal.valueOf(9));
        interval3.setSummeLastzug(BigDecimal.valueOf(10));
        interval3.setSummeGueterverkehr(BigDecimal.valueOf(11));
        interval3.setSummeSchwerverkehr(BigDecimal.valueOf(12));
        interval3.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(13));

        final IntervalDto interval4 = new IntervalDto();
        interval4.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(2, 0)));
        interval4.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(2, 30)));
        interval4.setAnzahlLfw(BigDecimal.valueOf(5));
        interval4.setAnzahlKrad(BigDecimal.valueOf(6));
        interval4.setAnzahlLkw(BigDecimal.valueOf(7));
        interval4.setAnzahlBus(BigDecimal.valueOf(8));
        interval4.setAnzahlRad(BigDecimal.valueOf(9));
        interval4.setSummeAllePkw(BigDecimal.valueOf(10));
        interval4.setSummeLastzug(BigDecimal.valueOf(11));
        interval4.setSummeGueterverkehr(BigDecimal.valueOf(12));
        interval4.setSummeSchwerverkehr(BigDecimal.valueOf(13));
        interval4.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(14));

        final IntervalDto interval5 = new IntervalDto();
        interval5.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(2, 30)));
        interval5.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(3, 0)));
        interval5.setAnzahlLfw(BigDecimal.valueOf(6));
        interval5.setAnzahlKrad(BigDecimal.valueOf(7));
        interval5.setAnzahlLkw(BigDecimal.valueOf(8));
        interval5.setAnzahlBus(BigDecimal.valueOf(9));
        interval5.setAnzahlRad(BigDecimal.valueOf(10));
        interval5.setSummeAllePkw(BigDecimal.valueOf(11));
        interval5.setSummeLastzug(BigDecimal.valueOf(12));
        interval5.setSummeGueterverkehr(BigDecimal.valueOf(13));
        interval5.setSummeSchwerverkehr(BigDecimal.valueOf(14));
        interval5.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(15));

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

        final var expectedSpitzenstunde = new LadeMesswerteDTO();
        expectedSpitzenstunde.setType("SpStdBlock KFZ");
        expectedSpitzenstunde.setSortingIndex(12000000);
        expectedSpitzenstunde.setStartUhrzeit(LocalTime.of(2, 0, 0));
        expectedSpitzenstunde.setEndeUhrzeit(LocalTime.of(3, 0, 0));
        expectedSpitzenstunde.setLfw(11);
        expectedSpitzenstunde.setKraftraeder(13);
        expectedSpitzenstunde.setLkw(15);
        expectedSpitzenstunde.setBusse(17);
        expectedSpitzenstunde.setFahrradfahrer(19);
        expectedSpitzenstunde.setPkw(21);
        expectedSpitzenstunde.setLastzuege(23);
        expectedSpitzenstunde.setGueterverkehr(25);
        expectedSpitzenstunde.setSchwerverkehr(27);
        expectedSpitzenstunde.setKfz(29);
        expectedSpitzenstunde.setAnteilSchwerverkehrAnKfzProzent(93.1);
        expectedSpitzenstunde.setAnteilGueterverkehrAnKfzProzent(86.2);
        expectedSpitzenstunde.setFussgaenger(null);

        Assertions.assertThat(result.getZaehldaten().size()).isEqualTo(8);
        Assertions.assertThat(result.getZaehldaten().getLast()).isEqualTo(expectedSpitzenstunde);
    }

    @Test
    void ladeListenausgabeZeitauswahlStundeWithStundensumme() {
        final IntervalDto interval2 = new IntervalDto();
        interval2.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(1, 0)));
        interval2.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(1, 30)));
        interval2.setAnzahlLfw(BigDecimal.valueOf(3));
        interval2.setAnzahlKrad(BigDecimal.valueOf(4));
        interval2.setAnzahlLkw(BigDecimal.valueOf(5));
        interval2.setAnzahlBus(BigDecimal.valueOf(6));
        interval2.setAnzahlRad(BigDecimal.valueOf(7));
        interval2.setSummeAllePkw(BigDecimal.valueOf(8));
        interval2.setSummeLastzug(BigDecimal.valueOf(9));
        interval2.setSummeGueterverkehr(BigDecimal.valueOf(10));
        interval2.setSummeSchwerverkehr(BigDecimal.valueOf(11));
        interval2.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(12));

        final IntervalDto interval3 = new IntervalDto();
        interval3.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(1, 30)));
        interval3.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(2, 0)));
        interval3.setAnzahlLfw(BigDecimal.valueOf(4));
        interval3.setAnzahlKrad(BigDecimal.valueOf(5));
        interval3.setAnzahlLkw(BigDecimal.valueOf(6));
        interval3.setAnzahlBus(BigDecimal.valueOf(7));
        interval3.setAnzahlRad(BigDecimal.valueOf(8));
        interval3.setSummeAllePkw(BigDecimal.valueOf(9));
        interval3.setSummeLastzug(BigDecimal.valueOf(10));
        interval3.setSummeGueterverkehr(BigDecimal.valueOf(11));
        interval3.setSummeSchwerverkehr(BigDecimal.valueOf(12));
        interval3.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(13));

        final var intervals = List.of(interval2, interval3);
        final var options = new MessstelleOptionsDTO();
        options.setZeitauswahl("Stunde");
        options.setIntervall(ZaehldatenIntervall.STUNDE_HALB);
        options.setStundensumme(true);
        options.setZeitblock(Zeitblock.ZB_01_02);
        final var result = listenausgabeService.ladeListenausgabe(intervals, true, options);

        final var expectedSumme = new LadeMesswerteDTO();
        expectedSumme.setType("Stunde");
        expectedSumme.setSortingIndex(11008008);
        expectedSumme.setStartUhrzeit(LocalTime.of(1, 0, 0));
        expectedSumme.setEndeUhrzeit(LocalTime.of(2, 0, 0));
        expectedSumme.setLfw(7);
        expectedSumme.setKraftraeder(9);
        expectedSumme.setLkw(11);
        expectedSumme.setBusse(13);
        expectedSumme.setFahrradfahrer(15);
        expectedSumme.setPkw(17);
        expectedSumme.setLastzuege(19);
        expectedSumme.setGueterverkehr(21);
        expectedSumme.setSchwerverkehr(23);
        expectedSumme.setKfz(25);
        expectedSumme.setAnteilSchwerverkehrAnKfzProzent(92.0);
        expectedSumme.setAnteilGueterverkehrAnKfzProzent(84.0);
        expectedSumme.setFussgaenger(null);

        Assertions.assertThat(result.getZaehldaten().size()).isEqualTo(3);
        Assertions.assertThat(result.getZaehldaten().getLast()).isEqualTo(expectedSumme);
    }

    @Test
    void ladeListenausgabeZeitauswahlStundeWithoutStundensumme() {
        final IntervalDto interval2 = new IntervalDto();
        interval2.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(1, 0)));
        interval2.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(1, 30)));
        interval2.setAnzahlLfw(BigDecimal.valueOf(3));
        interval2.setAnzahlKrad(BigDecimal.valueOf(4));
        interval2.setAnzahlLkw(BigDecimal.valueOf(5));
        interval2.setAnzahlBus(BigDecimal.valueOf(6));
        interval2.setAnzahlRad(BigDecimal.valueOf(7));
        interval2.setSummeAllePkw(BigDecimal.valueOf(8));
        interval2.setSummeLastzug(BigDecimal.valueOf(9));
        interval2.setSummeGueterverkehr(BigDecimal.valueOf(10));
        interval2.setSummeSchwerverkehr(BigDecimal.valueOf(11));
        interval2.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(12));

        final IntervalDto interval3 = new IntervalDto();
        interval3.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(1, 30)));
        interval3.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(2, 0)));
        interval3.setAnzahlLfw(BigDecimal.valueOf(4));
        interval3.setAnzahlKrad(BigDecimal.valueOf(5));
        interval3.setAnzahlLkw(BigDecimal.valueOf(6));
        interval3.setAnzahlBus(BigDecimal.valueOf(7));
        interval3.setAnzahlRad(BigDecimal.valueOf(8));
        interval3.setSummeAllePkw(BigDecimal.valueOf(9));
        interval3.setSummeLastzug(BigDecimal.valueOf(10));
        interval3.setSummeGueterverkehr(BigDecimal.valueOf(11));
        interval3.setSummeSchwerverkehr(BigDecimal.valueOf(12));
        interval3.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(13));

        final var intervals = List.of(interval2, interval3);
        final var options = new MessstelleOptionsDTO();
        options.setZeitauswahl("Stunde");
        options.setIntervall(ZaehldatenIntervall.STUNDE_HALB);
        options.setStundensumme(false);
        options.setZeitblock(Zeitblock.ZB_01_02);
        final var result = listenausgabeService.ladeListenausgabe(intervals, true, options);

        Assertions.assertThat(result.getZaehldaten().size()).isEqualTo(2);
    }

    @Test
    void ladeListenausgabeZeitauswahlStundeWithStundensummeAndIntervallStunde() {
        final IntervalDto interval2 = new IntervalDto();
        interval2.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(1, 0)));
        interval2.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(2, 0)));
        interval2.setAnzahlLfw(BigDecimal.valueOf(3));
        interval2.setAnzahlKrad(BigDecimal.valueOf(4));
        interval2.setAnzahlLkw(BigDecimal.valueOf(5));
        interval2.setAnzahlBus(BigDecimal.valueOf(6));
        interval2.setAnzahlRad(BigDecimal.valueOf(7));
        interval2.setSummeAllePkw(BigDecimal.valueOf(8));
        interval2.setSummeLastzug(BigDecimal.valueOf(9));
        interval2.setSummeGueterverkehr(BigDecimal.valueOf(10));
        interval2.setSummeSchwerverkehr(BigDecimal.valueOf(11));
        interval2.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(12));

        final var intervals = List.of(interval2);
        final var options = new MessstelleOptionsDTO();
        options.setZeitauswahl("Stunde");
        options.setIntervall(ZaehldatenIntervall.STUNDE_KOMPLETT);
        options.setStundensumme(true);
        options.setZeitblock(Zeitblock.ZB_01_02);
        final var result = listenausgabeService.ladeListenausgabe(intervals, true, options);

        Assertions.assertThat(result.getZaehldaten().size()).isEqualTo(1);
    }

    @Test
    void ladeListenausgabeZeitauswahlTageswertWithStundensummeAndIntervallHalbeStundeAndSpitzenstundeAndBlocksumme() {
        final var intervals = new ArrayList<IntervalDto>();

        var interval = new IntervalDto();
        interval.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 0)));
        interval.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 30)));
        interval.setAnzahlLfw(BigDecimal.valueOf(1));
        interval.setAnzahlKrad(BigDecimal.valueOf(2));
        interval.setAnzahlLkw(BigDecimal.valueOf(3));
        interval.setAnzahlBus(BigDecimal.valueOf(4));
        interval.setAnzahlRad(BigDecimal.valueOf(5));
        interval.setSummeAllePkw(BigDecimal.valueOf(6));
        interval.setSummeLastzug(BigDecimal.valueOf(7));
        interval.setSummeGueterverkehr(BigDecimal.valueOf(8));
        interval.setSummeSchwerverkehr(BigDecimal.valueOf(9));
        interval.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(10));
        intervals.add(interval);

        interval = new IntervalDto();
        interval.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 30)));
        interval.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(1, 0)));
        interval.setAnzahlLfw(BigDecimal.valueOf(2));
        interval.setAnzahlKrad(BigDecimal.valueOf(3));
        interval.setAnzahlLkw(BigDecimal.valueOf(4));
        interval.setAnzahlBus(BigDecimal.valueOf(5));
        interval.setAnzahlRad(BigDecimal.valueOf(6));
        interval.setSummeAllePkw(BigDecimal.valueOf(7));
        interval.setSummeLastzug(BigDecimal.valueOf(8));
        interval.setSummeGueterverkehr(BigDecimal.valueOf(9));
        interval.setSummeSchwerverkehr(BigDecimal.valueOf(10));
        interval.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(11));
        intervals.add(interval);

        interval = new IntervalDto();
        interval.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(1, 0)));
        interval.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(1, 30)));
        interval.setAnzahlLfw(BigDecimal.valueOf(3));
        interval.setAnzahlKrad(BigDecimal.valueOf(4));
        interval.setAnzahlLkw(BigDecimal.valueOf(5));
        interval.setAnzahlBus(BigDecimal.valueOf(6));
        interval.setAnzahlRad(BigDecimal.valueOf(7));
        interval.setSummeAllePkw(BigDecimal.valueOf(8));
        interval.setSummeLastzug(BigDecimal.valueOf(9));
        interval.setSummeGueterverkehr(BigDecimal.valueOf(10));
        interval.setSummeSchwerverkehr(BigDecimal.valueOf(11));
        interval.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(12));
        intervals.add(interval);

        interval = new IntervalDto();
        interval.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(1, 30)));
        interval.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(2, 0)));
        interval.setAnzahlLfw(BigDecimal.valueOf(4));
        interval.setAnzahlKrad(BigDecimal.valueOf(5));
        interval.setAnzahlLkw(BigDecimal.valueOf(6));
        interval.setAnzahlBus(BigDecimal.valueOf(7));
        interval.setAnzahlRad(BigDecimal.valueOf(8));
        interval.setSummeAllePkw(BigDecimal.valueOf(9));
        interval.setSummeLastzug(BigDecimal.valueOf(10));
        interval.setSummeGueterverkehr(BigDecimal.valueOf(11));
        interval.setSummeSchwerverkehr(BigDecimal.valueOf(12));
        interval.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(13));
        intervals.add(interval);

        interval = new IntervalDto();
        interval.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(2, 0)));
        interval.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(2, 30)));
        interval.setAnzahlLfw(BigDecimal.valueOf(5));
        interval.setAnzahlKrad(BigDecimal.valueOf(6));
        interval.setAnzahlLkw(BigDecimal.valueOf(7));
        interval.setAnzahlBus(BigDecimal.valueOf(8));
        interval.setAnzahlRad(BigDecimal.valueOf(9));
        interval.setSummeAllePkw(BigDecimal.valueOf(10));
        interval.setSummeLastzug(BigDecimal.valueOf(11));
        interval.setSummeGueterverkehr(BigDecimal.valueOf(12));
        interval.setSummeSchwerverkehr(BigDecimal.valueOf(13));
        interval.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(14));
        intervals.add(interval);

        interval = new IntervalDto();
        interval.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(2, 30)));
        interval.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(3, 0)));
        interval.setAnzahlLfw(BigDecimal.valueOf(6));
        interval.setAnzahlKrad(BigDecimal.valueOf(7));
        interval.setAnzahlLkw(BigDecimal.valueOf(8));
        interval.setAnzahlBus(BigDecimal.valueOf(9));
        interval.setAnzahlRad(BigDecimal.valueOf(10));
        interval.setSummeAllePkw(BigDecimal.valueOf(11));
        interval.setSummeLastzug(BigDecimal.valueOf(12));
        interval.setSummeGueterverkehr(BigDecimal.valueOf(13));
        interval.setSummeSchwerverkehr(BigDecimal.valueOf(14));
        interval.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(15));
        intervals.add(interval);

        interval = new IntervalDto();
        interval.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(3, 0)));
        interval.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(3, 30)));
        interval.setAnzahlLfw(BigDecimal.valueOf(1));
        interval.setAnzahlKrad(BigDecimal.valueOf(1));
        interval.setAnzahlLkw(BigDecimal.valueOf(1));
        interval.setAnzahlBus(BigDecimal.valueOf(1));
        interval.setAnzahlRad(BigDecimal.valueOf(1));
        interval.setSummeAllePkw(BigDecimal.valueOf(1));
        interval.setSummeLastzug(BigDecimal.valueOf(1));
        interval.setSummeGueterverkehr(BigDecimal.valueOf(9));
        interval.setSummeSchwerverkehr(BigDecimal.valueOf(9));
        interval.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(9));
        intervals.add(interval);

        interval = new IntervalDto();
        interval.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(3, 30)));
        interval.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(4, 0)));
        interval.setAnzahlLfw(BigDecimal.valueOf(1));
        interval.setAnzahlKrad(BigDecimal.valueOf(1));
        interval.setAnzahlLkw(BigDecimal.valueOf(1));
        interval.setAnzahlBus(BigDecimal.valueOf(1));
        interval.setAnzahlRad(BigDecimal.valueOf(1));
        interval.setSummeAllePkw(BigDecimal.valueOf(1));
        interval.setSummeLastzug(BigDecimal.valueOf(1));
        interval.setSummeGueterverkehr(BigDecimal.valueOf(9));
        interval.setSummeSchwerverkehr(BigDecimal.valueOf(9));
        interval.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(9));
        intervals.add(interval);

        interval = new IntervalDto();
        interval.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(4, 0)));
        interval.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(4, 30)));
        interval.setAnzahlLfw(BigDecimal.valueOf(1));
        interval.setAnzahlKrad(BigDecimal.valueOf(1));
        interval.setAnzahlLkw(BigDecimal.valueOf(1));
        interval.setAnzahlBus(BigDecimal.valueOf(1));
        interval.setAnzahlRad(BigDecimal.valueOf(1));
        interval.setSummeAllePkw(BigDecimal.valueOf(1));
        interval.setSummeLastzug(BigDecimal.valueOf(1));
        interval.setSummeGueterverkehr(BigDecimal.valueOf(9));
        interval.setSummeSchwerverkehr(BigDecimal.valueOf(9));
        interval.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(9));
        intervals.add(interval);

        interval = new IntervalDto();
        interval.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(4, 30)));
        interval.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(5, 0)));
        interval.setAnzahlLfw(BigDecimal.valueOf(1));
        interval.setAnzahlKrad(BigDecimal.valueOf(1));
        interval.setAnzahlLkw(BigDecimal.valueOf(1));
        interval.setAnzahlBus(BigDecimal.valueOf(1));
        interval.setAnzahlRad(BigDecimal.valueOf(1));
        interval.setSummeAllePkw(BigDecimal.valueOf(1));
        interval.setSummeLastzug(BigDecimal.valueOf(1));
        interval.setSummeGueterverkehr(BigDecimal.valueOf(9));
        interval.setSummeSchwerverkehr(BigDecimal.valueOf(9));
        interval.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(9));
        intervals.add(interval);

        interval = new IntervalDto();
        interval.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(5, 0)));
        interval.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(5, 30)));
        interval.setAnzahlLfw(BigDecimal.valueOf(1));
        interval.setAnzahlKrad(BigDecimal.valueOf(1));
        interval.setAnzahlLkw(BigDecimal.valueOf(1));
        interval.setAnzahlBus(BigDecimal.valueOf(1));
        interval.setAnzahlRad(BigDecimal.valueOf(1));
        interval.setSummeAllePkw(BigDecimal.valueOf(1));
        interval.setSummeLastzug(BigDecimal.valueOf(1));
        interval.setSummeGueterverkehr(BigDecimal.valueOf(9));
        interval.setSummeSchwerverkehr(BigDecimal.valueOf(9));
        interval.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(9));
        intervals.add(interval);

        interval = new IntervalDto();
        interval.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(5, 30)));
        interval.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(6, 0)));
        interval.setAnzahlLfw(BigDecimal.valueOf(1));
        interval.setAnzahlKrad(BigDecimal.valueOf(1));
        interval.setAnzahlLkw(BigDecimal.valueOf(1));
        interval.setAnzahlBus(BigDecimal.valueOf(1));
        interval.setAnzahlRad(BigDecimal.valueOf(1));
        interval.setSummeAllePkw(BigDecimal.valueOf(1));
        interval.setSummeLastzug(BigDecimal.valueOf(1));
        interval.setSummeGueterverkehr(BigDecimal.valueOf(9));
        interval.setSummeSchwerverkehr(BigDecimal.valueOf(9));
        interval.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(9));
        intervals.add(interval);

        interval = new IntervalDto();
        interval.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(6, 0)));
        interval.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(6, 30)));
        interval.setAnzahlLfw(BigDecimal.valueOf(1));
        interval.setAnzahlKrad(BigDecimal.valueOf(1));
        interval.setAnzahlLkw(BigDecimal.valueOf(1));
        interval.setAnzahlBus(BigDecimal.valueOf(1));
        interval.setAnzahlRad(BigDecimal.valueOf(1));
        interval.setSummeAllePkw(BigDecimal.valueOf(1));
        interval.setSummeLastzug(BigDecimal.valueOf(1));
        interval.setSummeGueterverkehr(BigDecimal.valueOf(9));
        interval.setSummeSchwerverkehr(BigDecimal.valueOf(9));
        interval.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(9));
        intervals.add(interval);

        interval = new IntervalDto();
        interval.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(6, 30)));
        interval.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(7, 0)));
        interval.setAnzahlLfw(BigDecimal.valueOf(1));
        interval.setAnzahlKrad(BigDecimal.valueOf(1));
        interval.setAnzahlLkw(BigDecimal.valueOf(1));
        interval.setAnzahlBus(BigDecimal.valueOf(1));
        interval.setAnzahlRad(BigDecimal.valueOf(1));
        interval.setSummeAllePkw(BigDecimal.valueOf(1));
        interval.setSummeLastzug(BigDecimal.valueOf(1));
        interval.setSummeGueterverkehr(BigDecimal.valueOf(9));
        interval.setSummeSchwerverkehr(BigDecimal.valueOf(9));
        interval.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(9));
        intervals.add(interval);

        interval = new IntervalDto();
        interval.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(7, 0)));
        interval.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(7, 30)));
        interval.setAnzahlLfw(BigDecimal.valueOf(1));
        interval.setAnzahlKrad(BigDecimal.valueOf(1));
        interval.setAnzahlLkw(BigDecimal.valueOf(1));
        interval.setAnzahlBus(BigDecimal.valueOf(1));
        interval.setAnzahlRad(BigDecimal.valueOf(1));
        interval.setSummeAllePkw(BigDecimal.valueOf(1));
        interval.setSummeLastzug(BigDecimal.valueOf(1));
        interval.setSummeGueterverkehr(BigDecimal.valueOf(9));
        interval.setSummeSchwerverkehr(BigDecimal.valueOf(9));
        interval.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(9));
        intervals.add(interval);

        interval = new IntervalDto();
        interval.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(7, 30)));
        interval.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(8, 0)));
        interval.setAnzahlLfw(BigDecimal.valueOf(1));
        interval.setAnzahlKrad(BigDecimal.valueOf(1));
        interval.setAnzahlLkw(BigDecimal.valueOf(1));
        interval.setAnzahlBus(BigDecimal.valueOf(1));
        interval.setAnzahlRad(BigDecimal.valueOf(1));
        interval.setSummeAllePkw(BigDecimal.valueOf(1));
        interval.setSummeLastzug(BigDecimal.valueOf(1));
        interval.setSummeGueterverkehr(BigDecimal.valueOf(9));
        interval.setSummeSchwerverkehr(BigDecimal.valueOf(9));
        interval.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(9));
        intervals.add(interval);

        interval = new IntervalDto();
        interval.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(8, 0)));
        interval.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(8, 30)));
        interval.setAnzahlLfw(BigDecimal.valueOf(1));
        interval.setAnzahlKrad(BigDecimal.valueOf(1));
        interval.setAnzahlLkw(BigDecimal.valueOf(1));
        interval.setAnzahlBus(BigDecimal.valueOf(1));
        interval.setAnzahlRad(BigDecimal.valueOf(1));
        interval.setSummeAllePkw(BigDecimal.valueOf(1));
        interval.setSummeLastzug(BigDecimal.valueOf(1));
        interval.setSummeGueterverkehr(BigDecimal.valueOf(9));
        interval.setSummeSchwerverkehr(BigDecimal.valueOf(9));
        interval.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(9));
        intervals.add(interval);

        interval = new IntervalDto();
        interval.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(8, 30)));
        interval.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(9, 0)));
        interval.setAnzahlLfw(BigDecimal.valueOf(1));
        interval.setAnzahlKrad(BigDecimal.valueOf(1));
        interval.setAnzahlLkw(BigDecimal.valueOf(1));
        interval.setAnzahlBus(BigDecimal.valueOf(1));
        interval.setAnzahlRad(BigDecimal.valueOf(1));
        interval.setSummeAllePkw(BigDecimal.valueOf(1));
        interval.setSummeLastzug(BigDecimal.valueOf(1));
        interval.setSummeGueterverkehr(BigDecimal.valueOf(9));
        interval.setSummeSchwerverkehr(BigDecimal.valueOf(9));
        interval.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(9));
        intervals.add(interval);

        interval = new IntervalDto();
        interval.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(9, 0)));
        interval.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(9, 30)));
        interval.setAnzahlLfw(BigDecimal.valueOf(1));
        interval.setAnzahlKrad(BigDecimal.valueOf(1));
        interval.setAnzahlLkw(BigDecimal.valueOf(1));
        interval.setAnzahlBus(BigDecimal.valueOf(1));
        interval.setAnzahlRad(BigDecimal.valueOf(1));
        interval.setSummeAllePkw(BigDecimal.valueOf(1));
        interval.setSummeLastzug(BigDecimal.valueOf(1));
        interval.setSummeGueterverkehr(BigDecimal.valueOf(9));
        interval.setSummeSchwerverkehr(BigDecimal.valueOf(9));
        interval.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(9));
        intervals.add(interval);

        interval = new IntervalDto();
        interval.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(9, 30)));
        interval.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(10, 0)));
        interval.setAnzahlLfw(BigDecimal.valueOf(1));
        interval.setAnzahlKrad(BigDecimal.valueOf(1));
        interval.setAnzahlLkw(BigDecimal.valueOf(1));
        interval.setAnzahlBus(BigDecimal.valueOf(1));
        interval.setAnzahlRad(BigDecimal.valueOf(1));
        interval.setSummeAllePkw(BigDecimal.valueOf(1));
        interval.setSummeLastzug(BigDecimal.valueOf(1));
        interval.setSummeGueterverkehr(BigDecimal.valueOf(9));
        interval.setSummeSchwerverkehr(BigDecimal.valueOf(9));
        interval.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(9));
        intervals.add(interval);

        interval = new IntervalDto();
        interval.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(10, 0)));
        interval.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(10, 30)));
        interval.setAnzahlLfw(BigDecimal.valueOf(1));
        interval.setAnzahlKrad(BigDecimal.valueOf(1));
        interval.setAnzahlLkw(BigDecimal.valueOf(1));
        interval.setAnzahlBus(BigDecimal.valueOf(1));
        interval.setAnzahlRad(BigDecimal.valueOf(1));
        interval.setSummeAllePkw(BigDecimal.valueOf(1));
        interval.setSummeLastzug(BigDecimal.valueOf(1));
        interval.setSummeGueterverkehr(BigDecimal.valueOf(9));
        interval.setSummeSchwerverkehr(BigDecimal.valueOf(9));
        interval.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(9));
        intervals.add(interval);

        interval = new IntervalDto();
        interval.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(10, 30)));
        interval.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(11, 0)));
        interval.setAnzahlLfw(BigDecimal.valueOf(1));
        interval.setAnzahlKrad(BigDecimal.valueOf(1));
        interval.setAnzahlLkw(BigDecimal.valueOf(1));
        interval.setAnzahlBus(BigDecimal.valueOf(1));
        interval.setAnzahlRad(BigDecimal.valueOf(1));
        interval.setSummeAllePkw(BigDecimal.valueOf(1));
        interval.setSummeLastzug(BigDecimal.valueOf(1));
        interval.setSummeGueterverkehr(BigDecimal.valueOf(9));
        interval.setSummeSchwerverkehr(BigDecimal.valueOf(9));
        interval.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(9));
        intervals.add(interval);

        interval = new IntervalDto();
        interval.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(11, 0)));
        interval.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(11, 30)));
        interval.setAnzahlLfw(BigDecimal.valueOf(1));
        interval.setAnzahlKrad(BigDecimal.valueOf(1));
        interval.setAnzahlLkw(BigDecimal.valueOf(1));
        interval.setAnzahlBus(BigDecimal.valueOf(1));
        interval.setAnzahlRad(BigDecimal.valueOf(1));
        interval.setSummeAllePkw(BigDecimal.valueOf(1));
        interval.setSummeLastzug(BigDecimal.valueOf(1));
        interval.setSummeGueterverkehr(BigDecimal.valueOf(9));
        interval.setSummeSchwerverkehr(BigDecimal.valueOf(9));
        interval.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(9));
        intervals.add(interval);

        interval = new IntervalDto();
        interval.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(11, 30)));
        interval.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(12, 0)));
        interval.setAnzahlLfw(BigDecimal.valueOf(1));
        interval.setAnzahlKrad(BigDecimal.valueOf(1));
        interval.setAnzahlLkw(BigDecimal.valueOf(1));
        interval.setAnzahlBus(BigDecimal.valueOf(1));
        interval.setAnzahlRad(BigDecimal.valueOf(1));
        interval.setSummeAllePkw(BigDecimal.valueOf(1));
        interval.setSummeLastzug(BigDecimal.valueOf(1));
        interval.setSummeGueterverkehr(BigDecimal.valueOf(9));
        interval.setSummeSchwerverkehr(BigDecimal.valueOf(9));
        interval.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(9));
        intervals.add(interval);

        interval = new IntervalDto();
        interval.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(12, 0)));
        interval.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(12, 30)));
        interval.setAnzahlLfw(BigDecimal.valueOf(1));
        interval.setAnzahlKrad(BigDecimal.valueOf(1));
        interval.setAnzahlLkw(BigDecimal.valueOf(1));
        interval.setAnzahlBus(BigDecimal.valueOf(1));
        interval.setAnzahlRad(BigDecimal.valueOf(1));
        interval.setSummeAllePkw(BigDecimal.valueOf(1));
        interval.setSummeLastzug(BigDecimal.valueOf(1));
        interval.setSummeGueterverkehr(BigDecimal.valueOf(9));
        interval.setSummeSchwerverkehr(BigDecimal.valueOf(9));
        interval.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(9));
        intervals.add(interval);

        interval = new IntervalDto();
        interval.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(12, 30)));
        interval.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(13, 0)));
        interval.setAnzahlLfw(BigDecimal.valueOf(1));
        interval.setAnzahlKrad(BigDecimal.valueOf(1));
        interval.setAnzahlLkw(BigDecimal.valueOf(1));
        interval.setAnzahlBus(BigDecimal.valueOf(1));
        interval.setAnzahlRad(BigDecimal.valueOf(1));
        interval.setSummeAllePkw(BigDecimal.valueOf(1));
        interval.setSummeLastzug(BigDecimal.valueOf(1));
        interval.setSummeGueterverkehr(BigDecimal.valueOf(9));
        interval.setSummeSchwerverkehr(BigDecimal.valueOf(9));
        interval.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(9));
        intervals.add(interval);

        interval = new IntervalDto();
        interval.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(13, 0)));
        interval.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(13, 30)));
        interval.setAnzahlLfw(BigDecimal.valueOf(1));
        interval.setAnzahlKrad(BigDecimal.valueOf(1));
        interval.setAnzahlLkw(BigDecimal.valueOf(1));
        interval.setAnzahlBus(BigDecimal.valueOf(1));
        interval.setAnzahlRad(BigDecimal.valueOf(1));
        interval.setSummeAllePkw(BigDecimal.valueOf(1));
        interval.setSummeLastzug(BigDecimal.valueOf(1));
        interval.setSummeGueterverkehr(BigDecimal.valueOf(9));
        interval.setSummeSchwerverkehr(BigDecimal.valueOf(9));
        interval.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(9));
        intervals.add(interval);

        interval = new IntervalDto();
        interval.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(13, 30)));
        interval.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(14, 0)));
        interval.setAnzahlLfw(BigDecimal.valueOf(1));
        interval.setAnzahlKrad(BigDecimal.valueOf(1));
        interval.setAnzahlLkw(BigDecimal.valueOf(1));
        interval.setAnzahlBus(BigDecimal.valueOf(1));
        interval.setAnzahlRad(BigDecimal.valueOf(1));
        interval.setSummeAllePkw(BigDecimal.valueOf(1));
        interval.setSummeLastzug(BigDecimal.valueOf(1));
        interval.setSummeGueterverkehr(BigDecimal.valueOf(9));
        interval.setSummeSchwerverkehr(BigDecimal.valueOf(9));
        interval.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(9));
        intervals.add(interval);

        interval = new IntervalDto();
        interval.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(14, 0)));
        interval.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(14, 30)));
        interval.setAnzahlLfw(BigDecimal.valueOf(1));
        interval.setAnzahlKrad(BigDecimal.valueOf(1));
        interval.setAnzahlLkw(BigDecimal.valueOf(1));
        interval.setAnzahlBus(BigDecimal.valueOf(1));
        interval.setAnzahlRad(BigDecimal.valueOf(1));
        interval.setSummeAllePkw(BigDecimal.valueOf(1));
        interval.setSummeLastzug(BigDecimal.valueOf(1));
        interval.setSummeGueterverkehr(BigDecimal.valueOf(9));
        interval.setSummeSchwerverkehr(BigDecimal.valueOf(9));
        interval.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(9));
        intervals.add(interval);

        interval = new IntervalDto();
        interval.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(14, 30)));
        interval.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(15, 0)));
        interval.setAnzahlLfw(BigDecimal.valueOf(1));
        interval.setAnzahlKrad(BigDecimal.valueOf(1));
        interval.setAnzahlLkw(BigDecimal.valueOf(1));
        interval.setAnzahlBus(BigDecimal.valueOf(1));
        interval.setAnzahlRad(BigDecimal.valueOf(1));
        interval.setSummeAllePkw(BigDecimal.valueOf(1));
        interval.setSummeLastzug(BigDecimal.valueOf(1));
        interval.setSummeGueterverkehr(BigDecimal.valueOf(9));
        interval.setSummeSchwerverkehr(BigDecimal.valueOf(9));
        interval.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(9));
        intervals.add(interval);

        interval = new IntervalDto();
        interval.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(15, 0)));
        interval.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(15, 30)));
        interval.setAnzahlLfw(BigDecimal.valueOf(1));
        interval.setAnzahlKrad(BigDecimal.valueOf(1));
        interval.setAnzahlLkw(BigDecimal.valueOf(1));
        interval.setAnzahlBus(BigDecimal.valueOf(1));
        interval.setAnzahlRad(BigDecimal.valueOf(1));
        interval.setSummeAllePkw(BigDecimal.valueOf(1));
        interval.setSummeLastzug(BigDecimal.valueOf(1));
        interval.setSummeGueterverkehr(BigDecimal.valueOf(9));
        interval.setSummeSchwerverkehr(BigDecimal.valueOf(9));
        interval.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(9));
        intervals.add(interval);

        interval = new IntervalDto();
        interval.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(15, 30)));
        interval.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(16, 0)));
        interval.setAnzahlLfw(BigDecimal.valueOf(1));
        interval.setAnzahlKrad(BigDecimal.valueOf(1));
        interval.setAnzahlLkw(BigDecimal.valueOf(1));
        interval.setAnzahlBus(BigDecimal.valueOf(1));
        interval.setAnzahlRad(BigDecimal.valueOf(1));
        interval.setSummeAllePkw(BigDecimal.valueOf(1));
        interval.setSummeLastzug(BigDecimal.valueOf(1));
        interval.setSummeGueterverkehr(BigDecimal.valueOf(9));
        interval.setSummeSchwerverkehr(BigDecimal.valueOf(9));
        interval.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(9));
        intervals.add(interval);

        interval = new IntervalDto();
        interval.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(16, 0)));
        interval.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(16, 30)));
        interval.setAnzahlLfw(BigDecimal.valueOf(1));
        interval.setAnzahlKrad(BigDecimal.valueOf(1));
        interval.setAnzahlLkw(BigDecimal.valueOf(1));
        interval.setAnzahlBus(BigDecimal.valueOf(1));
        interval.setAnzahlRad(BigDecimal.valueOf(1));
        interval.setSummeAllePkw(BigDecimal.valueOf(1));
        interval.setSummeLastzug(BigDecimal.valueOf(1));
        interval.setSummeGueterverkehr(BigDecimal.valueOf(9));
        interval.setSummeSchwerverkehr(BigDecimal.valueOf(9));
        interval.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(9));
        intervals.add(interval);

        interval = new IntervalDto();
        interval.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(16, 30)));
        interval.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(17, 0)));
        interval.setAnzahlLfw(BigDecimal.valueOf(1));
        interval.setAnzahlKrad(BigDecimal.valueOf(1));
        interval.setAnzahlLkw(BigDecimal.valueOf(1));
        interval.setAnzahlBus(BigDecimal.valueOf(1));
        interval.setAnzahlRad(BigDecimal.valueOf(1));
        interval.setSummeAllePkw(BigDecimal.valueOf(1));
        interval.setSummeLastzug(BigDecimal.valueOf(1));
        interval.setSummeGueterverkehr(BigDecimal.valueOf(9));
        interval.setSummeSchwerverkehr(BigDecimal.valueOf(9));
        interval.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(9));
        intervals.add(interval);

        interval = new IntervalDto();
        interval.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(17, 0)));
        interval.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(17, 30)));
        interval.setAnzahlLfw(BigDecimal.valueOf(1));
        interval.setAnzahlKrad(BigDecimal.valueOf(1));
        interval.setAnzahlLkw(BigDecimal.valueOf(1));
        interval.setAnzahlBus(BigDecimal.valueOf(1));
        interval.setAnzahlRad(BigDecimal.valueOf(1));
        interval.setSummeAllePkw(BigDecimal.valueOf(1));
        interval.setSummeLastzug(BigDecimal.valueOf(1));
        interval.setSummeGueterverkehr(BigDecimal.valueOf(9));
        interval.setSummeSchwerverkehr(BigDecimal.valueOf(9));
        interval.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(9));
        intervals.add(interval);

        interval = new IntervalDto();
        interval.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(17, 30)));
        interval.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(18, 0)));
        interval.setAnzahlLfw(BigDecimal.valueOf(1));
        interval.setAnzahlKrad(BigDecimal.valueOf(1));
        interval.setAnzahlLkw(BigDecimal.valueOf(1));
        interval.setAnzahlBus(BigDecimal.valueOf(1));
        interval.setAnzahlRad(BigDecimal.valueOf(1));
        interval.setSummeAllePkw(BigDecimal.valueOf(1));
        interval.setSummeLastzug(BigDecimal.valueOf(1));
        interval.setSummeGueterverkehr(BigDecimal.valueOf(9));
        interval.setSummeSchwerverkehr(BigDecimal.valueOf(9));
        interval.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(9));
        intervals.add(interval);

        interval = new IntervalDto();
        interval.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(18, 0)));
        interval.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(18, 30)));
        interval.setAnzahlLfw(BigDecimal.valueOf(1));
        interval.setAnzahlKrad(BigDecimal.valueOf(1));
        interval.setAnzahlLkw(BigDecimal.valueOf(1));
        interval.setAnzahlBus(BigDecimal.valueOf(1));
        interval.setAnzahlRad(BigDecimal.valueOf(1));
        interval.setSummeAllePkw(BigDecimal.valueOf(1));
        interval.setSummeLastzug(BigDecimal.valueOf(1));
        interval.setSummeGueterverkehr(BigDecimal.valueOf(9));
        interval.setSummeSchwerverkehr(BigDecimal.valueOf(9));
        interval.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(9));
        intervals.add(interval);

        interval = new IntervalDto();
        interval.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(18, 30)));
        interval.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(19, 0)));
        interval.setAnzahlLfw(BigDecimal.valueOf(1));
        interval.setAnzahlKrad(BigDecimal.valueOf(1));
        interval.setAnzahlLkw(BigDecimal.valueOf(1));
        interval.setAnzahlBus(BigDecimal.valueOf(1));
        interval.setAnzahlRad(BigDecimal.valueOf(1));
        interval.setSummeAllePkw(BigDecimal.valueOf(1));
        interval.setSummeLastzug(BigDecimal.valueOf(1));
        interval.setSummeGueterverkehr(BigDecimal.valueOf(9));
        interval.setSummeSchwerverkehr(BigDecimal.valueOf(9));
        interval.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(9));
        intervals.add(interval);

        interval = new IntervalDto();
        interval.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(19, 0)));
        interval.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(19, 30)));
        interval.setAnzahlLfw(BigDecimal.valueOf(1));
        interval.setAnzahlKrad(BigDecimal.valueOf(1));
        interval.setAnzahlLkw(BigDecimal.valueOf(1));
        interval.setAnzahlBus(BigDecimal.valueOf(1));
        interval.setAnzahlRad(BigDecimal.valueOf(1));
        interval.setSummeAllePkw(BigDecimal.valueOf(1));
        interval.setSummeLastzug(BigDecimal.valueOf(1));
        interval.setSummeGueterverkehr(BigDecimal.valueOf(9));
        interval.setSummeSchwerverkehr(BigDecimal.valueOf(9));
        interval.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(9));
        intervals.add(interval);

        interval = new IntervalDto();
        interval.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(19, 30)));
        interval.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(20, 0)));
        interval.setAnzahlLfw(BigDecimal.valueOf(1));
        interval.setAnzahlKrad(BigDecimal.valueOf(1));
        interval.setAnzahlLkw(BigDecimal.valueOf(1));
        interval.setAnzahlBus(BigDecimal.valueOf(1));
        interval.setAnzahlRad(BigDecimal.valueOf(1));
        interval.setSummeAllePkw(BigDecimal.valueOf(1));
        interval.setSummeLastzug(BigDecimal.valueOf(1));
        interval.setSummeGueterverkehr(BigDecimal.valueOf(9));
        interval.setSummeSchwerverkehr(BigDecimal.valueOf(9));
        interval.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(9));
        intervals.add(interval);

        interval = new IntervalDto();
        interval.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(20, 0)));
        interval.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(20, 30)));
        interval.setAnzahlLfw(BigDecimal.valueOf(1));
        interval.setAnzahlKrad(BigDecimal.valueOf(1));
        interval.setAnzahlLkw(BigDecimal.valueOf(1));
        interval.setAnzahlBus(BigDecimal.valueOf(1));
        interval.setAnzahlRad(BigDecimal.valueOf(1));
        interval.setSummeAllePkw(BigDecimal.valueOf(1));
        interval.setSummeLastzug(BigDecimal.valueOf(1));
        interval.setSummeGueterverkehr(BigDecimal.valueOf(9));
        interval.setSummeSchwerverkehr(BigDecimal.valueOf(9));
        interval.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(9));
        intervals.add(interval);

        interval = new IntervalDto();
        interval.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(20, 30)));
        interval.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(21, 0)));
        interval.setAnzahlLfw(BigDecimal.valueOf(1));
        interval.setAnzahlKrad(BigDecimal.valueOf(1));
        interval.setAnzahlLkw(BigDecimal.valueOf(1));
        interval.setAnzahlBus(BigDecimal.valueOf(1));
        interval.setAnzahlRad(BigDecimal.valueOf(1));
        interval.setSummeAllePkw(BigDecimal.valueOf(1));
        interval.setSummeLastzug(BigDecimal.valueOf(1));
        interval.setSummeGueterverkehr(BigDecimal.valueOf(9));
        interval.setSummeSchwerverkehr(BigDecimal.valueOf(9));
        interval.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(9));
        intervals.add(interval);

        interval = new IntervalDto();
        interval.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(21, 0)));
        interval.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(21, 30)));
        interval.setAnzahlLfw(BigDecimal.valueOf(1));
        interval.setAnzahlKrad(BigDecimal.valueOf(1));
        interval.setAnzahlLkw(BigDecimal.valueOf(1));
        interval.setAnzahlBus(BigDecimal.valueOf(1));
        interval.setAnzahlRad(BigDecimal.valueOf(1));
        interval.setSummeAllePkw(BigDecimal.valueOf(1));
        interval.setSummeLastzug(BigDecimal.valueOf(1));
        interval.setSummeGueterverkehr(BigDecimal.valueOf(9));
        interval.setSummeSchwerverkehr(BigDecimal.valueOf(9));
        interval.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(9));
        intervals.add(interval);

        interval = new IntervalDto();
        interval.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(21, 30)));
        interval.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(22, 0)));
        interval.setAnzahlLfw(BigDecimal.valueOf(1));
        interval.setAnzahlKrad(BigDecimal.valueOf(1));
        interval.setAnzahlLkw(BigDecimal.valueOf(1));
        interval.setAnzahlBus(BigDecimal.valueOf(1));
        interval.setAnzahlRad(BigDecimal.valueOf(1));
        interval.setSummeAllePkw(BigDecimal.valueOf(1));
        interval.setSummeLastzug(BigDecimal.valueOf(1));
        interval.setSummeGueterverkehr(BigDecimal.valueOf(9));
        interval.setSummeSchwerverkehr(BigDecimal.valueOf(9));
        interval.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(9));
        intervals.add(interval);

        interval = new IntervalDto();
        interval.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(22, 0)));
        interval.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(22, 30)));
        interval.setAnzahlLfw(BigDecimal.valueOf(1));
        interval.setAnzahlKrad(BigDecimal.valueOf(1));
        interval.setAnzahlLkw(BigDecimal.valueOf(1));
        interval.setAnzahlBus(BigDecimal.valueOf(1));
        interval.setAnzahlRad(BigDecimal.valueOf(1));
        interval.setSummeAllePkw(BigDecimal.valueOf(1));
        interval.setSummeLastzug(BigDecimal.valueOf(1));
        interval.setSummeGueterverkehr(BigDecimal.valueOf(9));
        interval.setSummeSchwerverkehr(BigDecimal.valueOf(9));
        interval.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(9));
        intervals.add(interval);

        interval = new IntervalDto();
        interval.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(22, 30)));
        interval.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(23, 0)));
        interval.setAnzahlLfw(BigDecimal.valueOf(1));
        interval.setAnzahlKrad(BigDecimal.valueOf(1));
        interval.setAnzahlLkw(BigDecimal.valueOf(1));
        interval.setAnzahlBus(BigDecimal.valueOf(1));
        interval.setAnzahlRad(BigDecimal.valueOf(1));
        interval.setSummeAllePkw(BigDecimal.valueOf(1));
        interval.setSummeLastzug(BigDecimal.valueOf(1));
        interval.setSummeGueterverkehr(BigDecimal.valueOf(9));
        interval.setSummeSchwerverkehr(BigDecimal.valueOf(9));
        interval.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(9));
        intervals.add(interval);

        interval = new IntervalDto();
        interval.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(23, 0)));
        interval.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(23, 30)));
        interval.setAnzahlLfw(BigDecimal.valueOf(1));
        interval.setAnzahlKrad(BigDecimal.valueOf(1));
        interval.setAnzahlLkw(BigDecimal.valueOf(1));
        interval.setAnzahlBus(BigDecimal.valueOf(1));
        interval.setAnzahlRad(BigDecimal.valueOf(1));
        interval.setSummeAllePkw(BigDecimal.valueOf(1));
        interval.setSummeLastzug(BigDecimal.valueOf(1));
        interval.setSummeGueterverkehr(BigDecimal.valueOf(9));
        interval.setSummeSchwerverkehr(BigDecimal.valueOf(9));
        interval.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(9));
        intervals.add(interval);

        interval = new IntervalDto();
        interval.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(23, 30)));
        interval.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(23, 59, 59)));
        interval.setAnzahlLfw(BigDecimal.valueOf(1));
        interval.setAnzahlKrad(BigDecimal.valueOf(1));
        interval.setAnzahlLkw(BigDecimal.valueOf(1));
        interval.setAnzahlBus(BigDecimal.valueOf(1));
        interval.setAnzahlRad(BigDecimal.valueOf(1));
        interval.setSummeAllePkw(BigDecimal.valueOf(1));
        interval.setSummeLastzug(BigDecimal.valueOf(1));
        interval.setSummeGueterverkehr(BigDecimal.valueOf(9));
        interval.setSummeSchwerverkehr(BigDecimal.valueOf(9));
        interval.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(9));
        intervals.add(interval);

        final var options = new MessstelleOptionsDTO();
        options.setZeitauswahl("Tageswert");
        options.setIntervall(ZaehldatenIntervall.STUNDE_HALB);
        options.setStundensumme(true);
        options.setBlocksumme(true);
        options.setSpitzenstunde(true);
        options.setTagessumme(true);
        options.setZeitblock(Zeitblock.ZB_00_24);
        final var result = listenausgabeService.ladeListenausgabe(intervals, true, options);

        final var expectedNumberOfIntervals =
                // Halbstunden Intervalle für 24h
                48
                        // Stundensummen
                        + 24
                        // Blocksummen
                        + 5
                        // Tagessumme
                        + 1
                        // Spitzenstunde Tag
                        + 1
                        // Spitzenstunde Block
                        + 5;

        Assertions.assertThat(result.getZaehldaten().size()).isEqualTo(expectedNumberOfIntervals);
    }

    @Test
    void ladeListenausgabeZeitauswahlBlockWithStundensummeAndIntervallHalbeStundeAndSpitzenstundeAndBlocksumme() {
        final var intervals = new ArrayList<IntervalDto>();

        var interval = new IntervalDto();
        interval.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(6, 0)));
        interval.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(6, 30)));
        interval.setAnzahlLfw(BigDecimal.valueOf(1));
        interval.setAnzahlKrad(BigDecimal.valueOf(1));
        interval.setAnzahlLkw(BigDecimal.valueOf(1));
        interval.setAnzahlBus(BigDecimal.valueOf(1));
        interval.setAnzahlRad(BigDecimal.valueOf(1));
        interval.setSummeAllePkw(BigDecimal.valueOf(1));
        interval.setSummeLastzug(BigDecimal.valueOf(1));
        interval.setSummeGueterverkehr(BigDecimal.valueOf(9));
        interval.setSummeSchwerverkehr(BigDecimal.valueOf(9));
        interval.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(9));
        intervals.add(interval);

        interval = new IntervalDto();
        interval.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(6, 30)));
        interval.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(7, 0)));
        interval.setAnzahlLfw(BigDecimal.valueOf(1));
        interval.setAnzahlKrad(BigDecimal.valueOf(1));
        interval.setAnzahlLkw(BigDecimal.valueOf(1));
        interval.setAnzahlBus(BigDecimal.valueOf(1));
        interval.setAnzahlRad(BigDecimal.valueOf(1));
        interval.setSummeAllePkw(BigDecimal.valueOf(1));
        interval.setSummeLastzug(BigDecimal.valueOf(1));
        interval.setSummeGueterverkehr(BigDecimal.valueOf(9));
        interval.setSummeSchwerverkehr(BigDecimal.valueOf(9));
        interval.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(9));
        intervals.add(interval);

        interval = new IntervalDto();
        interval.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(7, 0)));
        interval.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(7, 30)));
        interval.setAnzahlLfw(BigDecimal.valueOf(1));
        interval.setAnzahlKrad(BigDecimal.valueOf(1));
        interval.setAnzahlLkw(BigDecimal.valueOf(1));
        interval.setAnzahlBus(BigDecimal.valueOf(1));
        interval.setAnzahlRad(BigDecimal.valueOf(1));
        interval.setSummeAllePkw(BigDecimal.valueOf(1));
        interval.setSummeLastzug(BigDecimal.valueOf(1));
        interval.setSummeGueterverkehr(BigDecimal.valueOf(9));
        interval.setSummeSchwerverkehr(BigDecimal.valueOf(9));
        interval.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(9));
        intervals.add(interval);

        interval = new IntervalDto();
        interval.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(7, 30)));
        interval.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(8, 0)));
        interval.setAnzahlLfw(BigDecimal.valueOf(1));
        interval.setAnzahlKrad(BigDecimal.valueOf(1));
        interval.setAnzahlLkw(BigDecimal.valueOf(1));
        interval.setAnzahlBus(BigDecimal.valueOf(1));
        interval.setAnzahlRad(BigDecimal.valueOf(1));
        interval.setSummeAllePkw(BigDecimal.valueOf(1));
        interval.setSummeLastzug(BigDecimal.valueOf(1));
        interval.setSummeGueterverkehr(BigDecimal.valueOf(9));
        interval.setSummeSchwerverkehr(BigDecimal.valueOf(9));
        interval.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(9));
        intervals.add(interval);

        interval = new IntervalDto();
        interval.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(8, 0)));
        interval.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(8, 30)));
        interval.setAnzahlLfw(BigDecimal.valueOf(1));
        interval.setAnzahlKrad(BigDecimal.valueOf(1));
        interval.setAnzahlLkw(BigDecimal.valueOf(1));
        interval.setAnzahlBus(BigDecimal.valueOf(1));
        interval.setAnzahlRad(BigDecimal.valueOf(1));
        interval.setSummeAllePkw(BigDecimal.valueOf(1));
        interval.setSummeLastzug(BigDecimal.valueOf(1));
        interval.setSummeGueterverkehr(BigDecimal.valueOf(9));
        interval.setSummeSchwerverkehr(BigDecimal.valueOf(9));
        interval.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(9));
        intervals.add(interval);

        interval = new IntervalDto();
        interval.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(8, 30)));
        interval.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(9, 0)));
        interval.setAnzahlLfw(BigDecimal.valueOf(1));
        interval.setAnzahlKrad(BigDecimal.valueOf(1));
        interval.setAnzahlLkw(BigDecimal.valueOf(1));
        interval.setAnzahlBus(BigDecimal.valueOf(1));
        interval.setAnzahlRad(BigDecimal.valueOf(1));
        interval.setSummeAllePkw(BigDecimal.valueOf(1));
        interval.setSummeLastzug(BigDecimal.valueOf(1));
        interval.setSummeGueterverkehr(BigDecimal.valueOf(9));
        interval.setSummeSchwerverkehr(BigDecimal.valueOf(9));
        interval.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(9));
        intervals.add(interval);

        interval = new IntervalDto();
        interval.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(9, 0)));
        interval.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(9, 30)));
        interval.setAnzahlLfw(BigDecimal.valueOf(1));
        interval.setAnzahlKrad(BigDecimal.valueOf(1));
        interval.setAnzahlLkw(BigDecimal.valueOf(1));
        interval.setAnzahlBus(BigDecimal.valueOf(1));
        interval.setAnzahlRad(BigDecimal.valueOf(1));
        interval.setSummeAllePkw(BigDecimal.valueOf(1));
        interval.setSummeLastzug(BigDecimal.valueOf(1));
        interval.setSummeGueterverkehr(BigDecimal.valueOf(9));
        interval.setSummeSchwerverkehr(BigDecimal.valueOf(9));
        interval.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(9));
        intervals.add(interval);

        interval = new IntervalDto();
        interval.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(9, 30)));
        interval.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(10, 0)));
        interval.setAnzahlLfw(BigDecimal.valueOf(1));
        interval.setAnzahlKrad(BigDecimal.valueOf(1));
        interval.setAnzahlLkw(BigDecimal.valueOf(1));
        interval.setAnzahlBus(BigDecimal.valueOf(1));
        interval.setAnzahlRad(BigDecimal.valueOf(1));
        interval.setSummeAllePkw(BigDecimal.valueOf(1));
        interval.setSummeLastzug(BigDecimal.valueOf(1));
        interval.setSummeGueterverkehr(BigDecimal.valueOf(9));
        interval.setSummeSchwerverkehr(BigDecimal.valueOf(9));
        interval.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(9));
        intervals.add(interval);

        final var options = new MessstelleOptionsDTO();
        options.setZeitauswahl("Block");
        options.setIntervall(ZaehldatenIntervall.STUNDE_HALB);
        options.setStundensumme(true);
        options.setBlocksumme(true);
        options.setSpitzenstunde(true);
        options.setZeitblock(Zeitblock.ZB_06_10);
        final var result = listenausgabeService.ladeListenausgabe(intervals, true, options);

        final var expectedNumberOfIntervals =
                // Halbstunden Intervalle für 24h
                8
                        // Stundensummen
                        + 4
                        // Blocksummen
                        + 1
                        // Spitzenstunde Block
                        + 1;

        Assertions.assertThat(result.getZaehldaten().size()).isEqualTo(expectedNumberOfIntervals);
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
    void calculateSumOfIntervalsPerHourForLastThreeHoursOfDay() {
        final var interval0 = new IntervalDto();
        interval0.setAnzahlLkw(BigDecimal.valueOf(1));
        interval0.setDatumUhrzeitVon(LocalDateTime.of(2024, 1, 5, 22, 30, 0));
        interval0.setDatumUhrzeitBis(LocalDateTime.of(2024, 1, 5, 22, 45, 0));
        final var interval1 = new IntervalDto();
        interval1.setAnzahlLkw(BigDecimal.valueOf(2));
        interval1.setDatumUhrzeitVon(LocalDateTime.of(2024, 1, 5, 22, 45, 0));
        interval1.setDatumUhrzeitBis(LocalDateTime.of(2024, 1, 5, 23, 0, 0));
        final var interval2 = new IntervalDto();
        interval2.setAnzahlLkw(BigDecimal.valueOf(3));
        interval2.setDatumUhrzeitVon(LocalDateTime.of(2024, 1, 5, 23, 0, 0));
        interval2.setDatumUhrzeitBis(LocalDateTime.of(2024, 1, 5, 23, 15, 0));
        final var interval3 = new IntervalDto();
        interval3.setAnzahlLkw(BigDecimal.valueOf(4));
        interval3.setDatumUhrzeitVon(LocalDateTime.of(2024, 1, 5, 23, 15, 0));
        interval3.setDatumUhrzeitBis(LocalDateTime.of(2024, 1, 5, 23, 30, 0));
        final var interval4 = new IntervalDto();
        interval4.setAnzahlLkw(BigDecimal.valueOf(5));
        interval4.setDatumUhrzeitVon(LocalDateTime.of(2024, 1, 5, 23, 30, 0));
        interval4.setDatumUhrzeitBis(LocalDateTime.of(2024, 1, 5, 23, 45, 0));
        final var interval5 = new IntervalDto();
        interval5.setAnzahlLkw(BigDecimal.valueOf(6));
        interval5.setDatumUhrzeitVon(LocalDateTime.of(2024, 1, 5, 23, 45, 0));
        interval5.setDatumUhrzeitBis(LocalDateTime.of(2024, 1, 5, 23, 59, 59));

        var result = listenausgabeService.calculateSumOfIntervalsPerHour(List.of(interval0, interval1, interval2, interval3, interval4, interval5));

        final var expected0 = new LadeMesswerteDTO();
        expected0.setType("Stunde");
        expected0.setStartUhrzeit(Zeitblock.ZB_22_23.getStart().toLocalTime());
        expected0.setEndeUhrzeit(Zeitblock.ZB_22_23.getEnd().toLocalTime());
        expected0.setSortingIndex(51092092);
        expected0.setPkw(0);
        expected0.setLkw(3);
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
        expected1.setStartUhrzeit(Zeitblock.ZB_23_24.getStart().toLocalTime());
        expected1.setEndeUhrzeit(LocalTime.of(23, 59, 59));
        expected1.setSortingIndex(51095095);
        expected1.setPkw(0);
        expected1.setLkw(18);
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

        Assertions.assertThat(result)
                .isNotNull()
                .isNotEmpty()
                .isEqualTo(List.of(expected0, expected1));
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
    void getIntervalsWithinZeitblockForLastHourOfDay() {
        final var interval0 = new IntervalDto();
        interval0.setDatumUhrzeitVon(LocalDateTime.of(2024, 1, 5, 22, 30, 0));
        interval0.setDatumUhrzeitBis(LocalDateTime.of(2024, 1, 5, 22, 45, 0));
        final var interval1 = new IntervalDto();
        interval1.setDatumUhrzeitVon(LocalDateTime.of(2024, 1, 5, 22, 45, 0));
        interval1.setDatumUhrzeitBis(LocalDateTime.of(2024, 1, 5, 23, 0, 0));
        final var interval2 = new IntervalDto();
        interval2.setDatumUhrzeitVon(LocalDateTime.of(2024, 1, 5, 23, 0, 0));
        interval2.setDatumUhrzeitBis(LocalDateTime.of(2024, 1, 5, 23, 15, 0));
        final var interval3 = new IntervalDto();
        interval3.setDatumUhrzeitVon(LocalDateTime.of(2024, 1, 5, 23, 15, 0));
        interval3.setDatumUhrzeitBis(LocalDateTime.of(2024, 1, 5, 23, 30, 0));
        final var interval4 = new IntervalDto();
        interval4.setDatumUhrzeitVon(LocalDateTime.of(2024, 1, 5, 23, 30, 0));
        interval4.setDatumUhrzeitBis(LocalDateTime.of(2024, 1, 5, 23, 45, 0));
        final var interval5 = new IntervalDto();
        interval5.setDatumUhrzeitVon(LocalDateTime.of(2024, 1, 5, 23, 45, 0));
        interval5.setDatumUhrzeitBis(LocalDateTime.of(2024, 1, 5, 23, 59, 59));

        final var intervals = List.of(interval0, interval1, interval2, interval3, interval4, interval5);
        final var result = listenausgabeService.getIntervalsWithinZeitblock(intervals, Zeitblock.ZB_23_24);

        Assertions.assertThat(result)
                .isNotNull()
                .isNotEmpty()
                .isEqualTo(List.of(interval2, interval3, interval4, interval5));
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
