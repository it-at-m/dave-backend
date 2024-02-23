package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.dtos.laden.messwerte.LadeMesswerteDTO;
import de.muenchen.dave.geodateneai.gen.model.MeasurementValuesPerInterval;
import java.time.LocalTime;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

@Slf4j
class LadeMesswerteMapperTests {

    private final LadeMesswerteMapper mapper = new LadeMesswerteMapperImpl();

    @Test
    void testMeasurementValuesPerIntervalToLadeMesswerteDTO() {
        final MeasurementValuesPerInterval bean = new MeasurementValuesPerInterval();
        bean.setStartUhrzeit(LocalTime.MIN);
        bean.setEndeUhrzeit(LocalTime.MAX);
        bean.setAnzahlLfw(0);
        bean.setAnzahlKrad(1);
        bean.setAnzahlLkw(2);
        bean.setAnzahlBus(3);
        bean.setAnzahlRad(4);
        bean.setSummeAllePkw(5);
        bean.setSummeLastzug(6);
        bean.setSummeGueterverkehr(7);
        bean.setSummeSchwerverkehr(8);
        bean.setSummeKraftfahrzeugverkehr(9);
        bean.setProzentSchwerverkehr(1.1D);
        bean.setProzentGueterverkehr(2.2D);

        final LadeMesswerteDTO expected = new LadeMesswerteDTO();
        expected.setType("");
        expected.setStartUhrzeit(bean.getStartUhrzeit());
        expected.setEndeUhrzeit(bean.getEndeUhrzeit());
        expected.setPkw(bean.getSummeAllePkw());
        expected.setLkw(bean.getAnzahlLkw());
        expected.setLfw(bean.getAnzahlLfw());
        expected.setLastzuege(bean.getSummeLastzug());
        expected.setBusse(bean.getAnzahlBus());
        expected.setKraftraeder(bean.getAnzahlKrad());
        expected.setFahrradfahrer(bean.getAnzahlRad());
        expected.setKfz(bean.getSummeKraftfahrzeugverkehr());
        expected.setSchwerverkehr(bean.getSummeSchwerverkehr());
        expected.setGueterverkehr(bean.getSummeGueterverkehr());
        expected.setAnteilSchwerverkehrAnKfzProzent(bean.getProzentSchwerverkehr());
        expected.setAnteilGueterverkehrAnKfzProzent(bean.getProzentGueterverkehr());

        Assertions.assertThat(this.mapper.measurementValuesPerIntervalToLadeMesswerteDTO(bean))
                .isNotNull()
                .usingRecursiveComparison()
                .ignoringFields("sortingIndex", "fussgaenger")
                .isEqualTo(expected);
    }
}
