package de.muenchen.dave.services.processzaehldaten;

import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatenHeatmapDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatenTableDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatumDTO;
import de.muenchen.dave.domain.enums.Zaehldauer;
import de.muenchen.dave.domain.enums.Zeitblock;
import de.muenchen.dave.services.ladezaehldaten.LadeZaehldatenService;
import de.muenchen.dave.util.ChartLegendUtil;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ProcessZaehldatenHeatmapServiceTest {

    private ProcessZaehldatenHeatmapService processZaehldatenHeatmapService = new ProcessZaehldatenHeatmapService();

    private LadeZaehldatenTableDTO zaehldatenTable;

    @BeforeEach
    public void beforeEach() {
        zaehldatenTable = new LadeZaehldatenTableDTO();

        final LadeZaehldatumDTO ladeZaehldatum1 = new LadeZaehldatumDTO();
        ladeZaehldatum1.setType(null);
        ladeZaehldatum1.setStartUhrzeit(LocalTime.of(6, 0));
        ladeZaehldatum1.setEndeUhrzeit(LocalTime.of(7, 0));
        ladeZaehldatum1.setPkw(1);
        ladeZaehldatum1.setLkw(1);
        ladeZaehldatum1.setLastzuege(1);
        ladeZaehldatum1.setBusse(1);
        ladeZaehldatum1.setKraftraeder(1);
        ladeZaehldatum1.setFahrradfahrer(1);
        ladeZaehldatum1.setFussgaenger(1);
        ladeZaehldatum1.setPkwEinheiten(1);

        final LadeZaehldatumDTO ladeZaehldatum2 = new LadeZaehldatumDTO();
        ladeZaehldatum2.setType(null);
        ladeZaehldatum2.setStartUhrzeit(LocalTime.of(7, 0));
        ladeZaehldatum2.setEndeUhrzeit(LocalTime.of(8, 0));
        ladeZaehldatum2.setPkw(2);
        ladeZaehldatum2.setLkw(2);
        ladeZaehldatum2.setLastzuege(2);
        ladeZaehldatum2.setBusse(2);
        ladeZaehldatum2.setKraftraeder(2);
        ladeZaehldatum2.setFahrradfahrer(2);
        ladeZaehldatum2.setFussgaenger(2);
        ladeZaehldatum2.setPkwEinheiten(2);

        final LadeZaehldatumDTO ladeZaehldatum3 = new LadeZaehldatumDTO();
        ladeZaehldatum3.setType(null);
        ladeZaehldatum3.setStartUhrzeit(LocalTime.of(8, 0));
        ladeZaehldatum3.setEndeUhrzeit(LocalTime.of(9, 0));
        ladeZaehldatum3.setPkw(3);
        ladeZaehldatum3.setLkw(3);
        ladeZaehldatum3.setLastzuege(3);
        ladeZaehldatum3.setBusse(5);
        ladeZaehldatum3.setKraftraeder(3);
        ladeZaehldatum3.setFahrradfahrer(3);
        ladeZaehldatum3.setFussgaenger(3);
        ladeZaehldatum3.setPkwEinheiten(3);

        List<LadeZaehldatumDTO> zaehldaten = new ArrayList<>();
        zaehldaten.add(ladeZaehldatum1);
        zaehldaten.add(ladeZaehldatum2);
        zaehldaten.add(ladeZaehldatum3);

        zaehldatenTable.setZaehldaten(zaehldaten);
    }

    @Test
    public void ladeProcessedZaehldatenHeatmap() {
        final OptionsDTO options = new OptionsDTO();
        options.setPersonenkraftwagen(true);
        options.setLastkraftwagen(true);
        options.setLastzuege(true);
        options.setBusse(true);
        options.setKraftraeder(true);
        options.setRadverkehr(true);
        options.setFussverkehr(true);

        options.setKraftfahrzeugverkehr(true);
        options.setSchwerverkehr(true);
        options.setGueterverkehr(true);
        options.setPkwEinheiten(true);

        options.setZeitblock(Zeitblock.ZB_00_06);
        options.setZaehldauer(Zaehldauer.DAUER_24_STUNDEN);
        options.setZeitauswahl(LadeZaehldatenService.BLOCK);

        final LadeZaehldatenHeatmapDTO ladeZaehldatenHeatmap = processZaehldatenHeatmapService.ladeProcessedZaehldatenHeatmap(zaehldatenTable, options);

        assertThat(ladeZaehldatenHeatmap.getLegend().size(), is(11));
        assertThat(ladeZaehldatenHeatmap.getLegend().get(0), is(ChartLegendUtil.PKW_EINHEITEN_HEATMAP));
        assertThat(ladeZaehldatenHeatmap.getLegend().get(1), is(ChartLegendUtil.GUETERVERKEHR_HEATMAP));
        assertThat(ladeZaehldatenHeatmap.getLegend().get(2), is(ChartLegendUtil.SCHWERVERKEHR_HEATMAP));
        assertThat(ladeZaehldatenHeatmap.getLegend().get(3), is(ChartLegendUtil.KFZ_HEATMAP));
        assertThat(ladeZaehldatenHeatmap.getLegend().get(4), is(ChartLegendUtil.FUSSGAENGER_HEATMAP));
        assertThat(ladeZaehldatenHeatmap.getLegend().get(5), is(ChartLegendUtil.RAD_HEATMAP));
        assertThat(ladeZaehldatenHeatmap.getLegend().get(6), is(ChartLegendUtil.KRAFTRAEDER_HEATMAP));
        assertThat(ladeZaehldatenHeatmap.getLegend().get(7), is(ChartLegendUtil.BUSSE_HEATMAP));
        assertThat(ladeZaehldatenHeatmap.getLegend().get(8), is(ChartLegendUtil.LASTZUEGE_HEATMAP));
        assertThat(ladeZaehldatenHeatmap.getLegend().get(9), is(ChartLegendUtil.LKW_HEATMAP));
        assertThat(ladeZaehldatenHeatmap.getLegend().get(10), is(ChartLegendUtil.PKW_HEATMAP));

        assertThat(ladeZaehldatenHeatmap.getRangeMin(), is(0));
        assertThat(ladeZaehldatenHeatmap.getRangeMax(), is(17));

        assertThat(ladeZaehldatenHeatmap.getXAxisDataFirstChart().size(), is(3));
        Iterator<String> xAxisData = ladeZaehldatenHeatmap.getXAxisDataFirstChart().iterator();
        assertThat(xAxisData.next(), is("06:00"));
        assertThat(xAxisData.next(), is("07:00"));
        assertThat(xAxisData.next(), is("08:00"));

        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesFirstChart().size(), is(33));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesFirstChart().get(0), is(Arrays.asList(0, 0, 1)));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesFirstChart().get(1), is(Arrays.asList(0, 1, 2)));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesFirstChart().get(2), is(Arrays.asList(0, 2, 3)));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesFirstChart().get(3), is(Arrays.asList(0, 3, 5)));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesFirstChart().get(4), is(Arrays.asList(0, 4, 1)));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesFirstChart().get(5), is(Arrays.asList(0, 5, 1)));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesFirstChart().get(6), is(Arrays.asList(0, 6, 1)));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesFirstChart().get(7), is(Arrays.asList(0, 7, 1)));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesFirstChart().get(8), is(Arrays.asList(0, 8, 1)));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesFirstChart().get(9), is(Arrays.asList(0, 9, 1)));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesFirstChart().get(10), is(Arrays.asList(0, 10, 1)));

        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesFirstChart().get(11), is(Arrays.asList(1, 0, 2)));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesFirstChart().get(12), is(Arrays.asList(1, 1, 4)));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesFirstChart().get(13), is(Arrays.asList(1, 2, 6)));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesFirstChart().get(14), is(Arrays.asList(1, 3, 10)));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesFirstChart().get(15), is(Arrays.asList(1, 4, 2)));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesFirstChart().get(16), is(Arrays.asList(1, 5, 2)));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesFirstChart().get(17), is(Arrays.asList(1, 6, 2)));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesFirstChart().get(18), is(Arrays.asList(1, 7, 2)));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesFirstChart().get(19), is(Arrays.asList(1, 8, 2)));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesFirstChart().get(20), is(Arrays.asList(1, 9, 2)));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesFirstChart().get(21), is(Arrays.asList(1, 10, 2)));

        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesFirstChart().get(22), is(Arrays.asList(2, 0, 3)));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesFirstChart().get(23), is(Arrays.asList(2, 1, 6)));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesFirstChart().get(24), is(Arrays.asList(2, 2, 11)));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesFirstChart().get(25), is(Arrays.asList(2, 3, 17)));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesFirstChart().get(26), is(Arrays.asList(2, 4, 3)));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesFirstChart().get(27), is(Arrays.asList(2, 5, 3)));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesFirstChart().get(28), is(Arrays.asList(2, 6, 3)));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesFirstChart().get(29), is(Arrays.asList(2, 7, 5)));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesFirstChart().get(30), is(Arrays.asList(2, 8, 3)));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesFirstChart().get(31), is(Arrays.asList(2, 9, 3)));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesFirstChart().get(32), is(Arrays.asList(2, 10, 3)));
    }

    @Test
    public void ladeProcessedZaehldatenHeatmapOnlyPkw() {
        final OptionsDTO options = new OptionsDTO();
        options.setPersonenkraftwagen(true);
        options.setLastkraftwagen(false);
        options.setLastzuege(false);
        options.setBusse(false);
        options.setKraftraeder(false);
        options.setRadverkehr(false);
        options.setFussverkehr(false);

        options.setKraftfahrzeugverkehr(false);
        options.setSchwerverkehr(false);
        options.setGueterverkehr(false);
        options.setPkwEinheiten(false);

        options.setZeitblock(Zeitblock.ZB_00_06);
        options.setZaehldauer(Zaehldauer.DAUER_24_STUNDEN);
        options.setZeitauswahl(LadeZaehldatenService.BLOCK);

        final LadeZaehldatenHeatmapDTO ladeZaehldatenHeatmap = processZaehldatenHeatmapService.ladeProcessedZaehldatenHeatmap(zaehldatenTable, options);

        assertThat(ladeZaehldatenHeatmap.getLegend().size(), is(1));
        assertThat(ladeZaehldatenHeatmap.getLegend().get(0), is(ChartLegendUtil.PKW_HEATMAP));

        assertThat(ladeZaehldatenHeatmap.getRangeMin(), is(0));
        assertThat(ladeZaehldatenHeatmap.getRangeMax(), is(3));

        assertThat(ladeZaehldatenHeatmap.getXAxisDataFirstChart().size(), is(3));
        Iterator<String> xAxisData = ladeZaehldatenHeatmap.getXAxisDataFirstChart().iterator();
        assertThat(xAxisData.next(), is("06:00"));
        assertThat(xAxisData.next(), is("07:00"));
        assertThat(xAxisData.next(), is("08:00"));

        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesFirstChart().size(), is(3));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesFirstChart().get(0), is(Arrays.asList(0, 0, 1)));

        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesFirstChart().get(1), is(Arrays.asList(1, 0, 2)));

        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesFirstChart().get(2), is(Arrays.asList(2, 0, 3)));
    }

    @Test
    public void ladeProcessedZaehldatenHeatmapNoneChoosen() {
        final OptionsDTO options = new OptionsDTO();
        options.setPersonenkraftwagen(false);
        options.setLastkraftwagen(false);
        options.setLastzuege(false);
        options.setBusse(false);
        options.setKraftraeder(false);
        options.setRadverkehr(false);
        options.setFussverkehr(false);

        options.setKraftfahrzeugverkehr(false);
        options.setSchwerverkehr(false);
        options.setGueterverkehr(false);
        options.setPkwEinheiten(false);

        options.setZeitblock(Zeitblock.ZB_00_06);
        options.setZaehldauer(Zaehldauer.DAUER_24_STUNDEN);
        options.setZeitauswahl(LadeZaehldatenService.BLOCK);

        final LadeZaehldatenHeatmapDTO ladeZaehldatenHeatmap = processZaehldatenHeatmapService.ladeProcessedZaehldatenHeatmap(zaehldatenTable, options);

        assertThat(ladeZaehldatenHeatmap.getLegend().size(), is(0));

        assertThat(ladeZaehldatenHeatmap.getRangeMin(), is(0));
        assertThat(ladeZaehldatenHeatmap.getRangeMax(), is(0));

        assertThat(ladeZaehldatenHeatmap.getXAxisDataFirstChart().size(), is(3));
        Iterator<String> xAxisData = ladeZaehldatenHeatmap.getXAxisDataFirstChart().iterator();
        assertThat(xAxisData.next(), is("06:00"));
        assertThat(xAxisData.next(), is("07:00"));
        assertThat(xAxisData.next(), is("08:00"));

        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesFirstChart().size(), is(0));
    }

    @Test
    public void createHeatMapEntry() {
        List<Integer> result = ProcessZaehldatenHeatmapService.createHeatMapEntry(0, 1, 2);
        assertThat(result.get(0), is(0));
        assertThat(result.get(1), is(1));
        assertThat(result.get(2), is(2));

        result = ProcessZaehldatenHeatmapService.createHeatMapEntry(0, 1, null);
        assertThat(result.get(0), is(0));
        assertThat(result.get(1), is(1));
        assertThat(result.get(2), is(IsNull.nullValue()));
    }

    @Test
    public void insertSingleHeatmapDataIntoLadeZaehldatenHeatmap() {
        final LadeZaehldatenHeatmapDTO ladeZaehldatenHeatmap = new LadeZaehldatenHeatmapDTO();
        ladeZaehldatenHeatmap.setLegend(new ArrayList<>());
        ladeZaehldatenHeatmap.setRangeMin(0);
        ladeZaehldatenHeatmap.setRangeMax(0);
        ladeZaehldatenHeatmap.setXAxisDataFirstChart(new ArrayList<>());
        ladeZaehldatenHeatmap.setSeriesEntriesFirstChart(new ArrayList<>());

        ProcessZaehldatenHeatmapService.insertSingleHeatmapDataIntoLadeZaehldatenHeatmap(
                ladeZaehldatenHeatmap,
                0,
                1,
                10,
                "PKW");

        assertThat(ladeZaehldatenHeatmap.getLegend(), is(Arrays.asList("PKW")));
        assertThat(ladeZaehldatenHeatmap.getRangeMax(), is(10));
        assertThat(ladeZaehldatenHeatmap.getRangeMin(), is(0));
        assertThat(ladeZaehldatenHeatmap.getXAxisDataFirstChart(), is(new ArrayList<>()));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesFirstChart(), is(Arrays.asList(Arrays.asList(0, 1, 10))));

        ProcessZaehldatenHeatmapService.insertSingleHeatmapDataIntoLadeZaehldatenHeatmap(
                ladeZaehldatenHeatmap,
                1,
                2,
                -10,
                "LKW");

        assertThat(ladeZaehldatenHeatmap.getLegend(), is(Arrays.asList("PKW", "LKW")));
        assertThat(ladeZaehldatenHeatmap.getRangeMax(), is(10));
        assertThat(ladeZaehldatenHeatmap.getRangeMin(), is(-10));
        assertThat(ladeZaehldatenHeatmap.getXAxisDataFirstChart(), is(new ArrayList<>()));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesFirstChart(), is(Arrays.asList(Arrays.asList(0, 1, 10), Arrays.asList(1, 2, -10))));
    }

    @Test
    public void ladeProcessedZaehldatenHeatmapSplitInTwo() {
        final OptionsDTO options = new OptionsDTO();
        options.setPersonenkraftwagen(true);
        options.setLastkraftwagen(true);
        options.setLastzuege(true);
        options.setBusse(true);
        options.setKraftraeder(true);
        options.setRadverkehr(true);
        options.setFussverkehr(true);

        options.setKraftfahrzeugverkehr(true);
        options.setSchwerverkehr(true);
        options.setGueterverkehr(true);
        options.setPkwEinheiten(true);

        options.setZeitblock(Zeitblock.ZB_00_24);
        options.setZaehldauer(Zaehldauer.DAUER_2_X_4_STUNDEN);
        options.setZeitauswahl(LadeZaehldatenService.BLOCK);

        final LadeZaehldatumDTO ladeZaehldatum4 = new LadeZaehldatumDTO();
        ladeZaehldatum4.setType(null);
        ladeZaehldatum4.setStartUhrzeit(LocalTime.of(9, 0));
        ladeZaehldatum4.setEndeUhrzeit(LocalTime.of(10, 0));
        ladeZaehldatum4.setPkw(4);
        ladeZaehldatum4.setLkw(4);
        ladeZaehldatum4.setLastzuege(4);
        ladeZaehldatum4.setBusse(6);
        ladeZaehldatum4.setKraftraeder(4);
        ladeZaehldatum4.setFahrradfahrer(4);
        ladeZaehldatum4.setFussgaenger(4);
        ladeZaehldatum4.setPkwEinheiten(4);

        List<LadeZaehldatumDTO> zaehldaten = zaehldatenTable.getZaehldaten();
        zaehldaten.add(ladeZaehldatum4);

        zaehldatenTable.setZaehldaten(zaehldaten);

        final LadeZaehldatenHeatmapDTO ladeZaehldatenHeatmap = processZaehldatenHeatmapService.ladeProcessedZaehldatenHeatmap(zaehldatenTable, options);

        assertThat(ladeZaehldatenHeatmap.getLegend().size(), is(11));
        assertThat(ladeZaehldatenHeatmap.getLegend().get(0), is(ChartLegendUtil.PKW_EINHEITEN_HEATMAP));
        assertThat(ladeZaehldatenHeatmap.getLegend().get(1), is(ChartLegendUtil.GUETERVERKEHR_HEATMAP));
        assertThat(ladeZaehldatenHeatmap.getLegend().get(2), is(ChartLegendUtil.SCHWERVERKEHR_HEATMAP));
        assertThat(ladeZaehldatenHeatmap.getLegend().get(3), is(ChartLegendUtil.KFZ_HEATMAP));
        assertThat(ladeZaehldatenHeatmap.getLegend().get(4), is(ChartLegendUtil.FUSSGAENGER_HEATMAP));
        assertThat(ladeZaehldatenHeatmap.getLegend().get(5), is(ChartLegendUtil.RAD_HEATMAP));
        assertThat(ladeZaehldatenHeatmap.getLegend().get(6), is(ChartLegendUtil.KRAFTRAEDER_HEATMAP));
        assertThat(ladeZaehldatenHeatmap.getLegend().get(7), is(ChartLegendUtil.BUSSE_HEATMAP));
        assertThat(ladeZaehldatenHeatmap.getLegend().get(8), is(ChartLegendUtil.LASTZUEGE_HEATMAP));
        assertThat(ladeZaehldatenHeatmap.getLegend().get(9), is(ChartLegendUtil.LKW_HEATMAP));
        assertThat(ladeZaehldatenHeatmap.getLegend().get(10), is(ChartLegendUtil.PKW_HEATMAP));

        assertThat(ladeZaehldatenHeatmap.getRangeMin(), is(0));
        assertThat(ladeZaehldatenHeatmap.getRangeMax(), is(22));

        assertThat(ladeZaehldatenHeatmap.getXAxisDataFirstChart().size(), is(2));
        assertThat(ladeZaehldatenHeatmap.getXAxisDataSecondChart().size(), is(2));
        Iterator<String> xAxisDataFirstChart = ladeZaehldatenHeatmap.getXAxisDataFirstChart().iterator();
        assertThat(xAxisDataFirstChart.next(), is("06:00"));
        assertThat(xAxisDataFirstChart.next(), is("07:00"));
        Iterator<String> xAxisDataSecondChart = ladeZaehldatenHeatmap.getXAxisDataSecondChart().iterator();
        assertThat(xAxisDataSecondChart.next(), is("08:00"));
        assertThat(xAxisDataSecondChart.next(), is("09:00"));

        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesFirstChart().size(), is(22));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesFirstChart().get(0), is(Arrays.asList(0, 0, 1)));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesFirstChart().get(1), is(Arrays.asList(0, 1, 2)));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesFirstChart().get(2), is(Arrays.asList(0, 2, 3)));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesFirstChart().get(3), is(Arrays.asList(0, 3, 5)));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesFirstChart().get(4), is(Arrays.asList(0, 4, 1)));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesFirstChart().get(5), is(Arrays.asList(0, 5, 1)));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesFirstChart().get(6), is(Arrays.asList(0, 6, 1)));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesFirstChart().get(7), is(Arrays.asList(0, 7, 1)));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesFirstChart().get(8), is(Arrays.asList(0, 8, 1)));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesFirstChart().get(9), is(Arrays.asList(0, 9, 1)));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesFirstChart().get(10), is(Arrays.asList(0, 10, 1)));

        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesFirstChart().get(11), is(Arrays.asList(1, 0, 2)));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesFirstChart().get(12), is(Arrays.asList(1, 1, 4)));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesFirstChart().get(13), is(Arrays.asList(1, 2, 6)));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesFirstChart().get(14), is(Arrays.asList(1, 3, 10)));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesFirstChart().get(15), is(Arrays.asList(1, 4, 2)));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesFirstChart().get(16), is(Arrays.asList(1, 5, 2)));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesFirstChart().get(17), is(Arrays.asList(1, 6, 2)));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesFirstChart().get(18), is(Arrays.asList(1, 7, 2)));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesFirstChart().get(19), is(Arrays.asList(1, 8, 2)));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesFirstChart().get(20), is(Arrays.asList(1, 9, 2)));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesFirstChart().get(21), is(Arrays.asList(1, 10, 2)));

        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesSecondChart().size(), is(22));

        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesSecondChart().get(0), is(Arrays.asList(0, 0, 3)));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesSecondChart().get(1), is(Arrays.asList(0, 1, 6)));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesSecondChart().get(2), is(Arrays.asList(0, 2, 11)));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesSecondChart().get(3), is(Arrays.asList(0, 3, 17)));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesSecondChart().get(4), is(Arrays.asList(0, 4, 3)));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesSecondChart().get(5), is(Arrays.asList(0, 5, 3)));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesSecondChart().get(6), is(Arrays.asList(0, 6, 3)));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesSecondChart().get(7), is(Arrays.asList(0, 7, 5)));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesSecondChart().get(8), is(Arrays.asList(0, 8, 3)));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesSecondChart().get(9), is(Arrays.asList(0, 9, 3)));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesSecondChart().get(10), is(Arrays.asList(0, 10, 3)));

        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesSecondChart().get(11), is(Arrays.asList(1, 0, 4)));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesSecondChart().get(12), is(Arrays.asList(1, 1, 8)));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesSecondChart().get(13), is(Arrays.asList(1, 2, 14)));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesSecondChart().get(14), is(Arrays.asList(1, 3, 22)));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesSecondChart().get(15), is(Arrays.asList(1, 4, 4)));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesSecondChart().get(16), is(Arrays.asList(1, 5, 4)));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesSecondChart().get(17), is(Arrays.asList(1, 6, 4)));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesSecondChart().get(18), is(Arrays.asList(1, 7, 6)));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesSecondChart().get(19), is(Arrays.asList(1, 8, 4)));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesSecondChart().get(20), is(Arrays.asList(1, 9, 4)));
        assertThat(ladeZaehldatenHeatmap.getSeriesEntriesSecondChart().get(21), is(Arrays.asList(1, 10, 4)));
    }

    @Test
    public void splitSeriesEntriesIntoFirstChartAndSecondChartIfNecessaryInLadeZaehldatenHeatmap() {

        OptionsDTO options = new OptionsDTO();
        options.setZeitblock(Zeitblock.ZB_00_24);
        options.setZaehldauer(Zaehldauer.DAUER_2_X_4_STUNDEN);
        options.setZeitauswahl(LadeZaehldatenService.BLOCK);

        LadeZaehldatenHeatmapDTO ladeZaehldatenHeatmapDTO = new LadeZaehldatenHeatmapDTO();
        List<String> xAxisData = new ArrayList<>();
        xAxisData.add("06:00");
        xAxisData.add("07:00");
        xAxisData.add("08:00");
        xAxisData.add("09:00");
        ladeZaehldatenHeatmapDTO.setXAxisDataFirstChart(xAxisData);

        List<List<Integer>> seriesEntries = new ArrayList<>();
        seriesEntries.add(Arrays.asList(0, 0, 4));
        seriesEntries.add(Arrays.asList(0, 1, 5));
        seriesEntries.add(Arrays.asList(1, 0, 6));
        seriesEntries.add(Arrays.asList(1, 1, 7));
        ladeZaehldatenHeatmapDTO.setSeriesEntriesFirstChart(seriesEntries);

        ProcessZaehldatenHeatmapService.splitSeriesEntriesIntoFirstChartAndSecondChartIfNecessaryInLadeZaehldatenHeatmap(ladeZaehldatenHeatmapDTO, options);

        assertThat(ladeZaehldatenHeatmapDTO.getXAxisDataFirstChart().size(), is(2));
        assertThat(ladeZaehldatenHeatmapDTO.getXAxisDataFirstChart().get(0), is("06:00"));
        assertThat(ladeZaehldatenHeatmapDTO.getXAxisDataFirstChart().get(1), is("07:00"));

        assertThat(ladeZaehldatenHeatmapDTO.getXAxisDataSecondChart().size(), is(2));
        assertThat(ladeZaehldatenHeatmapDTO.getXAxisDataSecondChart().get(0), is("08:00"));
        assertThat(ladeZaehldatenHeatmapDTO.getXAxisDataSecondChart().get(1), is("09:00"));

        assertThat(ladeZaehldatenHeatmapDTO.getSeriesEntriesFirstChart().size(), is(2));
        assertThat(ladeZaehldatenHeatmapDTO.getSeriesEntriesFirstChart().get(0), is(Arrays.asList(0, 0, 4)));
        assertThat(ladeZaehldatenHeatmapDTO.getSeriesEntriesFirstChart().get(1), is(Arrays.asList(0, 1, 5)));

        assertThat(ladeZaehldatenHeatmapDTO.getSeriesEntriesSecondChart().size(), is(2));
        assertThat(ladeZaehldatenHeatmapDTO.getSeriesEntriesSecondChart().get(0), is(Arrays.asList(0, 0, 6)));
        assertThat(ladeZaehldatenHeatmapDTO.getSeriesEntriesSecondChart().get(1), is(Arrays.asList(0, 1, 7)));

    }

}
