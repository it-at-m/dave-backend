package de.muenchen.dave.services.messstelle;

import de.muenchen.dave.domain.dtos.laden.LadeZaehldatenSteplineDTO;
import de.muenchen.dave.domain.dtos.laden.StepLineSeriesEntryBaseDTO;
import de.muenchen.dave.domain.dtos.laden.StepLineSeriesEntryIntegerDTO;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GanglinieServiceTest {

    private GanglinieService ganglinieService;

    @BeforeEach
    public void beforeEach() {
        ganglinieService = new GanglinieService();
    }

    @Test
    void setRangeMaxRoundedToTwentyInZaehldatenStepline() {
        final var ladeZaehldatenStepline = new LadeZaehldatenSteplineDTO();

        ladeZaehldatenStepline.setRangeMax(null);
        GanglinieService.setRangeMaxRoundedToTwentyInZaehldatenStepline(ladeZaehldatenStepline, null);
        Assertions.assertThat(ladeZaehldatenStepline.getRangeMax()).isNotNull().isEqualTo(20);

        ladeZaehldatenStepline.setRangeMax(null);
        GanglinieService.setRangeMaxRoundedToTwentyInZaehldatenStepline(ladeZaehldatenStepline, 99);
        Assertions.assertThat(ladeZaehldatenStepline.getRangeMax()).isNotNull().isEqualTo(100);

        ladeZaehldatenStepline.setRangeMax(99);
        GanglinieService.setRangeMaxRoundedToTwentyInZaehldatenStepline(ladeZaehldatenStepline, 101);
        Assertions.assertThat(ladeZaehldatenStepline.getRangeMax()).isNotNull().isEqualTo(120);

        ladeZaehldatenStepline.setRangeMax(101);
        GanglinieService.setRangeMaxRoundedToTwentyInZaehldatenStepline(ladeZaehldatenStepline, 99);
        Assertions.assertThat(ladeZaehldatenStepline.getRangeMax()).isNotNull().isEqualTo(120);

        ladeZaehldatenStepline.setRangeMax(99);
        GanglinieService.setRangeMaxRoundedToTwentyInZaehldatenStepline(ladeZaehldatenStepline, null);
        Assertions.assertThat(ladeZaehldatenStepline.getRangeMax()).isNotNull().isEqualTo(100);
    }

    @Test
    void setRangeMaxPercentRoundedToTwoInZaehldatenStepline() {
        final var ladeZaehldatenStepline = new LadeZaehldatenSteplineDTO();

        ladeZaehldatenStepline.setRangeMaxPercent(null);
        GanglinieService.setRangeMaxPercentRoundedToTwoInZaehldatenStepline(ladeZaehldatenStepline, null);
        Assertions.assertThat(ladeZaehldatenStepline.getRangeMaxPercent()).isNotNull().isEqualTo(2);

        ladeZaehldatenStepline.setRangeMaxPercent(null);
        GanglinieService.setRangeMaxPercentRoundedToTwoInZaehldatenStepline(ladeZaehldatenStepline, BigDecimal.valueOf(100));
        Assertions.assertThat(ladeZaehldatenStepline.getRangeMaxPercent()).isNotNull().isEqualTo(100);

        ladeZaehldatenStepline.setRangeMaxPercent(99);
        GanglinieService.setRangeMaxPercentRoundedToTwoInZaehldatenStepline(ladeZaehldatenStepline, BigDecimal.valueOf(100));
        Assertions.assertThat(ladeZaehldatenStepline.getRangeMaxPercent()).isNotNull().isEqualTo(100);

        ladeZaehldatenStepline.setRangeMaxPercent(101);
        GanglinieService.setRangeMaxPercentRoundedToTwoInZaehldatenStepline(ladeZaehldatenStepline, BigDecimal.valueOf(100));
        Assertions.assertThat(ladeZaehldatenStepline.getRangeMaxPercent()).isNotNull().isEqualTo(102);

        ladeZaehldatenStepline.setRangeMaxPercent(100);
        GanglinieService.setRangeMaxPercentRoundedToTwoInZaehldatenStepline(ladeZaehldatenStepline, null);
        Assertions.assertThat(ladeZaehldatenStepline.getRangeMaxPercent()).isNotNull().isEqualTo(100);
    }

    @Test
    void setLegendInZaehldatenStepline() {
        final var ladeZaehldatenStepline = new LadeZaehldatenSteplineDTO();

        ladeZaehldatenStepline.setLegend(new ArrayList<>(List.of("A", "B", "D")));
        GanglinieService.setLegendInZaehldatenStepline(ladeZaehldatenStepline, "A");
        Assertions.assertThat(ladeZaehldatenStepline.getLegend()).isNotEmpty().isEqualTo(List.of("A", "B", "D"));

        ladeZaehldatenStepline.setLegend(new ArrayList<>(List.of("A", "B", "D")));
        GanglinieService.setLegendInZaehldatenStepline(ladeZaehldatenStepline, "C");
        Assertions.assertThat(ladeZaehldatenStepline.getLegend()).isNotEmpty().isEqualTo(List.of("A", "B", "D", "C"));

        ladeZaehldatenStepline.setLegend(new ArrayList<>(List.of("A", "B", "D")));
        GanglinieService.setLegendInZaehldatenStepline(ladeZaehldatenStepline, null);
        Assertions.assertThat(ladeZaehldatenStepline.getLegend()).isNotEmpty().isEqualTo(Arrays.stream(new String[] { "A", "B", "D", null }).toList());
    }

    @Test
    void setSeriesIndexForFirstChartValue() {
        final var stepLineSeriesEntry = new StepLineSeriesEntryBaseDTO();
        GanglinieService.setSeriesIndexForFirstChartValue(stepLineSeriesEntry);

        final var expected = new StepLineSeriesEntryBaseDTO();
        expected.setXAxisIndex(0);
        expected.setYAxisIndex(0);

        Assertions.assertThat(stepLineSeriesEntry).isNotNull().isEqualTo(expected);
    }

    @Test
    void setSeriesIndexForFirstChartPercent() {
        final var stepLineSeriesEntry = new StepLineSeriesEntryIntegerDTO();
        GanglinieService.setSeriesIndexForFirstChartPercent(stepLineSeriesEntry);

        final var expected = new StepLineSeriesEntryIntegerDTO();
        expected.setXAxisIndex(0);
        expected.setYAxisIndex(1);

        Assertions.assertThat(stepLineSeriesEntry).isNotNull().isEqualTo(expected);
    }

    @Test
    void getIntValueIfNotNull() {
        var result = ganglinieService.getIntValueIfNotNull(null);
        Assertions.assertThat(result).isNull();

        result = ganglinieService.getIntValueIfNotNull(BigDecimal.valueOf(2.6D));
        Assertions.assertThat(result).isNotNull().isEqualTo(2);

        result = ganglinieService.getIntValueIfNotNull(BigDecimal.valueOf(2.3D));
        Assertions.assertThat(result).isNotNull().isEqualTo(2);

        result = ganglinieService.getIntValueIfNotNull(BigDecimal.valueOf(2));
        Assertions.assertThat(result).isNotNull().isEqualTo(2);
    }

}
