/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2023
 */
package de.muenchen.dave.util.messstelle;

import de.muenchen.dave.domain.dtos.laden.messwerte.LadeMesswerteDTO;
import de.muenchen.dave.geodateneai.gen.model.MeasurementValuesPerInterval;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class MesswerteBaseUtilTest {

    @Test
    void calculateSum() {
        final MeasurementValuesPerInterval interval1 = new MeasurementValuesPerInterval();
        interval1.setAnzahlLfw(1);
        interval1.setAnzahlKrad(2);
        interval1.setAnzahlLkw(3);
        interval1.setAnzahlBus(4);
        interval1.setAnzahlRad(5);
        interval1.setSummeAllePkw(6);
        interval1.setSummeLastzug(7);
        interval1.setSummeGueterverkehr(8);
        interval1.setSummeSchwerverkehr(9);
        interval1.setSummeKraftfahrzeugverkehr(10);
        interval1.setProzentSchwerverkehr(1.1D);
        interval1.setProzentGueterverkehr(2.2D);

        final MeasurementValuesPerInterval interval2 = new MeasurementValuesPerInterval();
        interval2.setAnzahlLfw(1);
        interval2.setAnzahlKrad(2);
        interval2.setAnzahlLkw(3);
        interval2.setAnzahlBus(4);
        interval2.setAnzahlRad(5);
        interval2.setSummeAllePkw(6);
        interval2.setSummeLastzug(7);
        interval2.setSummeGueterverkehr(8);
        interval2.setSummeSchwerverkehr(9);
        interval2.setSummeKraftfahrzeugverkehr(10);
        interval2.setProzentSchwerverkehr(1.1D);
        interval2.setProzentGueterverkehr(2.2D);

        final LadeMesswerteDTO expected = new LadeMesswerteDTO();
        expected.setPkw(interval1.getSummeAllePkw() + interval2.getSummeAllePkw());
        expected.setLkw(interval1.getAnzahlLkw() + interval2.getAnzahlLkw());
        expected.setLfw(interval1.getAnzahlLfw() + interval2.getAnzahlLfw());
        expected.setLastzuege(interval1.getSummeLastzug() + interval2.getSummeLastzug());
        expected.setBusse(interval1.getAnzahlBus() + interval2.getAnzahlBus());
        expected.setKraftraeder(interval1.getAnzahlKrad() + interval2.getAnzahlKrad());
        expected.setFahrradfahrer(interval1.getAnzahlRad() + interval2.getAnzahlRad());
        expected.setKfz(interval1.getSummeKraftfahrzeugverkehr() + interval2.getSummeKraftfahrzeugverkehr());
        expected.setSchwerverkehr(interval1.getSummeSchwerverkehr() + interval2.getSummeSchwerverkehr());
        expected.setGueterverkehr(interval1.getSummeGueterverkehr() + interval2.getSummeGueterverkehr());
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
