package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.dtos.laden.messwerte.LadeMesswerteDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import de.muenchen.dave.geodateneai.gen.model.IntervalDto;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

@Slf4j
class LadeMesswerteMapperTests {

    private final LadeMesswerteMapper mapper = new LadeMesswerteMapperImpl();

    @Test
    void testMeasurementValuesPerIntervalToLadeMesswerteDTO() {
        final IntervalDto bean = new IntervalDto();
        bean.setDatumUhrzeitVon(LocalDateTime.of(LocalDate.now(), LocalTime.MIN));
        bean.setDatumUhrzeitBis(LocalDateTime.of(LocalDate.now(), LocalTime.MAX));
        bean.setAnzahlLfw(BigDecimal.valueOf(0));
        bean.setAnzahlKrad(BigDecimal.valueOf(1));
        bean.setAnzahlLkw(BigDecimal.valueOf(2));
        bean.setAnzahlBus(BigDecimal.valueOf(3));
        bean.setAnzahlRad(BigDecimal.valueOf(4));
        bean.setSummeAllePkw(BigDecimal.valueOf(5));
        bean.setSummeLastzug(BigDecimal.valueOf(6));
        bean.setSummeGueterverkehr(BigDecimal.valueOf(7));
        bean.setSummeSchwerverkehr(BigDecimal.valueOf(8));
        bean.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(9));
        bean.setProzentSchwerverkehr(BigDecimal.valueOf(1.1D));
        bean.setProzentGueterverkehr(BigDecimal.valueOf(2.2D));

        final LadeMesswerteDTO expected = new LadeMesswerteDTO();
        expected.setType("");
        expected.setStartUhrzeit(bean.getDatumUhrzeitVon().toLocalTime());
        expected.setEndeUhrzeit(bean.getDatumUhrzeitBis().toLocalTime());
        expected.setPkw(bean.getSummeAllePkw().intValue());
        expected.setLkw(bean.getAnzahlLkw().intValue());
        expected.setLfw(bean.getAnzahlLfw().intValue());
        expected.setLastzuege(bean.getSummeLastzug().intValue());
        expected.setBusse(bean.getAnzahlBus().intValue());
        expected.setKraftraeder(bean.getAnzahlKrad().intValue());
        expected.setFahrradfahrer(bean.getAnzahlRad().intValue());
        expected.setKfz(bean.getSummeKraftfahrzeugverkehr().intValue());
        expected.setSchwerverkehr(bean.getSummeSchwerverkehr().intValue());
        expected.setGueterverkehr(bean.getSummeGueterverkehr().intValue());
        expected.setAnteilSchwerverkehrAnKfzProzent(bean.getProzentSchwerverkehr().doubleValue());
        expected.setAnteilGueterverkehrAnKfzProzent(bean.getProzentGueterverkehr().doubleValue());

        Assertions.assertThat(this.mapper.interval2LadeMesswerte(bean))
                .isNotNull()
                .usingRecursiveComparison()
                .ignoringFields("sortingIndex", "fussgaenger")
                .isEqualTo(expected);
    }
}
