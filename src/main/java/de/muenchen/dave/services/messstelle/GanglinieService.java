/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.services.messstelle;

import de.muenchen.dave.domain.dtos.laden.LadeZaehldatenSteplineDTO;
import de.muenchen.dave.domain.dtos.laden.StepLineSeriesEntryBaseDTO;
import de.muenchen.dave.domain.dtos.laden.StepLineSeriesEntryBigDecimalDTO;
import de.muenchen.dave.domain.dtos.laden.StepLineSeriesEntryIntegerDTO;
import de.muenchen.dave.geodateneai.gen.model.MeasurementValuesPerInterval;
import de.muenchen.dave.util.ChartLegendUtil;
import de.muenchen.dave.util.ZaehldatenProcessingUtil;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class GanglinieService {

    public LadeZaehldatenSteplineDTO ladeGanglinie(final List<MeasurementValuesPerInterval> intervalle) {
        log.debug("#ladeGanglinie");
        final LadeZaehldatenSteplineDTO ladeZaehldatenStepline = new LadeZaehldatenSteplineDTO();
        ladeZaehldatenStepline.setRangeMax(0);
        ladeZaehldatenStepline.setRangeMaxPercent(0);
        ladeZaehldatenStepline.setLegend(new ArrayList<>());
        ladeZaehldatenStepline.setXAxisDataFirstChart(new ArrayList<>());
        ladeZaehldatenStepline.setSeriesEntriesFirstChart(new ArrayList<>());

        final SeriesEntries seriesEntries = new SeriesEntries();

        intervalle
                .forEach(intervall -> {
                    setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryPkw());
                    seriesEntries.getSeriesEntryPkw().getYAxisData().add(intervall.getSummeAllePkw());
                    setLegendInZaehldatenStepline(ladeZaehldatenStepline, ChartLegendUtil.PKW);
                    setRangeMaxRoundedToHundredInZaehldatenStepline(ladeZaehldatenStepline, intervall.getSummeAllePkw());

                    setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryLkw());
                    seriesEntries.getSeriesEntryLkw().getYAxisData().add(intervall.getAnzahlLkw());
                    setLegendInZaehldatenStepline(ladeZaehldatenStepline, ChartLegendUtil.LKW);
                    setRangeMaxRoundedToHundredInZaehldatenStepline(ladeZaehldatenStepline, intervall.getAnzahlLkw());

                    setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryLfw());
                    seriesEntries.getSeriesEntryLfw().getYAxisData().add(intervall.getAnzahlLfw());
                    setLegendInZaehldatenStepline(ladeZaehldatenStepline, ChartLegendUtil.LFW);
                    setRangeMaxRoundedToHundredInZaehldatenStepline(ladeZaehldatenStepline, intervall.getAnzahlLfw());

                    setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryLz());
                    seriesEntries.getSeriesEntryLz().getYAxisData().add(intervall.getSummeLastzug());
                    setLegendInZaehldatenStepline(ladeZaehldatenStepline, ChartLegendUtil.LASTZUEGE);
                    setRangeMaxRoundedToHundredInZaehldatenStepline(ladeZaehldatenStepline, intervall.getSummeLastzug());

                    setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryBus());
                    seriesEntries.getSeriesEntryBus().getYAxisData().add(intervall.getAnzahlBus());
                    setLegendInZaehldatenStepline(ladeZaehldatenStepline, ChartLegendUtil.BUSSE);
                    setRangeMaxRoundedToHundredInZaehldatenStepline(ladeZaehldatenStepline, intervall.getAnzahlBus());

                    setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryKrad());
                    seriesEntries.getSeriesEntryKrad().getYAxisData().add(intervall.getAnzahlKrad());
                    setLegendInZaehldatenStepline(ladeZaehldatenStepline, ChartLegendUtil.KRAFTRAEDER);
                    setRangeMaxRoundedToHundredInZaehldatenStepline(ladeZaehldatenStepline, intervall.getAnzahlKrad());

                    setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryRad());
                    seriesEntries.getSeriesEntryRad().getYAxisData().add(intervall.getAnzahlRad());
                    setLegendInZaehldatenStepline(ladeZaehldatenStepline, ChartLegendUtil.RAD);
                    setRangeMaxRoundedToHundredInZaehldatenStepline(ladeZaehldatenStepline, intervall.getAnzahlRad());

                    setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryKfz());
                    seriesEntries.getSeriesEntryKfz().getYAxisData().add(intervall.getSummeKraftfahrzeugverkehr());
                    setLegendInZaehldatenStepline(ladeZaehldatenStepline, ChartLegendUtil.KFZ);
                    setRangeMaxRoundedToHundredInZaehldatenStepline(ladeZaehldatenStepline, intervall.getSummeKraftfahrzeugverkehr());

                    setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntrySv());
                    seriesEntries.getSeriesEntrySv().getYAxisData().add(intervall.getSummeSchwerverkehr());
                    setLegendInZaehldatenStepline(ladeZaehldatenStepline, ChartLegendUtil.SCHWERVERKEHR);
                    setRangeMaxRoundedToHundredInZaehldatenStepline(ladeZaehldatenStepline, intervall.getSummeSchwerverkehr());

                    setSeriesIndexForFirstChartPercent(seriesEntries.getSeriesEntrySvProzent());
                    seriesEntries.getSeriesEntrySvProzent().getYAxisData().add(intervall.getProzentSchwerverkehr());
                    setLegendInZaehldatenStepline(ladeZaehldatenStepline, ChartLegendUtil.SCHWERVERKEHR_ANTEIL_PROZENT);
                    setRangeMaxPercentInZaehldatenStepline(ladeZaehldatenStepline, intervall.getProzentSchwerverkehr());

                    setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryGv());
                    seriesEntries.getSeriesEntryGv().getYAxisData().add(intervall.getSummeGueterverkehr());
                    setLegendInZaehldatenStepline(ladeZaehldatenStepline, ChartLegendUtil.GUETERVERKEHR);
                    setRangeMaxRoundedToHundredInZaehldatenStepline(ladeZaehldatenStepline, intervall.getSummeGueterverkehr());

                    setSeriesIndexForFirstChartPercent(seriesEntries.getSeriesEntryGvProzent());
                    seriesEntries.getSeriesEntryGvProzent().getYAxisData().add(intervall.getProzentGueterverkehr());
                    setLegendInZaehldatenStepline(ladeZaehldatenStepline, ChartLegendUtil.GUETERVERKEHR_ANTEIL_PROZENT);
                    setRangeMaxPercentInZaehldatenStepline(ladeZaehldatenStepline, intervall.getProzentGueterverkehr());

                    ladeZaehldatenStepline.setXAxisDataFirstChart(
                            ZaehldatenProcessingUtil.checkAndAddToXAxisWhenNotAvailable(
                                    ladeZaehldatenStepline.getXAxisDataFirstChart(),
                                    intervall.getUhrzeitVon().toString()));
                });
        ladeZaehldatenStepline.setSeriesEntriesFirstChart(seriesEntries.getChosenStepLineSeriesEntries());
        return ladeZaehldatenStepline;
    }

    private static final Integer ROUNDING_VALUE = 20;

    private static final Integer ROUNDING_VALUE_PERCENT = 2;

    protected static void setRangeMaxRoundedToHundredInZaehldatenStepline(final LadeZaehldatenSteplineDTO ladeZaehldatenStepline,
            final Integer value) {
        ladeZaehldatenStepline.setRangeMax(
                ZaehldatenProcessingUtil.getValueRounded(
                        Math.max(
                                ZaehldatenProcessingUtil.getZeroIfNull(value),
                                ladeZaehldatenStepline.getRangeMax()),
                        ROUNDING_VALUE));
    }

    protected static void setRangeMaxRoundedToHundredInZaehldatenStepline(final LadeZaehldatenSteplineDTO ladeZaehldatenStepline,
            final BigDecimal value) {
        setRangeMaxRoundedToHundredInZaehldatenStepline(
                ladeZaehldatenStepline,
                ZaehldatenProcessingUtil.getZeroIfNull(value).intValue());
    }

    protected static void setRangeMaxPercentInZaehldatenStepline(final LadeZaehldatenSteplineDTO ladeZaehldatenStepline,
            final BigDecimal value) {
        final int currentValue = ladeZaehldatenStepline.getRangeMaxPercent();
        ladeZaehldatenStepline.setRangeMaxPercent(
                ZaehldatenProcessingUtil.getValueRounded(
                        BigDecimal.valueOf(currentValue)
                                .max(ZaehldatenProcessingUtil.getZeroIfNull(value)),
                        ROUNDING_VALUE_PERCENT));
    }

    protected static void setLegendInZaehldatenStepline(final LadeZaehldatenSteplineDTO ladeZaehldatenStepline,
            final String legendEntry) {
        ladeZaehldatenStepline.setLegend(
                ChartLegendUtil.checkAndAddToLegendWhenNotAvailable(
                        ladeZaehldatenStepline.getLegend(),
                        legendEntry));
    }

    protected static void setSeriesIndexForFirstChartValue(final StepLineSeriesEntryBaseDTO stepLineSeriesEntry) {
        stepLineSeriesEntry.setXAxisIndex(0);
        stepLineSeriesEntry.setYAxisIndex(0);
    }

    protected static void setSeriesIndexForFirstChartPercent(final StepLineSeriesEntryBaseDTO stepLineSeriesEntry) {
        stepLineSeriesEntry.setXAxisIndex(0);
        stepLineSeriesEntry.setYAxisIndex(1);
    }

    /**
     * Innere Helfer-Klasse welche {@link StepLineSeriesEntryIntegerDTO} und
     * {@link StepLineSeriesEntryBigDecimalDTO} nach Fahrzeugklasse und Fahrzeugkategorie
     * aufgliedert und vorhält.
     */
    @Getter
    @Setter
    private static class SeriesEntries {

        private StepLineSeriesEntryBigDecimalDTO seriesEntryPkw;

        private StepLineSeriesEntryBigDecimalDTO seriesEntryLkw;

        private StepLineSeriesEntryBigDecimalDTO seriesEntryLfw;

        private StepLineSeriesEntryBigDecimalDTO seriesEntryLz;

        private StepLineSeriesEntryBigDecimalDTO seriesEntryBus;

        private StepLineSeriesEntryBigDecimalDTO seriesEntryKrad;

        private StepLineSeriesEntryBigDecimalDTO seriesEntryRad;

        private StepLineSeriesEntryBigDecimalDTO seriesEntryKfz;

        private StepLineSeriesEntryBigDecimalDTO seriesEntrySv;

        private StepLineSeriesEntryBigDecimalDTO seriesEntrySvProzent;

        private StepLineSeriesEntryBigDecimalDTO seriesEntryGv;

        private StepLineSeriesEntryBigDecimalDTO seriesEntryGvProzent;

        public SeriesEntries() {
            seriesEntryPkw = new StepLineSeriesEntryBigDecimalDTO();
            seriesEntryPkw.setName(ChartLegendUtil.PKW);
            seriesEntryLkw = new StepLineSeriesEntryBigDecimalDTO();
            seriesEntryLkw.setName(ChartLegendUtil.LKW);
            seriesEntryLfw = new StepLineSeriesEntryBigDecimalDTO();
            seriesEntryLfw.setName(ChartLegendUtil.LFW);
            seriesEntryLz = new StepLineSeriesEntryBigDecimalDTO();
            seriesEntryLz.setName(ChartLegendUtil.LASTZUEGE);
            seriesEntryBus = new StepLineSeriesEntryBigDecimalDTO();
            seriesEntryBus.setName(ChartLegendUtil.BUSSE);
            seriesEntryKrad = new StepLineSeriesEntryBigDecimalDTO();
            seriesEntryKrad.setName(ChartLegendUtil.KRAFTRAEDER);
            seriesEntryRad = new StepLineSeriesEntryBigDecimalDTO();
            seriesEntryRad.setName(ChartLegendUtil.RAD);
            seriesEntryKfz = new StepLineSeriesEntryBigDecimalDTO();
            seriesEntryKfz.setName(ChartLegendUtil.KFZ);
            seriesEntrySv = new StepLineSeriesEntryBigDecimalDTO();
            seriesEntrySv.setName(ChartLegendUtil.SCHWERVERKEHR);
            seriesEntrySvProzent = new StepLineSeriesEntryBigDecimalDTO();
            seriesEntrySvProzent.setName(ChartLegendUtil.SCHWERVERKEHR_ANTEIL_PROZENT);
            seriesEntryGv = new StepLineSeriesEntryBigDecimalDTO();
            seriesEntryGv.setName(ChartLegendUtil.GUETERVERKEHR);
            seriesEntryGvProzent = new StepLineSeriesEntryBigDecimalDTO();
            seriesEntryGvProzent.setName(ChartLegendUtil.GUETERVERKEHR_ANTEIL_PROZENT);
        }

        private static void addSeriesToAllEntriesIfChosen(final List<StepLineSeriesEntryBaseDTO> allEntries,
                final StepLineSeriesEntryBaseDTO entry,
                final Boolean isChosen) {
            if (isChosen) {
                allEntries.add(entry);
            }
        }

        /**
         * Gibt alle {@link StepLineSeriesEntryIntegerDTO} und {@link StepLineSeriesEntryBigDecimalDTO}
         * entsprechend der im Parameter options gewählten
         * Fahrzeugklassen, Fahrzeugkategorien und Prozentwerte als Liste zurück.
         *
         * @return Liste mit den erwünschten {@link StepLineSeriesEntryIntegerDTO} und
         *         {@link StepLineSeriesEntryBigDecimalDTO}.
         */
        public List<StepLineSeriesEntryBaseDTO> getChosenStepLineSeriesEntries() {
            final List<StepLineSeriesEntryBaseDTO> allEntries = new ArrayList<>();
            addSeriesToAllEntriesIfChosen(allEntries, seriesEntryPkw, true);
            addSeriesToAllEntriesIfChosen(allEntries, seriesEntryLkw, true);
            addSeriesToAllEntriesIfChosen(allEntries, seriesEntryLfw, true);
            addSeriesToAllEntriesIfChosen(allEntries, seriesEntryLz, true);
            addSeriesToAllEntriesIfChosen(allEntries, seriesEntryBus, true);
            addSeriesToAllEntriesIfChosen(allEntries, seriesEntryKrad, true);
            addSeriesToAllEntriesIfChosen(allEntries, seriesEntryRad, true);
            addSeriesToAllEntriesIfChosen(allEntries, seriesEntryKfz, true);
            addSeriesToAllEntriesIfChosen(allEntries, seriesEntrySv, true);
            addSeriesToAllEntriesIfChosen(allEntries, seriesEntrySvProzent, true);
            addSeriesToAllEntriesIfChosen(allEntries, seriesEntryGv, true);
            addSeriesToAllEntriesIfChosen(allEntries, seriesEntryGvProzent, true);
            return allEntries;
        }

    }

}
