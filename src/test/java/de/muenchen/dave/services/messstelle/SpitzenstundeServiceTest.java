/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2023
 */
package de.muenchen.dave.services.messstelle;

import de.muenchen.dave.domain.dtos.laden.messwerte.LadeMesswerteDTO;
import de.muenchen.dave.domain.enums.ZaehldatenIntervall;
import de.muenchen.dave.domain.enums.Zeitblock;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import de.muenchen.dave.geodateneai.gen.model.IntervalDto;
import de.muenchen.dave.util.messstelle.MesswerteSortingIndexUtil;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

@Slf4j
class SpitzenstundeServiceTest {

    private final SpitzenstundeService spitzenstundeService = new SpitzenstundeService();

    @Test
    void calculateSpitzenstundeStundeViertel() {
        int index = 0;

        final IntervalDto interval0 = new IntervalDto();
        interval0.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 0)));
        interval0.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 15)));
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
        interval1.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 15)));
        interval1.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 30)));
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
        interval2.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 30)));
        interval2.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 45)));
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
        interval3.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 45)));
        interval3.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(1, 0)));
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
        interval4.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(1, 0)));
        interval4.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(1, 15)));
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
        interval5.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(1, 15)));
        interval5.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(1, 30)));
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
        interval6.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(1, 30)));
        interval6.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(1, 45)));
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

        LadeMesswerteDTO result = spitzenstundeService.calculateSpitzenstunde(
                Zeitblock.ZB_00_24,
                List.of(interval0, interval1, interval2, interval3, interval4, interval5, interval6),
                true,
                ZaehldatenIntervall.STUNDE_VIERTEL);
        Assertions.assertThat(result.getType())
                .isNotNull().isEqualTo(SpitzenstundeService.SPITZENSTUNDE + SpitzenstundeService.TAG + " " + SpitzenstundeService.KFZ);
        Assertions.assertThat(result.getStartUhrzeit())
                .isNotNull().isEqualTo(interval2.getDatumUhrzeitVon().toLocalTime());
        Assertions.assertThat(result.getEndeUhrzeit())
                .isNotNull().isEqualTo(interval5.getDatumUhrzeitBis().toLocalTime());
        Assertions.assertThat(result.getKfz())
                .isNotNull().isEqualTo(54);

        result = spitzenstundeService.calculateSpitzenstunde(
                Zeitblock.ZB_00_06,
                List.of(interval0, interval1, interval2, interval3, interval4, interval5, interval6),
                false,
                ZaehldatenIntervall.STUNDE_VIERTEL);
        Assertions.assertThat(result.getStartUhrzeit())
                .isNotNull().isEqualTo(interval2.getDatumUhrzeitVon().toLocalTime());
        Assertions.assertThat(result.getEndeUhrzeit())
                .isNotNull().isEqualTo(interval5.getDatumUhrzeitBis().toLocalTime());
        Assertions.assertThat(result.getType())
                .isNotNull().isEqualTo(SpitzenstundeService.SPITZENSTUNDE + SpitzenstundeService.BLOCK + " " + SpitzenstundeService.RAD);
        Assertions.assertThat(result.getFahrradfahrer())
                .isNotNull().isEqualTo(34);
    }

    @Test
    void calculateSpitzenstundeStundeHalb() {
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

        LadeMesswerteDTO result = spitzenstundeService.calculateSpitzenstunde(
                Zeitblock.ZB_00_24,
                List.of(interval0, interval1, interval2, interval3, interval4, interval5, interval6),
                true,
                ZaehldatenIntervall.STUNDE_HALB);
        Assertions.assertThat(result.getType())
                .isNotNull().isEqualTo(SpitzenstundeService.SPITZENSTUNDE + SpitzenstundeService.TAG + " " + SpitzenstundeService.KFZ);
        Assertions.assertThat(result.getStartUhrzeit())
                .isNotNull().isEqualTo(interval4.getDatumUhrzeitVon().toLocalTime());
        Assertions.assertThat(result.getEndeUhrzeit())
                .isNotNull().isEqualTo(interval5.getDatumUhrzeitBis().toLocalTime());
        Assertions.assertThat(result.getKfz())
                .isNotNull().isEqualTo(29);

        result = spitzenstundeService.calculateSpitzenstunde(
                Zeitblock.ZB_00_06,
                List.of(interval0, interval1, interval2, interval3, interval4, interval5, interval6),
                false,
                ZaehldatenIntervall.STUNDE_HALB);
        Assertions.assertThat(result.getStartUhrzeit())
                .isNotNull().isEqualTo(interval4.getDatumUhrzeitVon().toLocalTime());
        Assertions.assertThat(result.getEndeUhrzeit())
                .isNotNull().isEqualTo(interval5.getDatumUhrzeitBis().toLocalTime());
        Assertions.assertThat(result.getType())
                .isNotNull().isEqualTo(SpitzenstundeService.SPITZENSTUNDE + SpitzenstundeService.BLOCK + " " + SpitzenstundeService.RAD);
        Assertions.assertThat(result.getFahrradfahrer())
                .isNotNull().isEqualTo(19);
    }

    @Test
    void calculateSpitzenstundeStundeKomplett() {
        int index = 0;

        final IntervalDto interval0 = new IntervalDto();
        interval0.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 0)));
        interval0.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(1, 0)));
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
        interval1.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(1, 0)));
        interval1.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(2, 0)));
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
        interval2.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(2, 0)));
        interval2.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(3, 0)));
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
        interval3.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(3, 0)));
        interval3.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(4, 0)));
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
        interval4.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(4, 0)));
        interval4.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(5, 0)));
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
        interval5.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(5, 0)));
        interval5.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(6, 0)));
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
        interval6.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(6, 0)));
        interval6.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(7, 0)));
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

        LadeMesswerteDTO result = spitzenstundeService.calculateSpitzenstunde(
                Zeitblock.ZB_00_24,
                List.of(interval0, interval1, interval2, interval3, interval4, interval5, interval6),
                true,
                ZaehldatenIntervall.STUNDE_KOMPLETT);
        Assertions.assertThat(result.getType())
                .isNotNull().isEqualTo(SpitzenstundeService.SPITZENSTUNDE + SpitzenstundeService.TAG + " " + SpitzenstundeService.KFZ);
        Assertions.assertThat(result.getStartUhrzeit())
                .isNotNull().isEqualTo(interval5.getDatumUhrzeitVon().toLocalTime());
        Assertions.assertThat(result.getEndeUhrzeit())
                .isNotNull().isEqualTo(interval5.getDatumUhrzeitBis().toLocalTime());
        Assertions.assertThat(result.getKfz())
                .isNotNull().isEqualTo(15);

        result = spitzenstundeService.calculateSpitzenstunde(
                Zeitblock.ZB_00_06,
                List.of(interval0, interval1, interval2, interval3, interval4, interval5, interval6),
                false,
                ZaehldatenIntervall.STUNDE_KOMPLETT);
        Assertions.assertThat(result.getStartUhrzeit())
                .isNotNull().isEqualTo(interval5.getDatumUhrzeitVon().toLocalTime());
        Assertions.assertThat(result.getEndeUhrzeit())
                .isNotNull().isEqualTo(interval5.getDatumUhrzeitBis().toLocalTime());
        Assertions.assertThat(result.getType())
                .isNotNull().isEqualTo(SpitzenstundeService.SPITZENSTUNDE + SpitzenstundeService.BLOCK + " " + SpitzenstundeService.RAD);
        Assertions.assertThat(result.getFahrradfahrer())
                .isNotNull().isEqualTo(10);
    }

    @Test
    void getIntervalsOfSpitzenstundeStundeViertel() {
        int index = 0;

        final IntervalDto interval0 = new IntervalDto();
        interval0.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 0)));
        interval0.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 15)));
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
        interval1.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 15)));
        interval1.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 30)));
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
        interval2.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 30)));
        interval2.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 45)));
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
        interval3.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 45)));
        interval3.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(1, 0)));
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
        interval4.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(1, 0)));
        interval4.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(1, 15)));
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
        interval5.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(1, 15)));
        interval5.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(1, 30)));
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
        interval6.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(1, 30)));
        interval6.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(1, 45)));
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

        List<IntervalDto> result = spitzenstundeService
                .getIntervalsOfSpitzenstunde(List.of(interval0, interval1, interval2, interval3, interval4, interval5, interval6), true,
                        ZaehldatenIntervall.STUNDE_VIERTEL);
        Assertions.assertThat(result)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(List.of(interval2, interval3, interval4, interval5));

        result = spitzenstundeService.getIntervalsOfSpitzenstunde(List.of(interval0, interval1, interval2, interval3, interval4, interval5, interval6), false,
                ZaehldatenIntervall.STUNDE_VIERTEL);
        Assertions.assertThat(result)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(List.of(interval2, interval3, interval4, interval5));
    }

    @Test
    void getIntervalsOfSpitzenstundeStundeHalb() {
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

        List<IntervalDto> result = spitzenstundeService
                .getIntervalsOfSpitzenstunde(List.of(interval0, interval1, interval2, interval3, interval4, interval5, interval6), true,
                        ZaehldatenIntervall.STUNDE_HALB);
        Assertions.assertThat(result)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(List.of(interval4, interval5));

        result = spitzenstundeService.getIntervalsOfSpitzenstunde(List.of(interval0, interval1, interval2, interval3, interval4, interval5, interval6), false,
                ZaehldatenIntervall.STUNDE_HALB);
        Assertions.assertThat(result)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(List.of(interval4, interval5));
    }

    @Test
    void getIntervalsOfSpitzenstundeStundeKomplett() {
        int index = 0;

        final IntervalDto interval0 = new IntervalDto();
        interval0.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 0)));
        interval0.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(1, 0)));
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
        interval1.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(1, 0)));
        interval1.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(2, 0)));
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
        interval2.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(2, 0)));
        interval2.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(3, 0)));
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
        interval3.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(3, 0)));
        interval3.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(4, 0)));
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
        interval4.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(4, 0)));
        interval4.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(5, 0)));
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
        interval5.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(5, 0)));
        interval5.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(6, 0)));
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
        interval6.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.of(6, 0)));
        interval6.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.of(7, 0)));
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

        List<IntervalDto> result = spitzenstundeService
                .getIntervalsOfSpitzenstunde(List.of(interval0, interval1, interval2, interval3, interval4, interval5, interval6), true,
                        ZaehldatenIntervall.STUNDE_KOMPLETT);
        Assertions.assertThat(result)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(List.of(interval5));

        result = spitzenstundeService.getIntervalsOfSpitzenstunde(List.of(interval0, interval1, interval2, interval3, interval4, interval5, interval6), false,
                ZaehldatenIntervall.STUNDE_KOMPLETT);
        Assertions.assertThat(result)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(List.of(interval5));
    }

    @Test
    void getType() {
        Assertions.assertThat(spitzenstundeService.getType(true, Zeitblock.ZB_00_24))
                .isNotNull().isEqualTo("SpStdTag KFZ");

        Assertions.assertThat(spitzenstundeService.getType(false, Zeitblock.ZB_00_24))
                .isNotNull().isEqualTo("SpStdTag Rad");

        Assertions.assertThat(spitzenstundeService.getType(true, Zeitblock.ZB_00_06))
                .isNotNull().isEqualTo("SpStdBlock KFZ");

        Assertions.assertThat(spitzenstundeService.getType(false, Zeitblock.ZB_06_10))
                .isNotNull().isEqualTo("SpStdBlock Rad");
    }

    @Test
    void getSortingIndex() {
        var result = spitzenstundeService.getSortingIndex(true, Zeitblock.ZB_00_24,  null);
        Assertions.assertThat(result).isNotNull().isEqualTo(MesswerteSortingIndexUtil.SORTING_INDEX_SPITZEN_STUNDE_DAY_KFZ);

        result = spitzenstundeService.getSortingIndex(false, Zeitblock.ZB_00_24,  null);
        Assertions.assertThat(result).isNotNull().isEqualTo(MesswerteSortingIndexUtil.SORTING_INDEX_SPITZEN_STUNDE_DAY_RAD);

        final var spitzenStunde = new LadeMesswerteDTO();
        spitzenStunde.setStartUhrzeit(LocalTime.of(6,15,0));
        spitzenStunde.setEndeUhrzeit(LocalTime.of(6,30,0));
        result = spitzenstundeService.getSortingIndex(true, null,  spitzenStunde);
        Assertions
                .assertThat(result)
                .isNotNull()
                .isEqualTo(MesswerteSortingIndexUtil.SORTING_INDEX_ZB_06_10 + MesswerteSortingIndexUtil.SORTING_INDEX_SECOND_SPITZEN_STUNDE_KFZ);

        result = spitzenstundeService.getSortingIndex(false, null,  spitzenStunde);
        Assertions
                .assertThat(result)
                .isNotNull()
                .isEqualTo(MesswerteSortingIndexUtil.SORTING_INDEX_ZB_06_10 + MesswerteSortingIndexUtil.SORTING_INDEX_SECOND_SPITZEN_STUNDE_RAD);
    }

    @Test
    void getSortingIndexSpitzenStundeCompleteDay() {
        var result = spitzenstundeService.getSortingIndexSpitzenStundeCompleteDay(true);
        Assertions.assertThat(result).isNotNull().isEqualTo(MesswerteSortingIndexUtil.SORTING_INDEX_SPITZEN_STUNDE_DAY_KFZ);

        result = spitzenstundeService.getSortingIndexSpitzenStundeCompleteDay(false);
        Assertions.assertThat(result).isNotNull().isEqualTo(MesswerteSortingIndexUtil.SORTING_INDEX_SPITZEN_STUNDE_DAY_RAD);
    }

    @Test
    void getSortingIndexSpitzenStundeWithinBlock() {
        var result = spitzenstundeService.getSortingIndexSpitzenStundeWithinBlock(true);
        Assertions.assertThat(result).isNotNull().isEqualTo(MesswerteSortingIndexUtil.SORTING_INDEX_SECOND_SPITZEN_STUNDE_KFZ);

        result = spitzenstundeService.getSortingIndexSpitzenStundeWithinBlock(false);
        Assertions.assertThat(result).isNotNull().isEqualTo(MesswerteSortingIndexUtil.SORTING_INDEX_SECOND_SPITZEN_STUNDE_RAD);
    }

    @Test
    void isValueToCheckAgainstCurrentSpitzenstundeLarger() {
        final var valueToCheck = new LadeMesswerteDTO();
        valueToCheck.setKfz(1000);
        valueToCheck.setFahrradfahrer(900);
        final var currentSpitzenstunde = new LadeMesswerteDTO();
        currentSpitzenstunde.setKfz(999);
        currentSpitzenstunde.setFahrradfahrer(899);
        var result = spitzenstundeService.isValueToCheckAgainstCurrentSpitzenstundeLarger(true, currentSpitzenstunde, valueToCheck);
        Assertions.assertThat(result).isNotNull().isTrue();
        result = spitzenstundeService.isValueToCheckAgainstCurrentSpitzenstundeLarger(false, currentSpitzenstunde, valueToCheck);
        Assertions.assertThat(result).isNotNull().isTrue();

        valueToCheck.setKfz(1000);
        valueToCheck.setFahrradfahrer(900);
        currentSpitzenstunde.setKfz(1000);
        currentSpitzenstunde.setFahrradfahrer(900);
        result = spitzenstundeService.isValueToCheckAgainstCurrentSpitzenstundeLarger(true, currentSpitzenstunde, valueToCheck);
        Assertions.assertThat(result).isNotNull().isFalse();
        result = spitzenstundeService.isValueToCheckAgainstCurrentSpitzenstundeLarger(false, currentSpitzenstunde, valueToCheck);
        Assertions.assertThat(result).isNotNull().isFalse();

        valueToCheck.setKfz(999);
        valueToCheck.setFahrradfahrer(899);
        currentSpitzenstunde.setKfz(1000);
        currentSpitzenstunde.setFahrradfahrer(900);
        result = spitzenstundeService.isValueToCheckAgainstCurrentSpitzenstundeLarger(true, currentSpitzenstunde, valueToCheck);
        Assertions.assertThat(result).isNotNull().isFalse();
        result = spitzenstundeService.isValueToCheckAgainstCurrentSpitzenstundeLarger(false, currentSpitzenstunde, valueToCheck);
        Assertions.assertThat(result).isNotNull().isFalse();
    }

    @Test
    void isNewValueLarger() {
        Assertions.assertThat(spitzenstundeService.isNewValueLarger(1, 2))
                .isEqualTo(true);
        Assertions.assertThat(spitzenstundeService.isNewValueLarger(3, 2))
                .isEqualTo(false);
        Assertions.assertThat(spitzenstundeService.isNewValueLarger(null, 2))
                .isEqualTo(true);
    }
}
