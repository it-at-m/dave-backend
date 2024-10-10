package de.muenchen.dave.services.messstelle;

import de.muenchen.dave.domain.dtos.laden.LadeZaehldatenHeatmapDTO;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HeatmapServiceTest {

    private HeatmapService heatmapService;

    @BeforeEach
    public void beforeEach() {
        heatmapService = new HeatmapService();
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
