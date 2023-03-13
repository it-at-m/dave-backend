package de.muenchen.dave.services.processzaehldaten;

import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatenSteplineDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatenTableDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatumDTO;
import de.muenchen.dave.domain.dtos.laden.StepLineSeriesEntryBaseDTO;
import de.muenchen.dave.domain.dtos.laden.StepLineSeriesEntryBigDecimalDTO;
import de.muenchen.dave.domain.dtos.laden.StepLineSeriesEntryIntegerDTO;
import de.muenchen.dave.domain.enums.Zaehldauer;
import de.muenchen.dave.domain.enums.Zeitblock;
import de.muenchen.dave.services.ladezaehldaten.LadeZaehldatenService;
import de.muenchen.dave.util.ChartLegendUtil;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ProcessZaehldatenSteplineServiceTest {

    private ProcessZaehldatenSteplineService processZaehldatenSteplineService = new ProcessZaehldatenSteplineService();

    private LadeZaehldatenTableDTO ladeZaehldatenTable;

    private static void setDataInZaehldatum(final LadeZaehldatumDTO ladeZaehldatum, final Integer value) {
        ladeZaehldatum.setPkw(value);
        ladeZaehldatum.setLkw(value);
        ladeZaehldatum.setLastzuege(value);
        ladeZaehldatum.setBusse(value);
        ladeZaehldatum.setKraftraeder(value);
        ladeZaehldatum.setFahrradfahrer(value);
        ladeZaehldatum.setFussgaenger(value);
        ladeZaehldatum.setPkwEinheiten(value);
    }

    @BeforeEach
    public void beforeEach() {
        ladeZaehldatenTable = new LadeZaehldatenTableDTO();

        final LadeZaehldatumDTO ladeZaehldatum1 = new LadeZaehldatumDTO();
        ladeZaehldatum1.setType("");
        ladeZaehldatum1.setStartUhrzeit(LocalTime.of(7, 0));
        ladeZaehldatum1.setEndeUhrzeit(LocalTime.of(8, 0));
        setDataInZaehldatum(ladeZaehldatum1, 1);


        final LadeZaehldatumDTO ladeZaehldatum2 = new LadeZaehldatumDTO();
        ladeZaehldatum2.setType("");
        ladeZaehldatum2.setStartUhrzeit(LocalTime.of(8, 0));
        ladeZaehldatum2.setEndeUhrzeit(LocalTime.of(9, 0));
        setDataInZaehldatum(ladeZaehldatum2, 1);

        final LadeZaehldatumDTO ladeZaehldatum3 = new LadeZaehldatumDTO();
        ladeZaehldatum3.setType("");
        ladeZaehldatum3.setStartUhrzeit(LocalTime.of(9, 0));
        ladeZaehldatum3.setEndeUhrzeit(LocalTime.of(10, 0));
        setDataInZaehldatum(ladeZaehldatum3, 1);

        final LadeZaehldatumDTO ladeZaehldatum4 = new LadeZaehldatumDTO();
        ladeZaehldatum4.setType(LadeZaehldatenService.BLOCK);

        final LadeZaehldatumDTO ladeZaehldatum5 = new LadeZaehldatumDTO();
        ladeZaehldatum5.setType("");
        ladeZaehldatum5.setStartUhrzeit(LocalTime.of(15, 0));
        ladeZaehldatum5.setEndeUhrzeit(LocalTime.of(16, 0));
        setDataInZaehldatum(ladeZaehldatum5, 2);

        final LadeZaehldatumDTO ladeZaehldatum6 = new LadeZaehldatumDTO();
        ladeZaehldatum6.setType("");
        ladeZaehldatum6.setStartUhrzeit(LocalTime.of(16, 0));
        ladeZaehldatum6.setEndeUhrzeit(LocalTime.of(17, 0));
        setDataInZaehldatum(ladeZaehldatum6, 2);

        final LadeZaehldatumDTO ladeZaehldatum7 = new LadeZaehldatumDTO();
        ladeZaehldatum7.setType("");
        ladeZaehldatum7.setStartUhrzeit(LocalTime.of(17, 0));
        ladeZaehldatum7.setEndeUhrzeit(LocalTime.of(18, 0));
        setDataInZaehldatum(ladeZaehldatum7, 2);

        final LadeZaehldatumDTO ladeZaehldatum8 = new LadeZaehldatumDTO();
        ladeZaehldatum8.setType(LadeZaehldatenService.BLOCK);

        ladeZaehldatenTable.setZaehldaten(Arrays.asList(
                ladeZaehldatum1,
                ladeZaehldatum2,
                ladeZaehldatum3,
                ladeZaehldatum4,
                ladeZaehldatum5,
                ladeZaehldatum6,
                ladeZaehldatum7,
                ladeZaehldatum8));
    }

    @Test
    public void ladeProcessedZaehldatenSteplineAllDataSingleTable() {
        final OptionsDTO options = new OptionsDTO();
        options.setZeitblock(Zeitblock.ZB_00_24);
        options.setZaehldauer(Zaehldauer.DAUER_24_STUNDEN);
        options.setPersonenkraftwagen(true);
        options.setLastkraftwagen(true);
        options.setLastzuege(true);
        options.setBusse(true);
        options.setKraftraeder(true);
        options.setRadverkehr(true);
        options.setFussverkehr(true);

        options.setKraftfahrzeugverkehr(true);
        options.setSchwerverkehr(true);
        options.setSchwerverkehrsanteilProzent(true);
        options.setGueterverkehr(true);
        options.setGueterverkehrsanteilProzent(true);
        options.setPkwEinheiten(true);

        final LadeZaehldatenSteplineDTO ladeZaehldatenStepline = processZaehldatenSteplineService.ladeProcessedZaehldatenStepline(
                ladeZaehldatenTable,
                options);

        assertThat(ladeZaehldatenStepline.getLegend().size(), is(13));
        assertThat(ladeZaehldatenStepline.getLegend().get(0), is(ChartLegendUtil.PKW));
        assertThat(ladeZaehldatenStepline.getLegend().get(1), is(ChartLegendUtil.LKW));
        assertThat(ladeZaehldatenStepline.getLegend().get(2), is(ChartLegendUtil.LASTZUEGE));
        assertThat(ladeZaehldatenStepline.getLegend().get(3), is(ChartLegendUtil.BUSSE));
        assertThat(ladeZaehldatenStepline.getLegend().get(4), is(ChartLegendUtil.KRAFTRAEDER));
        assertThat(ladeZaehldatenStepline.getLegend().get(5), is(ChartLegendUtil.RAD));
        assertThat(ladeZaehldatenStepline.getLegend().get(6), is(ChartLegendUtil.FUSSGAENGER));
        assertThat(ladeZaehldatenStepline.getLegend().get(7), is(ChartLegendUtil.KFZ));
        assertThat(ladeZaehldatenStepline.getLegend().get(8), is(ChartLegendUtil.SCHWERVERKEHR));
        assertThat(ladeZaehldatenStepline.getLegend().get(9), is(ChartLegendUtil.SCHWERVERKEHR_ANTEIL_PROZENT));
        assertThat(ladeZaehldatenStepline.getLegend().get(10), is(ChartLegendUtil.GUETERVERKEHR));
        assertThat(ladeZaehldatenStepline.getLegend().get(11), is(ChartLegendUtil.GUETERVERKEHR_ANTEIL_PROZENT));
        assertThat(ladeZaehldatenStepline.getLegend().get(12), is(ChartLegendUtil.PKW_EINHEITEN));

        assertThat(ladeZaehldatenStepline.getRangeMax(), is(20));
        assertThat(ladeZaehldatenStepline.getRangeMaxPercent(), is(60));

        assertThat(ladeZaehldatenStepline.getXAxisDataFirstChart().size(), is(6));
        assertThat(ladeZaehldatenStepline.getXAxisDataFirstChart().get(0), is("07:00"));
        assertThat(ladeZaehldatenStepline.getXAxisDataFirstChart().get(1), is("08:00"));
        assertThat(ladeZaehldatenStepline.getXAxisDataFirstChart().get(2), is("09:00"));
        assertThat(ladeZaehldatenStepline.getXAxisDataFirstChart().get(3), is("15:00"));
        assertThat(ladeZaehldatenStepline.getXAxisDataFirstChart().get(4), is("16:00"));
        assertThat(ladeZaehldatenStepline.getXAxisDataFirstChart().get(5), is("17:00"));

        assertThat(ladeZaehldatenStepline.getSeriesEntriesFirstChart().size(), is(13));
        assertThat(ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(0).getName(), is(ChartLegendUtil.PKW));
        assertThat(((StepLineSeriesEntryIntegerDTO) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(0)).getYAxisData().size(), is(6));
        assertThat(((StepLineSeriesEntryIntegerDTO) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(0)).getYAxisData().get(0), is(1));
        assertThat(((StepLineSeriesEntryIntegerDTO) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(0)).getYAxisData().get(3), is(2));
        assertThat(((StepLineSeriesEntryIntegerDTO) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(0)).getXAxisIndex(), is(0));
        assertThat(((StepLineSeriesEntryIntegerDTO) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(0)).getYAxisIndex(), is(0));

        assertThat(ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(9).getName(), is(ChartLegendUtil.SCHWERVERKEHR_ANTEIL_PROZENT));
        assertThat(((StepLineSeriesEntryBigDecimalDTO) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(9)).getYAxisData().size(), is(6));
        assertThat(((StepLineSeriesEntryBigDecimalDTO) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(9)).getYAxisData().get(0), is(BigDecimal.valueOf(60.0)));
        assertThat(((StepLineSeriesEntryBigDecimalDTO) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(9)).getYAxisData().get(3), is(BigDecimal.valueOf(60.0)));
        assertThat(((StepLineSeriesEntryBigDecimalDTO) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(9)).getXAxisIndex(), is(0));
        assertThat(((StepLineSeriesEntryBigDecimalDTO) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(9)).getYAxisIndex(), is(1));

        assertThat(ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(12).getName(), is(ChartLegendUtil.PKW_EINHEITEN));
        assertThat(((StepLineSeriesEntryIntegerDTO) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(12)).getYAxisData().size(), is(6));
        assertThat(((StepLineSeriesEntryIntegerDTO) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(12)).getYAxisData().get(0), is(1));
        assertThat(((StepLineSeriesEntryIntegerDTO) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(12)).getYAxisData().get(3), is(2));
        assertThat(((StepLineSeriesEntryIntegerDTO) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(12)).getXAxisIndex(), is(0));
        assertThat(((StepLineSeriesEntryIntegerDTO) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(12)).getYAxisIndex(), is(0));
    }

    @Test
    public void ladeProcessedZaehldatenSteplineAllDataSplittetTable() {
        final OptionsDTO options = new OptionsDTO();
        options.setZeitblock(Zeitblock.ZB_00_24);
        options.setZaehldauer(Zaehldauer.DAUER_2_X_4_STUNDEN);
        options.setPersonenkraftwagen(true);
        options.setLastkraftwagen(true);
        options.setLastzuege(true);
        options.setBusse(true);
        options.setKraftraeder(true);
        options.setRadverkehr(true);
        options.setFussverkehr(true);

        options.setKraftfahrzeugverkehr(true);
        options.setSchwerverkehr(true);
        options.setSchwerverkehrsanteilProzent(true);
        options.setGueterverkehr(true);
        options.setGueterverkehrsanteilProzent(true);
        options.setPkwEinheiten(true);

        final LadeZaehldatenSteplineDTO ladeZaehldatenStepline = processZaehldatenSteplineService.ladeProcessedZaehldatenStepline(
                ladeZaehldatenTable,
                options);

        assertThat(ladeZaehldatenStepline.getLegend().size(), is(13));
        assertThat(ladeZaehldatenStepline.getLegend().get(0), is(ChartLegendUtil.PKW));
        assertThat(ladeZaehldatenStepline.getLegend().get(1), is(ChartLegendUtil.LKW));
        assertThat(ladeZaehldatenStepline.getLegend().get(2), is(ChartLegendUtil.LASTZUEGE));
        assertThat(ladeZaehldatenStepline.getLegend().get(3), is(ChartLegendUtil.BUSSE));
        assertThat(ladeZaehldatenStepline.getLegend().get(4), is(ChartLegendUtil.KRAFTRAEDER));
        assertThat(ladeZaehldatenStepline.getLegend().get(5), is(ChartLegendUtil.RAD));
        assertThat(ladeZaehldatenStepline.getLegend().get(6), is(ChartLegendUtil.FUSSGAENGER));
        assertThat(ladeZaehldatenStepline.getLegend().get(7), is(ChartLegendUtil.KFZ));
        assertThat(ladeZaehldatenStepline.getLegend().get(8), is(ChartLegendUtil.SCHWERVERKEHR));
        assertThat(ladeZaehldatenStepline.getLegend().get(9), is(ChartLegendUtil.SCHWERVERKEHR_ANTEIL_PROZENT));
        assertThat(ladeZaehldatenStepline.getLegend().get(10), is(ChartLegendUtil.GUETERVERKEHR));
        assertThat(ladeZaehldatenStepline.getLegend().get(11), is(ChartLegendUtil.GUETERVERKEHR_ANTEIL_PROZENT));
        assertThat(ladeZaehldatenStepline.getLegend().get(12), is(ChartLegendUtil.PKW_EINHEITEN));

        assertThat(ladeZaehldatenStepline.getRangeMax(), is(20));
        assertThat(ladeZaehldatenStepline.getRangeMaxPercent(), is(60));

        assertThat(ladeZaehldatenStepline.getXAxisDataFirstChart().size(), is(3));
        assertThat(ladeZaehldatenStepline.getXAxisDataFirstChart().get(0), is("07:00"));
        assertThat(ladeZaehldatenStepline.getXAxisDataFirstChart().get(1), is("08:00"));
        assertThat(ladeZaehldatenStepline.getXAxisDataFirstChart().get(2), is("09:00"));

        assertThat(ladeZaehldatenStepline.getXAxisDataSecondChart().size(), is(3));
        assertThat(ladeZaehldatenStepline.getXAxisDataSecondChart().get(0), is("15:00"));
        assertThat(ladeZaehldatenStepline.getXAxisDataSecondChart().get(1), is("16:00"));
        assertThat(ladeZaehldatenStepline.getXAxisDataSecondChart().get(2), is("17:00"));

        assertThat(ladeZaehldatenStepline.getSeriesEntriesFirstChart().size(), is(13));
        assertThat(ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(0).getName(), is(ChartLegendUtil.PKW));
        assertThat(((StepLineSeriesEntryIntegerDTO) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(0)).getYAxisData().size(), is(3));
        assertThat(((StepLineSeriesEntryIntegerDTO) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(0)).getYAxisData().get(0), is(1));
        assertThat(((StepLineSeriesEntryIntegerDTO) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(0)).getYAxisData().get(2), is(1));
        assertThat(((StepLineSeriesEntryIntegerDTO) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(0)).getXAxisIndex(), is(0));
        assertThat(((StepLineSeriesEntryIntegerDTO) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(0)).getYAxisIndex(), is(0));

        assertThat(ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(9).getName(), is(ChartLegendUtil.SCHWERVERKEHR_ANTEIL_PROZENT));
        assertThat(((StepLineSeriesEntryBigDecimalDTO) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(9)).getYAxisData().size(), is(3));
        assertThat(((StepLineSeriesEntryBigDecimalDTO) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(9)).getYAxisData().get(0), is(BigDecimal.valueOf(60.0)));
        assertThat(((StepLineSeriesEntryBigDecimalDTO) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(9)).getYAxisData().get(2), is(BigDecimal.valueOf(60.0)));
        assertThat(((StepLineSeriesEntryBigDecimalDTO) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(9)).getXAxisIndex(), is(0));
        assertThat(((StepLineSeriesEntryBigDecimalDTO) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(9)).getYAxisIndex(), is(1));

        assertThat(ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(12).getName(), is(ChartLegendUtil.PKW_EINHEITEN));
        assertThat(((StepLineSeriesEntryIntegerDTO) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(12)).getYAxisData().size(), is(3));
        assertThat(((StepLineSeriesEntryIntegerDTO) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(12)).getYAxisData().get(0), is(1));
        assertThat(((StepLineSeriesEntryIntegerDTO) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(12)).getYAxisData().get(2), is(1));
        assertThat(((StepLineSeriesEntryIntegerDTO) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(12)).getXAxisIndex(), is(0));
        assertThat(((StepLineSeriesEntryIntegerDTO) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(12)).getYAxisIndex(), is(0));

        assertThat(ladeZaehldatenStepline.getSeriesEntriesSecondChart().size(), is(13));
        assertThat(ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(0).getName(), is(ChartLegendUtil.PKW));
        assertThat(((StepLineSeriesEntryIntegerDTO) ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(0)).getYAxisData().size(), is(3));
        assertThat(((StepLineSeriesEntryIntegerDTO) ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(0)).getYAxisData().get(0), is(2));
        assertThat(((StepLineSeriesEntryIntegerDTO) ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(0)).getYAxisData().get(2), is(2));
        assertThat(((StepLineSeriesEntryIntegerDTO) ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(0)).getXAxisIndex(), is(1));
        assertThat(((StepLineSeriesEntryIntegerDTO) ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(0)).getYAxisIndex(), is(2));

        assertThat(ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(9).getName(), is(ChartLegendUtil.SCHWERVERKEHR_ANTEIL_PROZENT));
        assertThat(((StepLineSeriesEntryBigDecimalDTO) ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(9)).getYAxisData().size(), is(3));
        assertThat(((StepLineSeriesEntryBigDecimalDTO) ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(9)).getYAxisData().get(0), is(BigDecimal.valueOf(60.0)));
        assertThat(((StepLineSeriesEntryBigDecimalDTO) ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(9)).getYAxisData().get(2), is(BigDecimal.valueOf(60.0)));
        assertThat(((StepLineSeriesEntryBigDecimalDTO) ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(9)).getXAxisIndex(), is(1));
        assertThat(((StepLineSeriesEntryBigDecimalDTO) ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(9)).getYAxisIndex(), is(3));

        assertThat(ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(12).getName(), is(ChartLegendUtil.PKW_EINHEITEN));
        assertThat(((StepLineSeriesEntryIntegerDTO) ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(12)).getYAxisData().size(), is(3));
        assertThat(((StepLineSeriesEntryIntegerDTO) ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(12)).getYAxisData().get(0), is(2));
        assertThat(((StepLineSeriesEntryIntegerDTO) ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(12)).getYAxisData().get(2), is(2));
        assertThat(((StepLineSeriesEntryIntegerDTO) ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(12)).getXAxisIndex(), is(1));
        assertThat(((StepLineSeriesEntryIntegerDTO) ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(12)).getYAxisIndex(), is(2));
    }

    @Test
    public void ladeProcessedZaehldatenSteplineWithoutPkwEinheitenSplittetTable() {
        final OptionsDTO options = new OptionsDTO();
        options.setZeitblock(Zeitblock.ZB_00_24);
        options.setZaehldauer(Zaehldauer.DAUER_2_X_4_STUNDEN);
        options.setPersonenkraftwagen(true);
        options.setLastkraftwagen(true);
        options.setLastzuege(true);
        options.setBusse(true);
        options.setKraftraeder(true);
        options.setRadverkehr(true);
        options.setFussverkehr(true);

        options.setKraftfahrzeugverkehr(true);
        options.setSchwerverkehr(true);
        options.setSchwerverkehrsanteilProzent(true);
        options.setGueterverkehr(true);
        options.setGueterverkehrsanteilProzent(true);
        options.setPkwEinheiten(false);

        final LadeZaehldatenSteplineDTO ladeZaehldatenStepline = processZaehldatenSteplineService.ladeProcessedZaehldatenStepline(
                ladeZaehldatenTable,
                options);

        assertThat(ladeZaehldatenStepline.getLegend().size(), is(12));
        assertThat(ladeZaehldatenStepline.getLegend().get(0), is(ChartLegendUtil.PKW));
        assertThat(ladeZaehldatenStepline.getLegend().get(1), is(ChartLegendUtil.LKW));
        assertThat(ladeZaehldatenStepline.getLegend().get(2), is(ChartLegendUtil.LASTZUEGE));
        assertThat(ladeZaehldatenStepline.getLegend().get(3), is(ChartLegendUtil.BUSSE));
        assertThat(ladeZaehldatenStepline.getLegend().get(4), is(ChartLegendUtil.KRAFTRAEDER));
        assertThat(ladeZaehldatenStepline.getLegend().get(5), is(ChartLegendUtil.RAD));
        assertThat(ladeZaehldatenStepline.getLegend().get(6), is(ChartLegendUtil.FUSSGAENGER));
        assertThat(ladeZaehldatenStepline.getLegend().get(7), is(ChartLegendUtil.KFZ));
        assertThat(ladeZaehldatenStepline.getLegend().get(8), is(ChartLegendUtil.SCHWERVERKEHR));
        assertThat(ladeZaehldatenStepline.getLegend().get(9), is(ChartLegendUtil.SCHWERVERKEHR_ANTEIL_PROZENT));
        assertThat(ladeZaehldatenStepline.getLegend().get(10), is(ChartLegendUtil.GUETERVERKEHR));
        assertThat(ladeZaehldatenStepline.getLegend().get(11), is(ChartLegendUtil.GUETERVERKEHR_ANTEIL_PROZENT));

        assertThat(ladeZaehldatenStepline.getRangeMax(), is(20));
        assertThat(ladeZaehldatenStepline.getRangeMaxPercent(), is(60));

        assertThat(ladeZaehldatenStepline.getXAxisDataFirstChart().size(), is(3));
        assertThat(ladeZaehldatenStepline.getXAxisDataFirstChart().get(0), is("07:00"));
        assertThat(ladeZaehldatenStepline.getXAxisDataFirstChart().get(1), is("08:00"));
        assertThat(ladeZaehldatenStepline.getXAxisDataFirstChart().get(2), is("09:00"));

        assertThat(ladeZaehldatenStepline.getXAxisDataSecondChart().size(), is(3));
        assertThat(ladeZaehldatenStepline.getXAxisDataSecondChart().get(0), is("15:00"));
        assertThat(ladeZaehldatenStepline.getXAxisDataSecondChart().get(1), is("16:00"));
        assertThat(ladeZaehldatenStepline.getXAxisDataSecondChart().get(2), is("17:00"));

        assertThat(ladeZaehldatenStepline.getSeriesEntriesFirstChart().size(), is(12));
        assertThat(ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(0).getName(), is(ChartLegendUtil.PKW));
        assertThat(((StepLineSeriesEntryIntegerDTO) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(0)).getYAxisData().size(), is(3));
        assertThat(((StepLineSeriesEntryIntegerDTO) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(0)).getYAxisData().get(0), is(1));
        assertThat(((StepLineSeriesEntryIntegerDTO) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(0)).getYAxisData().get(2), is(1));
        assertThat(((StepLineSeriesEntryIntegerDTO) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(0)).getXAxisIndex(), is(0));
        assertThat(((StepLineSeriesEntryIntegerDTO) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(0)).getYAxisIndex(), is(0));

        assertThat(ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(9).getName(), is(ChartLegendUtil.SCHWERVERKEHR_ANTEIL_PROZENT));
        assertThat(((StepLineSeriesEntryBigDecimalDTO) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(9)).getYAxisData().size(), is(3));
        assertThat(((StepLineSeriesEntryBigDecimalDTO) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(9)).getYAxisData().get(0), is(BigDecimal.valueOf(60.0)));
        assertThat(((StepLineSeriesEntryBigDecimalDTO) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(9)).getYAxisData().get(2), is(BigDecimal.valueOf(60.0)));
        assertThat(((StepLineSeriesEntryBigDecimalDTO) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(9)).getXAxisIndex(), is(0));
        assertThat(((StepLineSeriesEntryBigDecimalDTO) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(9)).getYAxisIndex(), is(1));

        assertThat(ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(11).getName(), is(ChartLegendUtil.GUETERVERKEHR_ANTEIL_PROZENT));
        assertThat(((StepLineSeriesEntryBigDecimalDTO) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(11)).getYAxisData().size(), is(3));
        assertThat(((StepLineSeriesEntryBigDecimalDTO) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(11)).getYAxisData().get(0), is(BigDecimal.valueOf(40.0)));
        assertThat(((StepLineSeriesEntryBigDecimalDTO) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(11)).getYAxisData().get(2), is(BigDecimal.valueOf(40.0)));
        assertThat(((StepLineSeriesEntryBigDecimalDTO) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(11)).getXAxisIndex(), is(0));
        assertThat(((StepLineSeriesEntryBigDecimalDTO) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(11)).getYAxisIndex(), is(1));


        assertThat(ladeZaehldatenStepline.getSeriesEntriesSecondChart().size(), is(12));
        assertThat(ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(0).getName(), is(ChartLegendUtil.PKW));
        assertThat(((StepLineSeriesEntryIntegerDTO) ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(0)).getYAxisData().size(), is(3));
        assertThat(((StepLineSeriesEntryIntegerDTO) ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(0)).getYAxisData().get(0), is(2));
        assertThat(((StepLineSeriesEntryIntegerDTO) ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(0)).getYAxisData().get(2), is(2));
        assertThat(((StepLineSeriesEntryIntegerDTO) ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(0)).getXAxisIndex(), is(1));
        assertThat(((StepLineSeriesEntryIntegerDTO) ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(0)).getYAxisIndex(), is(2));

        assertThat(ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(9).getName(), is(ChartLegendUtil.SCHWERVERKEHR_ANTEIL_PROZENT));
        assertThat(((StepLineSeriesEntryBigDecimalDTO) ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(9)).getYAxisData().size(), is(3));
        assertThat(((StepLineSeriesEntryBigDecimalDTO) ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(9)).getYAxisData().get(0), is(BigDecimal.valueOf(60.0)));
        assertThat(((StepLineSeriesEntryBigDecimalDTO) ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(9)).getYAxisData().get(2), is(BigDecimal.valueOf(60.0)));
        assertThat(((StepLineSeriesEntryBigDecimalDTO) ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(9)).getXAxisIndex(), is(1));
        assertThat(((StepLineSeriesEntryBigDecimalDTO) ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(9)).getYAxisIndex(), is(3));

        assertThat(ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(11).getName(), is(ChartLegendUtil.GUETERVERKEHR_ANTEIL_PROZENT));
        assertThat(((StepLineSeriesEntryBigDecimalDTO) ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(11)).getYAxisData().size(), is(3));
        assertThat(((StepLineSeriesEntryBigDecimalDTO) ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(11)).getYAxisData().get(0), is(BigDecimal.valueOf(40.0)));
        assertThat(((StepLineSeriesEntryBigDecimalDTO) ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(11)).getYAxisData().get(2), is(BigDecimal.valueOf(40.0)));
        assertThat(((StepLineSeriesEntryBigDecimalDTO) ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(11)).getXAxisIndex(), is(1));
        assertThat(((StepLineSeriesEntryBigDecimalDTO) ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(11)).getYAxisIndex(), is(3));
    }

    @Test
    public void setRangeMaxRoundedToTenInZaehldatenSteplineInteger() {
        final LadeZaehldatenSteplineDTO ladeZaehldatenStepline = new LadeZaehldatenSteplineDTO();
        ladeZaehldatenStepline.setRangeMax(0);

        ProcessZaehldatenSteplineService.setRangeMaxRoundedToHundredInZaehldatenStepline(ladeZaehldatenStepline, 42);
        assertThat(ladeZaehldatenStepline.getRangeMax(), is(60));

        ladeZaehldatenStepline.setRangeMax(300);
        ProcessZaehldatenSteplineService.setRangeMaxRoundedToHundredInZaehldatenStepline(ladeZaehldatenStepline, 100);
        assertThat(ladeZaehldatenStepline.getRangeMax(), is(300));

        ladeZaehldatenStepline.setRangeMax(158);
        ProcessZaehldatenSteplineService.setRangeMaxRoundedToHundredInZaehldatenStepline(ladeZaehldatenStepline, 120);
        assertThat(ladeZaehldatenStepline.getRangeMax(), is(160));

        final Integer value = null;
        ladeZaehldatenStepline.setRangeMax(300);
        ProcessZaehldatenSteplineService.setRangeMaxRoundedToHundredInZaehldatenStepline(ladeZaehldatenStepline, value);
        assertThat(ladeZaehldatenStepline.getRangeMax(), is(300));

        ladeZaehldatenStepline.setRangeMax(158);
        ProcessZaehldatenSteplineService.setRangeMaxRoundedToHundredInZaehldatenStepline(ladeZaehldatenStepline, value);
        assertThat(ladeZaehldatenStepline.getRangeMax(), is(160));
    }

    @Test
    public void setRangeMaxRoundedToTenInZaehldatenSteplineBigDecimal() {
        final LadeZaehldatenSteplineDTO ladeZaehldatenStepline = new LadeZaehldatenSteplineDTO();
        ladeZaehldatenStepline.setRangeMax(0);

        ProcessZaehldatenSteplineService.setRangeMaxRoundedToHundredInZaehldatenStepline(ladeZaehldatenStepline, BigDecimal.valueOf(42));
        assertThat(ladeZaehldatenStepline.getRangeMax(), is(60));

        ladeZaehldatenStepline.setRangeMax(300);
        ProcessZaehldatenSteplineService.setRangeMaxRoundedToHundredInZaehldatenStepline(ladeZaehldatenStepline, BigDecimal.valueOf(100));
        assertThat(ladeZaehldatenStepline.getRangeMax(), is(300));

        ladeZaehldatenStepline.setRangeMax(158);
        ProcessZaehldatenSteplineService.setRangeMaxRoundedToHundredInZaehldatenStepline(ladeZaehldatenStepline, BigDecimal.valueOf(120));
        assertThat(ladeZaehldatenStepline.getRangeMax(), is(160));

        final BigDecimal value = null;
        ladeZaehldatenStepline.setRangeMax(300);
        ProcessZaehldatenSteplineService.setRangeMaxRoundedToHundredInZaehldatenStepline(ladeZaehldatenStepline, value);
        assertThat(ladeZaehldatenStepline.getRangeMax(), is(300));

        ladeZaehldatenStepline.setRangeMax(158);
        ProcessZaehldatenSteplineService.setRangeMaxRoundedToHundredInZaehldatenStepline(ladeZaehldatenStepline, value);
        assertThat(ladeZaehldatenStepline.getRangeMax(), is(160));
    }

    @Test
    public void setRangeMaxPercentInZaehldatenStepline() {
        final LadeZaehldatenSteplineDTO ladeZaehldatenStepline = new LadeZaehldatenSteplineDTO();
        ladeZaehldatenStepline.setRangeMaxPercent(0);

        ProcessZaehldatenSteplineService.setRangeMaxPercentInZaehldatenStepline(ladeZaehldatenStepline, BigDecimal.valueOf(1));
        assertThat(ladeZaehldatenStepline.getRangeMaxPercent(), is(2));

        ladeZaehldatenStepline.setRangeMaxPercent(30);
        ProcessZaehldatenSteplineService.setRangeMaxPercentInZaehldatenStepline(ladeZaehldatenStepline, BigDecimal.valueOf(12));
        assertThat(ladeZaehldatenStepline.getRangeMaxPercent(), is(30));

        ladeZaehldatenStepline.setRangeMaxPercent(16);
        ProcessZaehldatenSteplineService.setRangeMaxPercentInZaehldatenStepline(ladeZaehldatenStepline, BigDecimal.valueOf(12));
        assertThat(ladeZaehldatenStepline.getRangeMaxPercent(), is(16));

        final BigDecimal value = null;
        ladeZaehldatenStepline.setRangeMaxPercent(30);
        ProcessZaehldatenSteplineService.setRangeMaxPercentInZaehldatenStepline(ladeZaehldatenStepline, value);
        assertThat(ladeZaehldatenStepline.getRangeMaxPercent(), is(30));

        ladeZaehldatenStepline.setRangeMaxPercent(16);
        ProcessZaehldatenSteplineService.setRangeMaxPercentInZaehldatenStepline(ladeZaehldatenStepline, value);
        assertThat(ladeZaehldatenStepline.getRangeMaxPercent(), is(16));

    }

    @Test
    public void setLegendInZaehldatenStepline() {
        final LadeZaehldatenSteplineDTO ladeZaehldatenStepline = new LadeZaehldatenSteplineDTO();
        ladeZaehldatenStepline.setLegend(new ArrayList<>());

        ProcessZaehldatenSteplineService.setLegendInZaehldatenStepline(ladeZaehldatenStepline, "B");
        assertThat(ladeZaehldatenStepline.getLegend(), is(Arrays.asList("B")));

        ProcessZaehldatenSteplineService.setLegendInZaehldatenStepline(ladeZaehldatenStepline, "C");
        assertThat(ladeZaehldatenStepline.getLegend(), is(Arrays.asList("B", "C")));

        ProcessZaehldatenSteplineService.setLegendInZaehldatenStepline(ladeZaehldatenStepline, "A");
        assertThat(ladeZaehldatenStepline.getLegend(), is(Arrays.asList("B", "C", "A")));

        ProcessZaehldatenSteplineService.setLegendInZaehldatenStepline(ladeZaehldatenStepline, "A");
        assertThat(ladeZaehldatenStepline.getLegend(), is(Arrays.asList("B", "C", "A")));

        ProcessZaehldatenSteplineService.setLegendInZaehldatenStepline(ladeZaehldatenStepline, "B");
        assertThat(ladeZaehldatenStepline.getLegend(), is(Arrays.asList("B", "C", "A")));

        ProcessZaehldatenSteplineService.setLegendInZaehldatenStepline(ladeZaehldatenStepline, "C");
        assertThat(ladeZaehldatenStepline.getLegend(), is(Arrays.asList("B", "C", "A")));

        ProcessZaehldatenSteplineService.setLegendInZaehldatenStepline(ladeZaehldatenStepline, null);
        assertThat(ladeZaehldatenStepline.getLegend(), is(Arrays.asList("B", "C", "A", null)));

    }

    @Test
    public void splitSeriesEntriesIntoFirstChartAndSecondChartIfNecessaryInLadeZaehldatenSteplineNoSplitting() {
        final OptionsDTO options = new OptionsDTO();
        options.setZeitblock(Zeitblock.ZB_00_24);
        options.setZaehldauer(Zaehldauer.DAUER_24_STUNDEN);

        final List<StepLineSeriesEntryBaseDTO> seriesEntriesFirstChart = new ArrayList<>();

        StepLineSeriesEntryIntegerDTO stepLineSeriesEntryIntegerDTO = new StepLineSeriesEntryIntegerDTO();
        stepLineSeriesEntryIntegerDTO.setName("1");
        stepLineSeriesEntryIntegerDTO.setXAxisIndex(0);
        stepLineSeriesEntryIntegerDTO.setYAxisIndex(0);
        stepLineSeriesEntryIntegerDTO.setYAxisData(Arrays.asList(1, 1, 1, 1));
        seriesEntriesFirstChart.add(stepLineSeriesEntryIntegerDTO);

        stepLineSeriesEntryIntegerDTO = new StepLineSeriesEntryIntegerDTO();
        stepLineSeriesEntryIntegerDTO.setName("2");
        stepLineSeriesEntryIntegerDTO.setXAxisIndex(0);
        stepLineSeriesEntryIntegerDTO.setYAxisIndex(0);
        stepLineSeriesEntryIntegerDTO.setYAxisData(Arrays.asList(2, 2, 2, 2));
        seriesEntriesFirstChart.add(stepLineSeriesEntryIntegerDTO);

        stepLineSeriesEntryIntegerDTO = new StepLineSeriesEntryIntegerDTO();
        stepLineSeriesEntryIntegerDTO.setName("3");
        stepLineSeriesEntryIntegerDTO.setXAxisIndex(0);
        stepLineSeriesEntryIntegerDTO.setYAxisIndex(0);
        stepLineSeriesEntryIntegerDTO.setYAxisData(Arrays.asList(3, 3, 3, 3));
        seriesEntriesFirstChart.add(stepLineSeriesEntryIntegerDTO);

        stepLineSeriesEntryIntegerDTO = new StepLineSeriesEntryIntegerDTO();
        stepLineSeriesEntryIntegerDTO.setName("4");
        stepLineSeriesEntryIntegerDTO.setXAxisIndex(0);
        stepLineSeriesEntryIntegerDTO.setYAxisIndex(0);
        stepLineSeriesEntryIntegerDTO.setYAxisData(Arrays.asList(4, 4, 4, 4));
        seriesEntriesFirstChart.add(stepLineSeriesEntryIntegerDTO);

        LadeZaehldatenSteplineDTO ladeZaehldatenStepline = new LadeZaehldatenSteplineDTO();
        ladeZaehldatenStepline.setXAxisDataFirstChart(Arrays.asList("1", "2", "3", "4"));
        ladeZaehldatenStepline.setSeriesEntriesFirstChart(seriesEntriesFirstChart);

        ProcessZaehldatenSteplineService
                .splitSeriesEntriesIntoFirstChartAndSecondChartIfNecessaryInLadeZaehldatenStepline(ladeZaehldatenStepline, options);

        assertThat(ladeZaehldatenStepline.getXAxisDataFirstChart(), is(Arrays.asList("1", "2", "3", "4")));
        assertThat(ladeZaehldatenStepline.getXAxisDataSecondChart(), is(IsNull.nullValue()));
        assertThat(ladeZaehldatenStepline.getSeriesEntriesFirstChart(), is(seriesEntriesFirstChart));
        assertThat(ladeZaehldatenStepline.getSeriesEntriesSecondChart(), is(IsNull.nullValue()));
    }

    @Test
    public void splitSeriesEntriesIntoFirstChartAndSecondChartIfNecessaryInLadeZaehldatenSteplineWithSplitting() {
        final OptionsDTO options = new OptionsDTO();
        options.setZeitblock(Zeitblock.ZB_00_24);
        options.setZaehldauer(Zaehldauer.DAUER_2_X_4_STUNDEN);

        final List<StepLineSeriesEntryBaseDTO> seriesEntriesFirstChart = new ArrayList<>();

        StepLineSeriesEntryIntegerDTO stepLineSeriesEntryIntegerDTO = new StepLineSeriesEntryIntegerDTO();
        stepLineSeriesEntryIntegerDTO.setName("1");
        stepLineSeriesEntryIntegerDTO.setXAxisIndex(0);
        stepLineSeriesEntryIntegerDTO.setYAxisIndex(0);
        stepLineSeriesEntryIntegerDTO.setYAxisData(Arrays.asList(11, 12, 13, 14));
        seriesEntriesFirstChart.add(stepLineSeriesEntryIntegerDTO);

        stepLineSeriesEntryIntegerDTO = new StepLineSeriesEntryIntegerDTO();
        stepLineSeriesEntryIntegerDTO.setName("2");
        stepLineSeriesEntryIntegerDTO.setXAxisIndex(0);
        stepLineSeriesEntryIntegerDTO.setYAxisIndex(0);
        stepLineSeriesEntryIntegerDTO.setYAxisData(Arrays.asList(21, 22, 23, 24));
        seriesEntriesFirstChart.add(stepLineSeriesEntryIntegerDTO);

        stepLineSeriesEntryIntegerDTO = new StepLineSeriesEntryIntegerDTO();
        stepLineSeriesEntryIntegerDTO.setName("3");
        stepLineSeriesEntryIntegerDTO.setXAxisIndex(0);
        stepLineSeriesEntryIntegerDTO.setYAxisIndex(0);
        stepLineSeriesEntryIntegerDTO.setYAxisData(Arrays.asList(31, 32, 33, 34));
        seriesEntriesFirstChart.add(stepLineSeriesEntryIntegerDTO);

        stepLineSeriesEntryIntegerDTO = new StepLineSeriesEntryIntegerDTO();
        stepLineSeriesEntryIntegerDTO.setName("4");
        stepLineSeriesEntryIntegerDTO.setXAxisIndex(0);
        stepLineSeriesEntryIntegerDTO.setYAxisIndex(0);
        stepLineSeriesEntryIntegerDTO.setYAxisData(Arrays.asList(41, 42, 43, 44));
        seriesEntriesFirstChart.add(stepLineSeriesEntryIntegerDTO);

        LadeZaehldatenSteplineDTO ladeZaehldatenStepline = new LadeZaehldatenSteplineDTO();
        ladeZaehldatenStepline.setXAxisDataFirstChart(Arrays.asList("1", "2", "3", "4"));
        ladeZaehldatenStepline.setSeriesEntriesFirstChart(seriesEntriesFirstChart);

        ProcessZaehldatenSteplineService
                .splitSeriesEntriesIntoFirstChartAndSecondChartIfNecessaryInLadeZaehldatenStepline(ladeZaehldatenStepline, options);

        assertThat(ladeZaehldatenStepline.getXAxisDataFirstChart(), is(Arrays.asList("1", "2")));
        assertThat(ladeZaehldatenStepline.getXAxisDataSecondChart(), is(Arrays.asList("3", "4")));

        // First chart
        assertThat(((StepLineSeriesEntryIntegerDTO) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(0)).getYAxisData(), is(Arrays.asList(11, 12)));
        assertThat(((StepLineSeriesEntryIntegerDTO) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(1)).getYAxisData(), is(Arrays.asList(21, 22)));
        assertThat(((StepLineSeriesEntryIntegerDTO) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(2)).getYAxisData(), is(Arrays.asList(31, 32)));
        assertThat(((StepLineSeriesEntryIntegerDTO) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(3)).getYAxisData(), is(Arrays.asList(41, 42)));

        assertThat(ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(0).getXAxisIndex(), is(0));
        assertThat(ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(0).getYAxisIndex(), is(0));
        assertThat(ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(1).getXAxisIndex(), is(0));
        assertThat(ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(1).getYAxisIndex(), is(0));
        assertThat(ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(2).getXAxisIndex(), is(0));
        assertThat(ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(2).getYAxisIndex(), is(0));
        assertThat(ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(3).getXAxisIndex(), is(0));
        assertThat(ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(3).getYAxisIndex(), is(0));

        // Second chart
        assertThat(((StepLineSeriesEntryIntegerDTO) ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(0)).getYAxisData(), is(Arrays.asList(13, 14)));
        assertThat(((StepLineSeriesEntryIntegerDTO) ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(1)).getYAxisData(), is(Arrays.asList(23, 24)));
        assertThat(((StepLineSeriesEntryIntegerDTO) ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(2)).getYAxisData(), is(Arrays.asList(33, 34)));
        assertThat(((StepLineSeriesEntryIntegerDTO) ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(3)).getYAxisData(), is(Arrays.asList(43, 44)));

        assertThat(ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(0).getXAxisIndex(), is(1));
        assertThat(ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(0).getYAxisIndex(), is(2));
        assertThat(ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(1).getXAxisIndex(), is(1));
        assertThat(ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(1).getYAxisIndex(), is(2));
        assertThat(ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(2).getXAxisIndex(), is(1));
        assertThat(ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(2).getYAxisIndex(), is(2));
        assertThat(ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(3).getXAxisIndex(), is(1));
        assertThat(ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(3).getYAxisIndex(), is(2));

    }

    @Test
    public void setSeriesIndexForChart() {
        final StepLineSeriesEntryBaseDTO seriesEntryFirstChart = new StepLineSeriesEntryBaseDTO();
        final StepLineSeriesEntryBaseDTO seriesEntrySecondChart = new StepLineSeriesEntryBaseDTO();

        ProcessZaehldatenSteplineService
                .setSeriesIndexForChart(ChartLegendUtil.SCHWERVERKEHR_ANTEIL_PROZENT, seriesEntryFirstChart, seriesEntrySecondChart);

        assertThat(seriesEntryFirstChart.getXAxisIndex(), is(0));
        assertThat(seriesEntryFirstChart.getYAxisIndex(), is(1));
        assertThat(seriesEntrySecondChart.getXAxisIndex(), is(1));
        assertThat(seriesEntrySecondChart.getYAxisIndex(), is(3));

        ProcessZaehldatenSteplineService
                .setSeriesIndexForChart("A", seriesEntryFirstChart, seriesEntrySecondChart);

        assertThat(seriesEntryFirstChart.getXAxisIndex(), is(0));
        assertThat(seriesEntryFirstChart.getYAxisIndex(), is(0));
        assertThat(seriesEntrySecondChart.getXAxisIndex(), is(1));
        assertThat(seriesEntrySecondChart.getYAxisIndex(), is(2));

        ProcessZaehldatenSteplineService
                .setSeriesIndexForChart(ChartLegendUtil.GUETERVERKEHR_ANTEIL_PROZENT, seriesEntryFirstChart, seriesEntrySecondChart);

        assertThat(seriesEntryFirstChart.getXAxisIndex(), is(0));
        assertThat(seriesEntryFirstChart.getYAxisIndex(), is(1));
        assertThat(seriesEntrySecondChart.getXAxisIndex(), is(1));
        assertThat(seriesEntrySecondChart.getYAxisIndex(), is(3));
    }

    @Test
    public void setSeriesIndexForFirstChartValue() {
        final StepLineSeriesEntryBaseDTO seriesEntryFirstChart = new StepLineSeriesEntryBaseDTO();
        seriesEntryFirstChart.setXAxisIndex(-1);
        seriesEntryFirstChart.setYAxisIndex(-1);

        ProcessZaehldatenSteplineService.setSeriesIndexForFirstChartValue(seriesEntryFirstChart);

        assertThat(seriesEntryFirstChart.getXAxisIndex(), is(0));
        assertThat(seriesEntryFirstChart.getYAxisIndex(), is(0));
    }

    @Test
    public void setSeriesIndexForFirstChartPercent() {
        final StepLineSeriesEntryBaseDTO seriesEntryFirstChart = new StepLineSeriesEntryBaseDTO();
        seriesEntryFirstChart.setXAxisIndex(-1);
        seriesEntryFirstChart.setYAxisIndex(-1);

        ProcessZaehldatenSteplineService.setSeriesIndexForFirstChartPercent(seriesEntryFirstChart);

        assertThat(seriesEntryFirstChart.getXAxisIndex(), is(0));
        assertThat(seriesEntryFirstChart.getYAxisIndex(), is(1));
    }

    @Test
    public void setSeriesIndexForSecondChartValue() {
        final StepLineSeriesEntryBaseDTO seriesEntryFirstChart = new StepLineSeriesEntryBaseDTO();
        seriesEntryFirstChart.setXAxisIndex(-1);
        seriesEntryFirstChart.setYAxisIndex(-1);

        ProcessZaehldatenSteplineService.setSeriesIndexForSecondChartValue(seriesEntryFirstChart);

        assertThat(seriesEntryFirstChart.getXAxisIndex(), is(1));
        assertThat(seriesEntryFirstChart.getYAxisIndex(), is(2));
    }

    @Test
    public void setSeriesIndexForSecondChartPercent() {
        final StepLineSeriesEntryBaseDTO seriesEntryFirstChart = new StepLineSeriesEntryBaseDTO();
        seriesEntryFirstChart.setXAxisIndex(-1);
        seriesEntryFirstChart.setYAxisIndex(-1);

        ProcessZaehldatenSteplineService.setSeriesIndexForSecondChartPercent(seriesEntryFirstChart);

        assertThat(seriesEntryFirstChart.getXAxisIndex(), is(1));
        assertThat(seriesEntryFirstChart.getYAxisIndex(), is(3));
    }

}
