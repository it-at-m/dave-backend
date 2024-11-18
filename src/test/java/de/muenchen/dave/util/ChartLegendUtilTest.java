package de.muenchen.dave.util;

import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ChartLegendUtilTest {

    @Test
    public void checkAndAddToLegendWhenNotAvailable() {
        List<String> legend = new ArrayList<>();
        legend.add(ChartLegendUtil.PKW);
        legend.add(ChartLegendUtil.LKW);

        assertThat(ChartLegendUtil.checkAndAddToLegendWhenNotAvailable(legend, ChartLegendUtil.BUSSE).size(), is(3));
        assertThat(legend.get(0), is(ChartLegendUtil.PKW));
        assertThat(legend.get(1), is(ChartLegendUtil.LKW));
        assertThat(legend.get(2), is(ChartLegendUtil.BUSSE));

        assertThat(ChartLegendUtil.checkAndAddToLegendWhenNotAvailable(legend, ChartLegendUtil.PKW).size(), is(3));
        assertThat(legend.get(0), is(ChartLegendUtil.PKW));
        assertThat(legend.get(1), is(ChartLegendUtil.LKW));
        assertThat(legend.get(2), is(ChartLegendUtil.BUSSE));

        assertThat(ChartLegendUtil.checkAndAddToLegendWhenNotAvailable(legend, null).size(), is(4));
        assertThat(legend.get(0), is(ChartLegendUtil.PKW));
        assertThat(legend.get(1), is(ChartLegendUtil.LKW));
        assertThat(legend.get(2), is(ChartLegendUtil.BUSSE));
        assertThat(legend.get(3), is(IsNull.nullValue()));
    }

}
