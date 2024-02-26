/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2023
 */
package de.muenchen.dave.services.messstelle;

import de.muenchen.dave.domain.dtos.laden.messwerte.LadeMesswerteDTO;
import de.muenchen.dave.domain.enums.Zeitblock;
import de.muenchen.dave.geodateneai.gen.model.MeasurementValuesPerInterval;
import java.time.LocalTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

@Slf4j
public class SpitzenstundeServiceTest {

    private SpitzenstundeService spitzenstundeService = new SpitzenstundeService();

    @Test
    void calculateSpitzenstunde() {
        int index = 0;

        final MeasurementValuesPerInterval interval0 = new MeasurementValuesPerInterval();
        interval0.setStartUhrzeit(LocalTime.of(0, 0));
        interval0.setEndeUhrzeit(LocalTime.of(0, 15));
        interval0.setAnzahlLfw(1 + index);
        interval0.setAnzahlKrad(2 + index);
        interval0.setAnzahlLkw(3 + index);
        interval0.setAnzahlBus(4 + index);
        interval0.setAnzahlRad(5 + index);
        interval0.setSummeAllePkw(6 + index);
        interval0.setSummeLastzug(7 + index);
        interval0.setSummeGueterverkehr(8 + index);
        interval0.setSummeSchwerverkehr(9 + index);
        interval0.setSummeKraftfahrzeugverkehr(10 + index);

        final MeasurementValuesPerInterval interval1 = new MeasurementValuesPerInterval();
        index++;
        interval1.setStartUhrzeit(LocalTime.of(0, 15));
        interval1.setEndeUhrzeit(LocalTime.of(0, 30));
        interval1.setAnzahlLfw(1 + index);
        interval1.setAnzahlKrad(2 + index);
        interval1.setAnzahlLkw(3 + index);
        interval1.setAnzahlBus(4 + index);
        interval1.setAnzahlRad(5 + index);
        interval1.setSummeAllePkw(6 + index);
        interval1.setSummeLastzug(7 + index);
        interval1.setSummeGueterverkehr(8 + index);
        interval1.setSummeSchwerverkehr(9 + index);
        interval1.setSummeKraftfahrzeugverkehr(10 + index);

        final MeasurementValuesPerInterval interval2 = new MeasurementValuesPerInterval();
        index++;
        interval2.setStartUhrzeit(LocalTime.of(0, 30));
        interval2.setEndeUhrzeit(LocalTime.of(0, 45));
        interval2.setAnzahlLfw(1 + index);
        interval2.setAnzahlKrad(2 + index);
        interval2.setAnzahlLkw(3 + index);
        interval2.setAnzahlBus(4 + index);
        interval2.setAnzahlRad(5 + index);
        interval2.setSummeAllePkw(6 + index);
        interval2.setSummeLastzug(7 + index);
        interval2.setSummeGueterverkehr(8 + index);
        interval2.setSummeSchwerverkehr(9 + index);
        interval2.setSummeKraftfahrzeugverkehr(10 + index);

        final MeasurementValuesPerInterval interval3 = new MeasurementValuesPerInterval();
        index++;
        interval3.setStartUhrzeit(LocalTime.of(0, 45));
        interval3.setEndeUhrzeit(LocalTime.of(1, 0));
        interval3.setAnzahlLfw(1 + index);
        interval3.setAnzahlKrad(2 + index);
        interval3.setAnzahlLkw(3 + index);
        interval3.setAnzahlBus(4 + index);
        interval3.setAnzahlRad(5 + index);
        interval3.setSummeAllePkw(6 + index);
        interval3.setSummeLastzug(7 + index);
        interval3.setSummeGueterverkehr(8 + index);
        interval3.setSummeSchwerverkehr(9 + index);
        interval3.setSummeKraftfahrzeugverkehr(10 + index);

        final MeasurementValuesPerInterval interval4 = new MeasurementValuesPerInterval();
        index++;
        interval4.setStartUhrzeit(LocalTime.of(1, 0));
        interval4.setEndeUhrzeit(LocalTime.of(1, 15));
        interval4.setAnzahlLfw(1 + index);
        interval4.setAnzahlKrad(2 + index);
        interval4.setAnzahlLkw(3 + index);
        interval4.setAnzahlBus(4 + index);
        interval4.setAnzahlRad(5 + index);
        interval4.setSummeAllePkw(6 + index);
        interval4.setSummeLastzug(7 + index);
        interval4.setSummeGueterverkehr(8 + index);
        interval4.setSummeSchwerverkehr(9 + index);
        interval4.setSummeKraftfahrzeugverkehr(10 + index);

        final MeasurementValuesPerInterval interval5 = new MeasurementValuesPerInterval();
        index++;
        interval5.setStartUhrzeit(LocalTime.of(1, 15));
        interval5.setEndeUhrzeit(LocalTime.of(1, 30));
        interval5.setAnzahlLfw(1 + index);
        interval5.setAnzahlKrad(2 + index);
        interval5.setAnzahlLkw(3 + index);
        interval5.setAnzahlBus(4 + index);
        interval5.setAnzahlRad(5 + index);
        interval5.setSummeAllePkw(6 + index);
        interval5.setSummeLastzug(7 + index);
        interval5.setSummeGueterverkehr(8 + index);
        interval5.setSummeSchwerverkehr(9 + index);
        interval5.setSummeKraftfahrzeugverkehr(10 + index);

        final MeasurementValuesPerInterval interval6 = new MeasurementValuesPerInterval();
        interval6.setStartUhrzeit(LocalTime.of(1, 30));
        interval6.setEndeUhrzeit(LocalTime.of(1, 45));
        interval6.setAnzahlLfw(0);
        interval6.setAnzahlKrad(0);
        interval6.setAnzahlLkw(0);
        interval6.setAnzahlBus(0);
        interval6.setAnzahlRad(0);
        interval6.setSummeAllePkw(0);
        interval6.setSummeLastzug(0);
        interval6.setSummeGueterverkehr(0);
        interval6.setSummeSchwerverkehr(0);
        interval6.setSummeKraftfahrzeugverkehr(0);

        LadeMesswerteDTO result = spitzenstundeService.calculateSpitzenstunde(Zeitblock.ZB_00_24,
                List.of(interval0, interval1, interval2, interval3, interval4, interval5, interval6), true);
        Assertions.assertThat(result.getType())
                .isNotNull().isEqualTo(SpitzenstundeService.SPITZENSTUNDE_TAG_KFZ);
        Assertions.assertThat(result.getStartUhrzeit())
                .isNotNull().isEqualTo(interval2.getStartUhrzeit());
        Assertions.assertThat(result.getEndeUhrzeit())
                .isNotNull().isEqualTo(interval5.getEndeUhrzeit());

        result = spitzenstundeService.calculateSpitzenstunde(Zeitblock.ZB_00_06,
                List.of(interval0, interval1, interval2, interval3, interval4, interval5, interval6), false);
        Assertions.assertThat(result.getStartUhrzeit())
                .isNotNull().isEqualTo(interval2.getStartUhrzeit());
        Assertions.assertThat(result.getEndeUhrzeit())
                .isNotNull().isEqualTo(interval5.getEndeUhrzeit());
        Assertions.assertThat(result.getType())
                .isNotNull().isEqualTo(SpitzenstundeService.SPITZENSTUNDE_BLOCK_RAD);
    }
}
