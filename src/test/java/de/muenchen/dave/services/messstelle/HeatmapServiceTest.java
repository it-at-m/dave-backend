package de.muenchen.dave.services.messstelle;

import de.muenchen.dave.domain.dtos.laden.LadeZaehldatenHeatmapDTO;
import de.muenchen.dave.domain.dtos.messstelle.FahrzeugOptionsDTO;
import de.muenchen.dave.domain.dtos.messstelle.MessstelleOptionsDTO;
import de.muenchen.dave.geodateneai.gen.model.IntervalDto;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
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
class HeatmapServiceTest {

    private HeatmapService heatmapService;

    @BeforeEach
    public void beforeEach() {
        heatmapService = new HeatmapService();
    }

    @Test
    void ladeHeatmap() {
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

        final var options = new MessstelleOptionsDTO();
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
        options.setFahrzeuge(optionsFahrzeuge);

        final var result = heatmapService.ladeHeatmap(intervals, options);

        final var expected = new LadeZaehldatenHeatmapDTO();
        expected.setLegend(List.of("Güterverkehr", "Schwerverkehr", "Kraftfahrzeuge", "Fahrräder", "Krafträder", "Busse", "Lieferwagen", "Lastzüge",
                "Lastkraftwagen", "Personenkraftwagen"));
        expected.setRangeMin(0);
        expected.setRangeMax(12);
        expected.setXAxisDataFirstChart(List.of("00:00", "00:30", "01:00"));
        expected.setSeriesEntriesFirstChart(List.of(
                List.of(0, 0, 8),
                List.of(0, 1, 9),
                List.of(0, 2, 10),
                List.of(0, 3, 5),
                List.of(0, 4, 2),
                List.of(0, 5, 4),
                List.of(0, 6, 1),
                List.of(0, 7, 7),
                List.of(0, 8, 3),
                List.of(0, 9, 6),
                List.of(1, 0, 9),
                List.of(1, 1, 10),
                List.of(1, 2, 11),
                List.of(1, 3, 6),
                List.of(1, 4, 3),
                List.of(1, 5, 5),
                List.of(1, 6, 2),
                List.of(1, 7, 8),
                List.of(1, 8, 4),
                List.of(1, 9, 7),
                List.of(2, 0, 10),
                List.of(2, 1, 11),
                List.of(2, 2, 12),
                List.of(2, 3, 7),
                List.of(2, 4, 4),
                List.of(2, 5, 6),
                List.of(2, 6, 3),
                List.of(2, 7, 9),
                List.of(2, 8, 5),
                List.of(2, 9, 8)));

        Assertions.assertThat(result).isNotNull().isEqualTo(expected);
    }

    @Test
    void insertSingleHeatmapDataIntoLadeZaehldatenHeatmap() {
        var ladeZaehldatenHeatmap = new LadeZaehldatenHeatmapDTO();
        ladeZaehldatenHeatmap.setLegend(new ArrayList<>());
        ladeZaehldatenHeatmap.setSeriesEntriesFirstChart(new ArrayList<>());
        HeatmapService.insertSingleHeatmapDataIntoLadeZaehldatenHeatmap(
                ladeZaehldatenHeatmap,
                1,
                2,
                3,
                "A");
        var expected = new LadeZaehldatenHeatmapDTO();
        expected.setLegend(List.of("A"));
        expected.setRangeMin(0);
        expected.setRangeMax(3);
        expected.setSeriesEntriesFirstChart(List.of(List.of(1, 2, 3)));
        Assertions.assertThat(ladeZaehldatenHeatmap).isNotNull().isEqualTo(expected);

        HeatmapService.insertSingleHeatmapDataIntoLadeZaehldatenHeatmap(
                ladeZaehldatenHeatmap,
                2,
                3,
                4,
                "B");
        expected = new LadeZaehldatenHeatmapDTO();
        expected.setLegend(List.of("A", "B"));
        expected.setRangeMin(0);
        expected.setRangeMax(4);
        expected.setSeriesEntriesFirstChart(List.of(List.of(1, 2, 3), List.of(2, 3, 4)));
        Assertions.assertThat(ladeZaehldatenHeatmap).isNotNull().isEqualTo(expected);

        HeatmapService.insertSingleHeatmapDataIntoLadeZaehldatenHeatmap(
                ladeZaehldatenHeatmap,
                3,
                4,
                2,
                "B");
        expected = new LadeZaehldatenHeatmapDTO();
        expected.setLegend(List.of("A", "B"));
        expected.setRangeMin(0);
        expected.setRangeMax(4);
        expected.setSeriesEntriesFirstChart(List.of(List.of(1, 2, 3), List.of(2, 3, 4), List.of(3, 4, 2)));
        Assertions.assertThat(ladeZaehldatenHeatmap).isNotNull().isEqualTo(expected);

        HeatmapService.insertSingleHeatmapDataIntoLadeZaehldatenHeatmap(
                ladeZaehldatenHeatmap,
                4,
                5,
                null,
                "C");
        expected = new LadeZaehldatenHeatmapDTO();
        expected.setLegend(List.of("A", "B", "C"));
        expected.setRangeMin(0);
        expected.setRangeMax(4);
        expected.setSeriesEntriesFirstChart(
                List.of(List.of(1, 2, 3), List.of(2, 3, 4), List.of(3, 4, 2), Arrays.stream(new Integer[] { 4, 5, null }).toList()));
        Assertions.assertThat(ladeZaehldatenHeatmap).isNotNull().isEqualTo(expected);
    }

    @Test
    void createHeatMapEntry() {
        var result = HeatmapService.createHeatMapEntry(1, 2, 3);
        Assertions.assertThat(result).isNotEmpty().isEqualTo(List.of(1, 2, 3));

        result = HeatmapService.createHeatMapEntry(1, 2, null);
        Assertions.assertThat(result).isNotEmpty().isEqualTo(Arrays.stream(new Integer[] { 1, 2, null }).toList());
    }

}
