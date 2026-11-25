package de.muenchen.dave.services.processzaehldaten;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import de.muenchen.dave.domain.dtos.OptionsDto;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatenStepLineDto;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatenTableDto;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatumDto;
import de.muenchen.dave.domain.dtos.laden.StepLineSeriesEntryBaseDto;
import de.muenchen.dave.domain.dtos.laden.StepLineSeriesEntryBigDecimalDto;
import de.muenchen.dave.domain.dtos.laden.StepLineSeriesEntryIntegerDto;
import de.muenchen.dave.domain.enums.Zaehldauer;
import de.muenchen.dave.domain.enums.Zeitblock;
import de.muenchen.dave.services.ladezaehldaten.LadeZaehldatenService;
import de.muenchen.dave.util.ChartLegendUtil;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProcessZaehldatenSteplineServiceTest {

    private final ProcessZaehldatenSteplineService processZaehldatenSteplineService = new ProcessZaehldatenSteplineService();

    private LadeZaehldatenTableDto ladeZaehldatenTable;

    private static void setDataInZaehldatum(final LadeZaehldatumDto ladeZaehldatum, final Integer value) {
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
        ladeZaehldatenTable = new LadeZaehldatenTableDto();

        final LadeZaehldatumDto ladeZaehldatum1 = new LadeZaehldatumDto();
        ladeZaehldatum1.setType("");
        ladeZaehldatum1.setStartUhrzeit(LocalTime.of(7, 0));
        ladeZaehldatum1.setEndeUhrzeit(LocalTime.of(8, 0));
        setDataInZaehldatum(ladeZaehldatum1, 1);

        final LadeZaehldatumDto ladeZaehldatum2 = new LadeZaehldatumDto();
        ladeZaehldatum2.setType("");
        ladeZaehldatum2.setStartUhrzeit(LocalTime.of(8, 0));
        ladeZaehldatum2.setEndeUhrzeit(LocalTime.of(9, 0));
        setDataInZaehldatum(ladeZaehldatum2, 1);

        final LadeZaehldatumDto ladeZaehldatum3 = new LadeZaehldatumDto();
        ladeZaehldatum3.setType("");
        ladeZaehldatum3.setStartUhrzeit(LocalTime.of(9, 0));
        ladeZaehldatum3.setEndeUhrzeit(LocalTime.of(10, 0));
        setDataInZaehldatum(ladeZaehldatum3, 1);

        final LadeZaehldatumDto ladeZaehldatum4 = new LadeZaehldatumDto();
        ladeZaehldatum4.setType(LadeZaehldatenService.BLOCK);

        final LadeZaehldatumDto ladeZaehldatum5 = new LadeZaehldatumDto();
        ladeZaehldatum5.setType("");
        ladeZaehldatum5.setStartUhrzeit(LocalTime.of(15, 0));
        ladeZaehldatum5.setEndeUhrzeit(LocalTime.of(16, 0));
        setDataInZaehldatum(ladeZaehldatum5, 2);

        final LadeZaehldatumDto ladeZaehldatum6 = new LadeZaehldatumDto();
        ladeZaehldatum6.setType("");
        ladeZaehldatum6.setStartUhrzeit(LocalTime.of(16, 0));
        ladeZaehldatum6.setEndeUhrzeit(LocalTime.of(17, 0));
        setDataInZaehldatum(ladeZaehldatum6, 2);

        final LadeZaehldatumDto ladeZaehldatum7 = new LadeZaehldatumDto();
        ladeZaehldatum7.setType("");
        ladeZaehldatum7.setStartUhrzeit(LocalTime.of(17, 0));
        ladeZaehldatum7.setEndeUhrzeit(LocalTime.of(18, 0));
        setDataInZaehldatum(ladeZaehldatum7, 2);

        final LadeZaehldatumDto ladeZaehldatum8 = new LadeZaehldatumDto();
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
        final OptionsDto options = new OptionsDto();
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

        final LadeZaehldatenStepLineDto ladeZaehldatenStepline = processZaehldatenSteplineService.ladeProcessedZaehldatenStepline(
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
        assertThat(((StepLineSeriesEntryIntegerDto) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(0)).getYAxisData().size(), is(6));
        assertThat(((StepLineSeriesEntryIntegerDto) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(0)).getYAxisData().get(0), is(1));
        assertThat(((StepLineSeriesEntryIntegerDto) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(0)).getYAxisData().get(3), is(2));
        assertThat(ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(0).getXAxisIndex(), is(0));
        assertThat(ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(0).getYAxisIndex(), is(0));

        assertThat(ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(9).getName(), is(ChartLegendUtil.SCHWERVERKEHR_ANTEIL_PROZENT));
        assertThat(((StepLineSeriesEntryBigDecimalDto) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(9)).getYAxisData().size(), is(6));
        assertThat(((StepLineSeriesEntryBigDecimalDto) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(9)).getYAxisData().get(0),
                is(BigDecimal.valueOf(60.0)));
        assertThat(((StepLineSeriesEntryBigDecimalDto) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(9)).getYAxisData().get(3),
                is(BigDecimal.valueOf(60.0)));
        assertThat(ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(9).getXAxisIndex(), is(0));
        assertThat(ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(9).getYAxisIndex(), is(1));

        assertThat(ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(12).getName(), is(ChartLegendUtil.PKW_EINHEITEN));
        assertThat(((StepLineSeriesEntryIntegerDto) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(12)).getYAxisData().size(), is(6));
        assertThat(((StepLineSeriesEntryIntegerDto) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(12)).getYAxisData().get(0), is(1));
        assertThat(((StepLineSeriesEntryIntegerDto) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(12)).getYAxisData().get(3), is(2));
        assertThat(ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(12).getXAxisIndex(), is(0));
        assertThat(ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(12).getYAxisIndex(), is(0));
    }

    @Test
    public void ladeProcessedZaehldatenSteplineAllDataSplittetTable() {
        final OptionsDto options = new OptionsDto();
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

        final LadeZaehldatenStepLineDto ladeZaehldatenStepline = processZaehldatenSteplineService.ladeProcessedZaehldatenStepline(
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
        assertThat(((StepLineSeriesEntryIntegerDto) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(0)).getYAxisData().size(), is(3));
        assertThat(((StepLineSeriesEntryIntegerDto) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(0)).getYAxisData().get(0), is(1));
        assertThat(((StepLineSeriesEntryIntegerDto) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(0)).getYAxisData().get(2), is(1));
        assertThat(ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(0).getXAxisIndex(), is(0));
        assertThat(ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(0).getYAxisIndex(), is(0));

        assertThat(ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(9).getName(), is(ChartLegendUtil.SCHWERVERKEHR_ANTEIL_PROZENT));
        assertThat(((StepLineSeriesEntryBigDecimalDto) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(9)).getYAxisData().size(), is(3));
        assertThat(((StepLineSeriesEntryBigDecimalDto) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(9)).getYAxisData().get(0),
                is(BigDecimal.valueOf(60.0)));
        assertThat(((StepLineSeriesEntryBigDecimalDto) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(9)).getYAxisData().get(2),
                is(BigDecimal.valueOf(60.0)));
        assertThat(ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(9).getXAxisIndex(), is(0));
        assertThat(ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(9).getYAxisIndex(), is(1));

        assertThat(ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(12).getName(), is(ChartLegendUtil.PKW_EINHEITEN));
        assertThat(((StepLineSeriesEntryIntegerDto) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(12)).getYAxisData().size(), is(3));
        assertThat(((StepLineSeriesEntryIntegerDto) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(12)).getYAxisData().get(0), is(1));
        assertThat(((StepLineSeriesEntryIntegerDto) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(12)).getYAxisData().get(2), is(1));
        assertThat(ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(12).getXAxisIndex(), is(0));
        assertThat(ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(12).getYAxisIndex(), is(0));

        assertThat(ladeZaehldatenStepline.getSeriesEntriesSecondChart().size(), is(13));
        assertThat(ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(0).getName(), is(ChartLegendUtil.PKW));
        assertThat(((StepLineSeriesEntryIntegerDto) ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(0)).getYAxisData().size(), is(3));
        assertThat(((StepLineSeriesEntryIntegerDto) ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(0)).getYAxisData().get(0), is(2));
        assertThat(((StepLineSeriesEntryIntegerDto) ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(0)).getYAxisData().get(2), is(2));
        assertThat(ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(0).getXAxisIndex(), is(1));
        assertThat(ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(0).getYAxisIndex(), is(2));

        assertThat(ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(9).getName(), is(ChartLegendUtil.SCHWERVERKEHR_ANTEIL_PROZENT));
        assertThat(((StepLineSeriesEntryBigDecimalDto) ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(9)).getYAxisData().size(), is(3));
        assertThat(((StepLineSeriesEntryBigDecimalDto) ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(9)).getYAxisData().get(0),
                is(BigDecimal.valueOf(60.0)));
        assertThat(((StepLineSeriesEntryBigDecimalDto) ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(9)).getYAxisData().get(2),
                is(BigDecimal.valueOf(60.0)));
        assertThat(ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(9).getXAxisIndex(), is(1));
        assertThat(ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(9).getYAxisIndex(), is(3));

        assertThat(ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(12).getName(), is(ChartLegendUtil.PKW_EINHEITEN));
        assertThat(((StepLineSeriesEntryIntegerDto) ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(12)).getYAxisData().size(), is(3));
        assertThat(((StepLineSeriesEntryIntegerDto) ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(12)).getYAxisData().get(0), is(2));
        assertThat(((StepLineSeriesEntryIntegerDto) ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(12)).getYAxisData().get(2), is(2));
        assertThat(ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(12).getXAxisIndex(), is(1));
        assertThat(ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(12).getYAxisIndex(), is(2));
    }

    @Test
    public void ladeProcessedZaehldatenSteplineWithoutPkwEinheitenSplittetTable() {
        final OptionsDto options = new OptionsDto();
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

        final LadeZaehldatenStepLineDto ladeZaehldatenStepline = processZaehldatenSteplineService.ladeProcessedZaehldatenStepline(
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
        assertThat(((StepLineSeriesEntryIntegerDto) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(0)).getYAxisData().size(), is(3));
        assertThat(((StepLineSeriesEntryIntegerDto) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(0)).getYAxisData().get(0), is(1));
        assertThat(((StepLineSeriesEntryIntegerDto) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(0)).getYAxisData().get(2), is(1));
        assertThat(ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(0).getXAxisIndex(), is(0));
        assertThat(ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(0).getYAxisIndex(), is(0));

        assertThat(ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(9).getName(), is(ChartLegendUtil.SCHWERVERKEHR_ANTEIL_PROZENT));
        assertThat(((StepLineSeriesEntryBigDecimalDto) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(9)).getYAxisData().size(), is(3));
        assertThat(((StepLineSeriesEntryBigDecimalDto) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(9)).getYAxisData().get(0),
                is(BigDecimal.valueOf(60.0)));
        assertThat(((StepLineSeriesEntryBigDecimalDto) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(9)).getYAxisData().get(2),
                is(BigDecimal.valueOf(60.0)));
        assertThat(ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(9).getXAxisIndex(), is(0));
        assertThat(ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(9).getYAxisIndex(), is(1));

        assertThat(ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(11).getName(), is(ChartLegendUtil.GUETERVERKEHR_ANTEIL_PROZENT));
        assertThat(((StepLineSeriesEntryBigDecimalDto) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(11)).getYAxisData().size(), is(3));
        assertThat(((StepLineSeriesEntryBigDecimalDto) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(11)).getYAxisData().get(0),
                is(BigDecimal.valueOf(40.0)));
        assertThat(((StepLineSeriesEntryBigDecimalDto) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(11)).getYAxisData().get(2),
                is(BigDecimal.valueOf(40.0)));
        assertThat(ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(11).getXAxisIndex(), is(0));
        assertThat(ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(11).getYAxisIndex(), is(1));

        assertThat(ladeZaehldatenStepline.getSeriesEntriesSecondChart().size(), is(12));
        assertThat(ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(0).getName(), is(ChartLegendUtil.PKW));
        assertThat(((StepLineSeriesEntryIntegerDto) ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(0)).getYAxisData().size(), is(3));
        assertThat(((StepLineSeriesEntryIntegerDto) ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(0)).getYAxisData().get(0), is(2));
        assertThat(((StepLineSeriesEntryIntegerDto) ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(0)).getYAxisData().get(2), is(2));
        assertThat(ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(0).getXAxisIndex(), is(1));
        assertThat(ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(0).getYAxisIndex(), is(2));

        assertThat(ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(9).getName(), is(ChartLegendUtil.SCHWERVERKEHR_ANTEIL_PROZENT));
        assertThat(((StepLineSeriesEntryBigDecimalDto) ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(9)).getYAxisData().size(), is(3));
        assertThat(((StepLineSeriesEntryBigDecimalDto) ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(9)).getYAxisData().get(0),
                is(BigDecimal.valueOf(60.0)));
        assertThat(((StepLineSeriesEntryBigDecimalDto) ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(9)).getYAxisData().get(2),
                is(BigDecimal.valueOf(60.0)));
        assertThat(ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(9).getXAxisIndex(), is(1));
        assertThat(ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(9).getYAxisIndex(), is(3));

        assertThat(ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(11).getName(), is(ChartLegendUtil.GUETERVERKEHR_ANTEIL_PROZENT));
        assertThat(((StepLineSeriesEntryBigDecimalDto) ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(11)).getYAxisData().size(), is(3));
        assertThat(((StepLineSeriesEntryBigDecimalDto) ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(11)).getYAxisData().get(0),
                is(BigDecimal.valueOf(40.0)));
        assertThat(((StepLineSeriesEntryBigDecimalDto) ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(11)).getYAxisData().get(2),
                is(BigDecimal.valueOf(40.0)));
        assertThat(ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(11).getXAxisIndex(), is(1));
        assertThat(ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(11).getYAxisIndex(), is(3));
    }

    @Test
    public void setRangeMaxRoundedToTenInZaehldatenSteplineInteger() {
        final LadeZaehldatenStepLineDto ladeZaehldatenStepline = new LadeZaehldatenStepLineDto();
        ladeZaehldatenStepline.setRangeMax(0);

        ProcessZaehldatenSteplineService.setRangeMaxRoundedInZaehldatenStepline(ladeZaehldatenStepline, 42);
        assertThat(ladeZaehldatenStepline.getRangeMax(), is(60));

        ladeZaehldatenStepline.setRangeMax(300);
        ProcessZaehldatenSteplineService.setRangeMaxRoundedInZaehldatenStepline(ladeZaehldatenStepline, 100);
        assertThat(ladeZaehldatenStepline.getRangeMax(), is(300));

        ladeZaehldatenStepline.setRangeMax(158);
        ProcessZaehldatenSteplineService.setRangeMaxRoundedInZaehldatenStepline(ladeZaehldatenStepline, 120);
        assertThat(ladeZaehldatenStepline.getRangeMax(), is(160));

        final Integer value = null;
        ladeZaehldatenStepline.setRangeMax(300);
        ProcessZaehldatenSteplineService.setRangeMaxRoundedInZaehldatenStepline(ladeZaehldatenStepline, value);
        assertThat(ladeZaehldatenStepline.getRangeMax(), is(300));

        ladeZaehldatenStepline.setRangeMax(158);
        ProcessZaehldatenSteplineService.setRangeMaxRoundedInZaehldatenStepline(ladeZaehldatenStepline, value);
        assertThat(ladeZaehldatenStepline.getRangeMax(), is(160));
    }

    @Test
    public void setRangeMaxRoundedToTenInZaehldatenSteplineBigDecimal() {
        final LadeZaehldatenStepLineDto ladeZaehldatenStepline = new LadeZaehldatenStepLineDto();
        ladeZaehldatenStepline.setRangeMax(0);

        ProcessZaehldatenSteplineService.setRangeMaxRoundedInZaehldatenStepline(ladeZaehldatenStepline, BigDecimal.valueOf(42));
        assertThat(ladeZaehldatenStepline.getRangeMax(), is(60));

        ladeZaehldatenStepline.setRangeMax(300);
        ProcessZaehldatenSteplineService.setRangeMaxRoundedInZaehldatenStepline(ladeZaehldatenStepline, BigDecimal.valueOf(100));
        assertThat(ladeZaehldatenStepline.getRangeMax(), is(300));

        ladeZaehldatenStepline.setRangeMax(158);
        ProcessZaehldatenSteplineService.setRangeMaxRoundedInZaehldatenStepline(ladeZaehldatenStepline, BigDecimal.valueOf(120));
        assertThat(ladeZaehldatenStepline.getRangeMax(), is(160));

        final BigDecimal value = null;
        ladeZaehldatenStepline.setRangeMax(300);
        ProcessZaehldatenSteplineService.setRangeMaxRoundedInZaehldatenStepline(ladeZaehldatenStepline, value);
        assertThat(ladeZaehldatenStepline.getRangeMax(), is(300));

        ladeZaehldatenStepline.setRangeMax(158);
        ProcessZaehldatenSteplineService.setRangeMaxRoundedInZaehldatenStepline(ladeZaehldatenStepline, value);
        assertThat(ladeZaehldatenStepline.getRangeMax(), is(160));
    }

    @Test
    public void setRangeMaxPercentInZaehldatenStepline() {
        final LadeZaehldatenStepLineDto ladeZaehldatenStepline = new LadeZaehldatenStepLineDto();
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
    public void splitSeriesEntriesIntoFirstChartAndSecondChartIfNecessaryInLadeZaehldatenSteplineNoSplitting() {
        final OptionsDto options = new OptionsDto();
        options.setZeitblock(Zeitblock.ZB_00_24);
        options.setZaehldauer(Zaehldauer.DAUER_24_STUNDEN);

        final List<StepLineSeriesEntryBaseDto> seriesEntriesFirstChart = new ArrayList<>();

        StepLineSeriesEntryIntegerDto stepLineSeriesEntryIntegerDTO = new StepLineSeriesEntryIntegerDto();
        stepLineSeriesEntryIntegerDTO.setName("1");
        stepLineSeriesEntryIntegerDTO.setXAxisIndex(0);
        stepLineSeriesEntryIntegerDTO.setYAxisIndex(0);
        stepLineSeriesEntryIntegerDTO.setYAxisData(Arrays.asList(1, 1, 1, 1));
        seriesEntriesFirstChart.add(stepLineSeriesEntryIntegerDTO);

        stepLineSeriesEntryIntegerDTO = new StepLineSeriesEntryIntegerDto();
        stepLineSeriesEntryIntegerDTO.setName("2");
        stepLineSeriesEntryIntegerDTO.setXAxisIndex(0);
        stepLineSeriesEntryIntegerDTO.setYAxisIndex(0);
        stepLineSeriesEntryIntegerDTO.setYAxisData(Arrays.asList(2, 2, 2, 2));
        seriesEntriesFirstChart.add(stepLineSeriesEntryIntegerDTO);

        stepLineSeriesEntryIntegerDTO = new StepLineSeriesEntryIntegerDto();
        stepLineSeriesEntryIntegerDTO.setName("3");
        stepLineSeriesEntryIntegerDTO.setXAxisIndex(0);
        stepLineSeriesEntryIntegerDTO.setYAxisIndex(0);
        stepLineSeriesEntryIntegerDTO.setYAxisData(Arrays.asList(3, 3, 3, 3));
        seriesEntriesFirstChart.add(stepLineSeriesEntryIntegerDTO);

        stepLineSeriesEntryIntegerDTO = new StepLineSeriesEntryIntegerDto();
        stepLineSeriesEntryIntegerDTO.setName("4");
        stepLineSeriesEntryIntegerDTO.setXAxisIndex(0);
        stepLineSeriesEntryIntegerDTO.setYAxisIndex(0);
        stepLineSeriesEntryIntegerDTO.setYAxisData(Arrays.asList(4, 4, 4, 4));
        seriesEntriesFirstChart.add(stepLineSeriesEntryIntegerDTO);

        LadeZaehldatenStepLineDto ladeZaehldatenStepline = new LadeZaehldatenStepLineDto();
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
        final OptionsDto options = new OptionsDto();
        options.setZeitblock(Zeitblock.ZB_00_24);
        options.setZaehldauer(Zaehldauer.DAUER_2_X_4_STUNDEN);

        final List<StepLineSeriesEntryBaseDto> seriesEntriesFirstChart = new ArrayList<>();

        StepLineSeriesEntryIntegerDto stepLineSeriesEntryIntegerDTO = new StepLineSeriesEntryIntegerDto();
        stepLineSeriesEntryIntegerDTO.setName("1");
        stepLineSeriesEntryIntegerDTO.setXAxisIndex(0);
        stepLineSeriesEntryIntegerDTO.setYAxisIndex(0);
        stepLineSeriesEntryIntegerDTO.setYAxisData(Arrays.asList(11, 12, 13, 14));
        seriesEntriesFirstChart.add(stepLineSeriesEntryIntegerDTO);

        stepLineSeriesEntryIntegerDTO = new StepLineSeriesEntryIntegerDto();
        stepLineSeriesEntryIntegerDTO.setName("2");
        stepLineSeriesEntryIntegerDTO.setXAxisIndex(0);
        stepLineSeriesEntryIntegerDTO.setYAxisIndex(0);
        stepLineSeriesEntryIntegerDTO.setYAxisData(Arrays.asList(21, 22, 23, 24));
        seriesEntriesFirstChart.add(stepLineSeriesEntryIntegerDTO);

        stepLineSeriesEntryIntegerDTO = new StepLineSeriesEntryIntegerDto();
        stepLineSeriesEntryIntegerDTO.setName("3");
        stepLineSeriesEntryIntegerDTO.setXAxisIndex(0);
        stepLineSeriesEntryIntegerDTO.setYAxisIndex(0);
        stepLineSeriesEntryIntegerDTO.setYAxisData(Arrays.asList(31, 32, 33, 34));
        seriesEntriesFirstChart.add(stepLineSeriesEntryIntegerDTO);

        stepLineSeriesEntryIntegerDTO = new StepLineSeriesEntryIntegerDto();
        stepLineSeriesEntryIntegerDTO.setName("4");
        stepLineSeriesEntryIntegerDTO.setXAxisIndex(0);
        stepLineSeriesEntryIntegerDTO.setYAxisIndex(0);
        stepLineSeriesEntryIntegerDTO.setYAxisData(Arrays.asList(41, 42, 43, 44));
        seriesEntriesFirstChart.add(stepLineSeriesEntryIntegerDTO);

        LadeZaehldatenStepLineDto ladeZaehldatenStepline = new LadeZaehldatenStepLineDto();
        ladeZaehldatenStepline.setXAxisDataFirstChart(Arrays.asList("1", "2", "3", "4"));
        ladeZaehldatenStepline.setSeriesEntriesFirstChart(seriesEntriesFirstChart);

        ProcessZaehldatenSteplineService
                .splitSeriesEntriesIntoFirstChartAndSecondChartIfNecessaryInLadeZaehldatenStepline(ladeZaehldatenStepline, options);

        assertThat(ladeZaehldatenStepline.getXAxisDataFirstChart(), is(Arrays.asList("1", "2")));
        assertThat(ladeZaehldatenStepline.getXAxisDataSecondChart(), is(Arrays.asList("3", "4")));

        // First chart
        assertThat(((StepLineSeriesEntryIntegerDto) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(0)).getYAxisData(), is(Arrays.asList(11, 12)));
        assertThat(((StepLineSeriesEntryIntegerDto) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(1)).getYAxisData(), is(Arrays.asList(21, 22)));
        assertThat(((StepLineSeriesEntryIntegerDto) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(2)).getYAxisData(), is(Arrays.asList(31, 32)));
        assertThat(((StepLineSeriesEntryIntegerDto) ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(3)).getYAxisData(), is(Arrays.asList(41, 42)));

        assertThat(ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(0).getXAxisIndex(), is(0));
        assertThat(ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(0).getYAxisIndex(), is(0));
        assertThat(ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(1).getXAxisIndex(), is(0));
        assertThat(ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(1).getYAxisIndex(), is(0));
        assertThat(ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(2).getXAxisIndex(), is(0));
        assertThat(ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(2).getYAxisIndex(), is(0));
        assertThat(ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(3).getXAxisIndex(), is(0));
        assertThat(ladeZaehldatenStepline.getSeriesEntriesFirstChart().get(3).getYAxisIndex(), is(0));

        // Second chart
        assertThat(((StepLineSeriesEntryIntegerDto) ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(0)).getYAxisData(), is(Arrays.asList(13, 14)));
        assertThat(((StepLineSeriesEntryIntegerDto) ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(1)).getYAxisData(), is(Arrays.asList(23, 24)));
        assertThat(((StepLineSeriesEntryIntegerDto) ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(2)).getYAxisData(), is(Arrays.asList(33, 34)));
        assertThat(((StepLineSeriesEntryIntegerDto) ladeZaehldatenStepline.getSeriesEntriesSecondChart().get(3)).getYAxisData(), is(Arrays.asList(43, 44)));

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
        final StepLineSeriesEntryBaseDto seriesEntryFirstChart = new StepLineSeriesEntryIntegerDto();
        final StepLineSeriesEntryBaseDto seriesEntrySecondChart = new StepLineSeriesEntryIntegerDto();

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
}
