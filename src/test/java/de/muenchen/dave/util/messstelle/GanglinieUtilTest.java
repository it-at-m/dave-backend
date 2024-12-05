package de.muenchen.dave.util.messstelle;

import de.muenchen.dave.domain.dtos.laden.LadeZaehldatenSteplineDTO;
import de.muenchen.dave.domain.dtos.laden.StepLineSeriesEntryBaseDTO;
import de.muenchen.dave.domain.dtos.laden.StepLineSeriesEntryIntegerDTO;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class GanglinieUtilTest {

    @Test
    void setRangeMaxRoundedToTwentyInZaehldatenStepline() {
        final var ladeZaehldatenStepline = new LadeZaehldatenSteplineDTO();

        ladeZaehldatenStepline.setRangeMax(null);
        GanglinieUtil.setRangeMaxRoundedToTwentyInZaehldatenStepline(ladeZaehldatenStepline, null);
        Assertions.assertThat(ladeZaehldatenStepline.getRangeMax()).isNotNull().isEqualTo(20);

        ladeZaehldatenStepline.setRangeMax(null);
        GanglinieUtil.setRangeMaxRoundedToTwentyInZaehldatenStepline(ladeZaehldatenStepline, 99);
        Assertions.assertThat(ladeZaehldatenStepline.getRangeMax()).isNotNull().isEqualTo(100);

        ladeZaehldatenStepline.setRangeMax(99);
        GanglinieUtil.setRangeMaxRoundedToTwentyInZaehldatenStepline(ladeZaehldatenStepline, 101);
        Assertions.assertThat(ladeZaehldatenStepline.getRangeMax()).isNotNull().isEqualTo(120);

        ladeZaehldatenStepline.setRangeMax(101);
        GanglinieUtil.setRangeMaxRoundedToTwentyInZaehldatenStepline(ladeZaehldatenStepline, 99);
        Assertions.assertThat(ladeZaehldatenStepline.getRangeMax()).isNotNull().isEqualTo(120);

        ladeZaehldatenStepline.setRangeMax(99);
        GanglinieUtil.setRangeMaxRoundedToTwentyInZaehldatenStepline(ladeZaehldatenStepline, null);
        Assertions.assertThat(ladeZaehldatenStepline.getRangeMax()).isNotNull().isEqualTo(100);
    }

    @Test
    void setRangeMaxPercentRoundedToTwoInZaehldatenStepline() {
        final var ladeZaehldatenStepline = new LadeZaehldatenSteplineDTO();

        ladeZaehldatenStepline.setRangeMaxPercent(null);
        GanglinieUtil.setRangeMaxPercentRoundedToTwoInZaehldatenStepline(ladeZaehldatenStepline, null);
        Assertions.assertThat(ladeZaehldatenStepline.getRangeMaxPercent()).isNotNull().isEqualTo(2);

        ladeZaehldatenStepline.setRangeMaxPercent(null);
        GanglinieUtil.setRangeMaxPercentRoundedToTwoInZaehldatenStepline(ladeZaehldatenStepline, BigDecimal.valueOf(100));
        Assertions.assertThat(ladeZaehldatenStepline.getRangeMaxPercent()).isNotNull().isEqualTo(100);

        ladeZaehldatenStepline.setRangeMaxPercent(99);
        GanglinieUtil.setRangeMaxPercentRoundedToTwoInZaehldatenStepline(ladeZaehldatenStepline, BigDecimal.valueOf(100));
        Assertions.assertThat(ladeZaehldatenStepline.getRangeMaxPercent()).isNotNull().isEqualTo(100);

        ladeZaehldatenStepline.setRangeMaxPercent(101);
        GanglinieUtil.setRangeMaxPercentRoundedToTwoInZaehldatenStepline(ladeZaehldatenStepline, BigDecimal.valueOf(100));
        Assertions.assertThat(ladeZaehldatenStepline.getRangeMaxPercent()).isNotNull().isEqualTo(102);

        ladeZaehldatenStepline.setRangeMaxPercent(100);
        GanglinieUtil.setRangeMaxPercentRoundedToTwoInZaehldatenStepline(ladeZaehldatenStepline, null);
        Assertions.assertThat(ladeZaehldatenStepline.getRangeMaxPercent()).isNotNull().isEqualTo(100);
    }

    @Test
    void setLegendInZaehldatenStepline() {
        final var ladeZaehldatenStepline = new LadeZaehldatenSteplineDTO();

        ladeZaehldatenStepline.setLegend(new ArrayList<>(List.of("A", "B", "D")));
        GanglinieUtil.setLegendInZaehldatenStepline(ladeZaehldatenStepline, "A");
        Assertions.assertThat(ladeZaehldatenStepline.getLegend()).isNotEmpty().isEqualTo(List.of("A", "B", "D"));

        ladeZaehldatenStepline.setLegend(new ArrayList<>(List.of("A", "B", "D")));
        GanglinieUtil.setLegendInZaehldatenStepline(ladeZaehldatenStepline, "C");
        Assertions.assertThat(ladeZaehldatenStepline.getLegend()).isNotEmpty().isEqualTo(List.of("A", "B", "D", "C"));

        ladeZaehldatenStepline.setLegend(new ArrayList<>(List.of("A", "B", "D")));
        GanglinieUtil.setLegendInZaehldatenStepline(ladeZaehldatenStepline, null);
        Assertions.assertThat(ladeZaehldatenStepline.getLegend()).isNotEmpty().isEqualTo(Arrays.stream(new String[] { "A", "B", "D", null }).toList());
    }

    @Test
    void setSeriesIndexForFirstChartValue() {
        final var stepLineSeriesEntry = new StepLineSeriesEntryBaseDTO();
        GanglinieUtil.setSeriesIndexForFirstChartValue(stepLineSeriesEntry);

        final var expected = new StepLineSeriesEntryBaseDTO();
        expected.setXAxisIndex(0);
        expected.setYAxisIndex(0);

        Assertions.assertThat(stepLineSeriesEntry).isNotNull().isEqualTo(expected);
    }

    @Test
    void setSeriesIndexForFirstChartPercent() {
        final var stepLineSeriesEntry = new StepLineSeriesEntryIntegerDTO();
        GanglinieUtil.setSeriesIndexForFirstChartPercent(stepLineSeriesEntry);

        final var expected = new StepLineSeriesEntryIntegerDTO();
        expected.setXAxisIndex(0);
        expected.setYAxisIndex(1);

        Assertions.assertThat(stepLineSeriesEntry).isNotNull().isEqualTo(expected);
    }

    @Test
    void getIntValueIfNotNull() {
        var result = GanglinieUtil.getIntValueIfNotNull(null);
        Assertions.assertThat(result).isNull();

        result = GanglinieUtil.getIntValueIfNotNull(BigDecimal.valueOf(2.6D));
        Assertions.assertThat(result).isNotNull().isEqualTo(2);

        result = GanglinieUtil.getIntValueIfNotNull(BigDecimal.valueOf(2.3D));
        Assertions.assertThat(result).isNotNull().isEqualTo(2);

        result = GanglinieUtil.getIntValueIfNotNull(BigDecimal.valueOf(2));
        Assertions.assertThat(result).isNotNull().isEqualTo(2);
    }

    @Test
    void getInitialZaehldatenStepline() {
        final var result = GanglinieUtil.getInitialZaehldatenStepline();
        final var expected = new LadeZaehldatenSteplineDTO();
        expected.setRangeMax(0);
        expected.setRangeMaxPercent(0);
        expected.setLegend(new ArrayList<>());
        expected.setXAxisDataFirstChart(new ArrayList<>());
        expected.setSeriesEntriesFirstChart(new ArrayList<>());
        Assertions.assertThat(result).isNotNull().isEqualTo(expected);
    }

}
