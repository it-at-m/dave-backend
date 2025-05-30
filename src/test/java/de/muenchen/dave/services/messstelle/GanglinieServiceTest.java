package de.muenchen.dave.services.messstelle;

import de.muenchen.dave.domain.dtos.laden.LadeZaehldatenSteplineDTO;
import de.muenchen.dave.domain.dtos.laden.StepLineSeriesEntryBaseDTO;
import de.muenchen.dave.domain.dtos.laden.StepLineSeriesEntryBigDecimalDTO;
import de.muenchen.dave.domain.dtos.laden.StepLineSeriesEntryIntegerDTO;
import de.muenchen.dave.domain.dtos.messstelle.FahrzeugOptionsDTO;
import de.muenchen.dave.geodateneai.gen.model.IntervalDto;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GanglinieServiceTest {

    private GanglinieService ganglinieService;

    @BeforeEach
    public void beforeEach() {
        ganglinieService = new GanglinieService();
    }

    @Test
    void ladeGanglinie() {
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
        interval0.setProzentGueterverkehr(BigDecimal.valueOf(11));
        interval0.setProzentSchwerverkehr(BigDecimal.valueOf(12));

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
        interval1.setProzentGueterverkehr(BigDecimal.valueOf(12));
        interval1.setProzentSchwerverkehr(BigDecimal.valueOf(13));

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
        interval2.setProzentGueterverkehr(BigDecimal.valueOf(13));
        interval2.setProzentSchwerverkehr(BigDecimal.valueOf(14));

        final var intervals = List.of(interval0, interval1, interval2);
        final var optionsFahrzeuge = new FahrzeugOptionsDTO();
        optionsFahrzeuge.setKraftfahrzeugverkehr(true);
        optionsFahrzeuge.setSchwerverkehr(true);
        optionsFahrzeuge.setGueterverkehr(true);
        optionsFahrzeuge.setRadverkehr(true);
        optionsFahrzeuge.setFussverkehr(true);
        optionsFahrzeuge.setSchwerverkehrsanteilProzent(true);
        optionsFahrzeuge.setGueterverkehrsanteilProzent(true);
        optionsFahrzeuge.setLieferwagen(true);
        optionsFahrzeuge.setPersonenkraftwagen(true);
        optionsFahrzeuge.setLastkraftwagen(true);
        optionsFahrzeuge.setLastzuege(true);
        optionsFahrzeuge.setBusse(true);
        optionsFahrzeuge.setKraftraeder(true);

        final var result = ganglinieService.ladeGanglinie(intervals, optionsFahrzeuge);

        final var expected = new LadeZaehldatenSteplineDTO();
        expected.setLegend(List.of("Pkw", "Lkw", "Lz", "Lfw", "Bus", "Krad", "Rad", "Kfz", "SV", "SV %", "GV", "GV %"));
        expected.setRangeMax(20);
        expected.setRangeMaxPercent(14);
        expected.setXAxisDataFirstChart(List.of("00:00", "00:30", "01:00"));
        var seriesEntries = new ArrayList<StepLineSeriesEntryBaseDTO>();
        var seriesEntry = new StepLineSeriesEntryIntegerDTO();
        seriesEntry.setName("Pkw");
        seriesEntry.setYAxisData(List.of(6, 7, 8));
        seriesEntry.setXAxisIndex(0);
        seriesEntry.setYAxisIndex(0);
        seriesEntries.add(seriesEntry);
        seriesEntry = new StepLineSeriesEntryIntegerDTO();
        seriesEntry.setName("Lkw");
        seriesEntry.setYAxisData(List.of(3, 4, 5));
        seriesEntry.setXAxisIndex(0);
        seriesEntry.setYAxisIndex(0);
        seriesEntries.add(seriesEntry);
        seriesEntry = new StepLineSeriesEntryIntegerDTO();
        seriesEntry.setName("Lz");
        seriesEntry.setYAxisData(List.of(7, 8, 9));
        seriesEntry.setXAxisIndex(0);
        seriesEntry.setYAxisIndex(0);
        seriesEntries.add(seriesEntry);
        seriesEntry = new StepLineSeriesEntryIntegerDTO();
        seriesEntry.setName("Lfw");
        seriesEntry.setYAxisData(List.of(1, 2, 3));
        seriesEntry.setXAxisIndex(0);
        seriesEntry.setYAxisIndex(0);
        seriesEntries.add(seriesEntry);
        seriesEntry = new StepLineSeriesEntryIntegerDTO();
        seriesEntry.setName("Busse");
        seriesEntry.setYAxisData(List.of(4, 5, 6));
        seriesEntry.setXAxisIndex(0);
        seriesEntry.setYAxisIndex(0);
        seriesEntries.add(seriesEntry);
        seriesEntry = new StepLineSeriesEntryIntegerDTO();
        seriesEntry.setName("Krad");
        seriesEntry.setYAxisData(List.of(2, 3, 4));
        seriesEntry.setXAxisIndex(0);
        seriesEntry.setYAxisIndex(0);
        seriesEntries.add(seriesEntry);
        seriesEntry = new StepLineSeriesEntryIntegerDTO();
        seriesEntry.setName("Rad");
        seriesEntry.setYAxisData(List.of(5, 6, 7));
        seriesEntry.setXAxisIndex(0);
        seriesEntry.setYAxisIndex(0);
        seriesEntries.add(seriesEntry);
        seriesEntry = new StepLineSeriesEntryIntegerDTO();
        seriesEntry.setName("Kfz");
        seriesEntry.setYAxisData(List.of(10, 11, 12));
        seriesEntry.setXAxisIndex(0);
        seriesEntry.setYAxisIndex(0);
        seriesEntries.add(seriesEntry);
        seriesEntry = new StepLineSeriesEntryIntegerDTO();
        seriesEntry.setName("SV");
        seriesEntry.setYAxisData(List.of(9, 10, 11));
        seriesEntry.setXAxisIndex(0);
        seriesEntry.setYAxisIndex(0);
        seriesEntries.add(seriesEntry);
        var seriesEntryBigDecimal = new StepLineSeriesEntryBigDecimalDTO();
        seriesEntryBigDecimal.setName("SV %");
        seriesEntryBigDecimal.setYAxisData(List.of(BigDecimal.valueOf(12), BigDecimal.valueOf(13), BigDecimal.valueOf(14)));
        seriesEntryBigDecimal.setXAxisIndex(0);
        seriesEntryBigDecimal.setYAxisIndex(1);
        seriesEntries.add(seriesEntryBigDecimal);
        seriesEntry = new StepLineSeriesEntryIntegerDTO();
        seriesEntry.setName("GV");
        seriesEntry.setYAxisData(List.of(8, 9, 10));
        seriesEntry.setXAxisIndex(0);
        seriesEntry.setYAxisIndex(0);
        seriesEntries.add(seriesEntry);
        seriesEntryBigDecimal = new StepLineSeriesEntryBigDecimalDTO();
        seriesEntryBigDecimal.setName("GV %");
        seriesEntryBigDecimal.setYAxisData(List.of(BigDecimal.valueOf(11), BigDecimal.valueOf(12), BigDecimal.valueOf(13)));
        seriesEntryBigDecimal.setXAxisIndex(0);
        seriesEntryBigDecimal.setYAxisIndex(1);
        seriesEntries.add(seriesEntryBigDecimal);

        expected.setSeriesEntriesFirstChart(seriesEntries);
        Assertions.assertThat(result).isEqualTo(expected);

    }
}
