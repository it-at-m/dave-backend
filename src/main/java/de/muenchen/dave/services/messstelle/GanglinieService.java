/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.services.messstelle;

import de.muenchen.dave.domain.dtos.laden.LadeZaehldatenSteplineDTO;
import de.muenchen.dave.domain.dtos.messstelle.FahrzeugOptionsDTO;
import de.muenchen.dave.geodateneai.gen.model.IntervalDto;
import de.muenchen.dave.util.ChartLegendUtil;
import de.muenchen.dave.util.ZaehldatenProcessingUtil;
import de.muenchen.dave.util.messstelle.GanglinieUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class GanglinieService {

    // Refactoring: Synergieeffekt mit ProcessZaehldatenSteplineService nutzen
    public LadeZaehldatenSteplineDTO ladeGanglinie(final List<IntervalDto> intervals, final FahrzeugOptionsDTO fahrzeugOptions) {
        log.debug("#ladeGanglinie");

        final var ladeZaehldatenStepline = GanglinieUtil.getInitialZaehldatenStepline();
        final var seriesEntries = new GanglinieUtil.SeriesEntries();

        intervals
                .forEach(interval -> {
                    if (fahrzeugOptions.isPersonenkraftwagen()) {
                        GanglinieUtil.setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryPkw());
                        seriesEntries.getSeriesEntryPkw().getYAxisData().add(GanglinieUtil.getIntValueIfNotNull(interval.getSummeAllePkw()));
                        GanglinieUtil.setLegendInZaehldatenStepline(ladeZaehldatenStepline, ChartLegendUtil.PKW);
                        GanglinieUtil.setRangeMaxRoundedToTwentyInZaehldatenStepline(ladeZaehldatenStepline, GanglinieUtil.getIntValueIfNotNull(interval.getSummeAllePkw()));
                    }
                    if (fahrzeugOptions.isLastkraftwagen()) {
                        GanglinieUtil.setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryLkw());
                        seriesEntries.getSeriesEntryLkw().getYAxisData().add(GanglinieUtil.getIntValueIfNotNull(interval.getAnzahlLkw()));
                        GanglinieUtil.setLegendInZaehldatenStepline(ladeZaehldatenStepline, ChartLegendUtil.LKW);
                        GanglinieUtil.setRangeMaxRoundedToTwentyInZaehldatenStepline(ladeZaehldatenStepline, GanglinieUtil.getIntValueIfNotNull(interval.getAnzahlLkw()));
                    }
                    if (fahrzeugOptions.isLastzuege()) {
                        GanglinieUtil.setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryLz());
                        seriesEntries.getSeriesEntryLz().getYAxisData().add(GanglinieUtil.getIntValueIfNotNull(interval.getSummeLastzug()));
                        GanglinieUtil.setLegendInZaehldatenStepline(ladeZaehldatenStepline, ChartLegendUtil.LASTZUEGE);
                        GanglinieUtil.setRangeMaxRoundedToTwentyInZaehldatenStepline(ladeZaehldatenStepline, GanglinieUtil.getIntValueIfNotNull(interval.getSummeLastzug()));
                    }
                    if (fahrzeugOptions.isLieferwagen()) {
                        GanglinieUtil.setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryLfw());
                        seriesEntries.getSeriesEntryLfw().getYAxisData().add(GanglinieUtil.getIntValueIfNotNull(interval.getAnzahlLfw()));
                        GanglinieUtil.setLegendInZaehldatenStepline(ladeZaehldatenStepline, ChartLegendUtil.LFW);
                        GanglinieUtil.setRangeMaxRoundedToTwentyInZaehldatenStepline(ladeZaehldatenStepline, GanglinieUtil.getIntValueIfNotNull(interval.getAnzahlLfw()));
                    }
                    if (fahrzeugOptions.isBusse()) {
                        GanglinieUtil.setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryBus());
                        seriesEntries.getSeriesEntryBus().getYAxisData().add(GanglinieUtil.getIntValueIfNotNull(interval.getAnzahlBus()));
                        GanglinieUtil.setLegendInZaehldatenStepline(ladeZaehldatenStepline, ChartLegendUtil.BUSSE);
                        GanglinieUtil.setRangeMaxRoundedToTwentyInZaehldatenStepline(ladeZaehldatenStepline, GanglinieUtil.getIntValueIfNotNull(interval.getAnzahlBus()));
                    }
                    if (fahrzeugOptions.isKraftraeder()) {
                        GanglinieUtil.setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryKrad());
                        seriesEntries.getSeriesEntryKrad().getYAxisData().add(GanglinieUtil.getIntValueIfNotNull(interval.getAnzahlKrad()));
                        GanglinieUtil.setLegendInZaehldatenStepline(ladeZaehldatenStepline, ChartLegendUtil.KRAFTRAEDER);
                        GanglinieUtil.setRangeMaxRoundedToTwentyInZaehldatenStepline(ladeZaehldatenStepline, GanglinieUtil.getIntValueIfNotNull(interval.getAnzahlKrad()));
                    }
                    if (fahrzeugOptions.isRadverkehr()) {
                        GanglinieUtil.setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryRad());
                        seriesEntries.getSeriesEntryRad().getYAxisData().add(GanglinieUtil.getIntValueIfNotNull(interval.getAnzahlRad()));
                        GanglinieUtil.setLegendInZaehldatenStepline(ladeZaehldatenStepline, ChartLegendUtil.RAD);
                        GanglinieUtil.setRangeMaxRoundedToTwentyInZaehldatenStepline(ladeZaehldatenStepline, GanglinieUtil.getIntValueIfNotNull(interval.getAnzahlRad()));
                    }
                    if (fahrzeugOptions.isKraftfahrzeugverkehr()) {
                        GanglinieUtil.setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryKfz());
                        seriesEntries.getSeriesEntryKfz().getYAxisData().add(GanglinieUtil.getIntValueIfNotNull(interval.getSummeKraftfahrzeugverkehr()));
                        GanglinieUtil.setLegendInZaehldatenStepline(ladeZaehldatenStepline, ChartLegendUtil.KFZ);
                        GanglinieUtil.setRangeMaxRoundedToTwentyInZaehldatenStepline(ladeZaehldatenStepline, GanglinieUtil.getIntValueIfNotNull(interval.getSummeKraftfahrzeugverkehr()));
                    }
                    if (fahrzeugOptions.isSchwerverkehr()) {
                        GanglinieUtil.setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntrySv());
                        seriesEntries.getSeriesEntrySv().getYAxisData().add(GanglinieUtil.getIntValueIfNotNull(interval.getSummeSchwerverkehr()));
                        GanglinieUtil.setLegendInZaehldatenStepline(ladeZaehldatenStepline, ChartLegendUtil.SCHWERVERKEHR);
                        GanglinieUtil.setRangeMaxRoundedToTwentyInZaehldatenStepline(ladeZaehldatenStepline, GanglinieUtil.getIntValueIfNotNull(interval.getSummeSchwerverkehr()));
                    }
                    if (fahrzeugOptions.isSchwerverkehrsanteilProzent()) {
                        GanglinieUtil.setSeriesIndexForFirstChartPercent(seriesEntries.getSeriesEntrySvProzent());
                        seriesEntries.getSeriesEntrySvProzent().getYAxisData().add(interval.getProzentSchwerverkehr());
                        GanglinieUtil.setLegendInZaehldatenStepline(ladeZaehldatenStepline, ChartLegendUtil.SCHWERVERKEHR_ANTEIL_PROZENT);
                        GanglinieUtil.setRangeMaxPercentRoundedToTwoInZaehldatenStepline(ladeZaehldatenStepline, interval.getProzentSchwerverkehr());
                    }
                    if (fahrzeugOptions.isGueterverkehr()) {
                        GanglinieUtil.setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryGv());
                        seriesEntries.getSeriesEntryGv().getYAxisData().add(GanglinieUtil.getIntValueIfNotNull(interval.getSummeGueterverkehr()));
                        GanglinieUtil.setLegendInZaehldatenStepline(ladeZaehldatenStepline, ChartLegendUtil.GUETERVERKEHR);
                        GanglinieUtil.setRangeMaxRoundedToTwentyInZaehldatenStepline(ladeZaehldatenStepline, GanglinieUtil.getIntValueIfNotNull(interval.getSummeGueterverkehr()));
                    }
                    if (fahrzeugOptions.isGueterverkehrsanteilProzent()) {
                        GanglinieUtil.setSeriesIndexForFirstChartPercent(seriesEntries.getSeriesEntryGvProzent());
                        seriesEntries.getSeriesEntryGvProzent().getYAxisData().add(interval.getProzentGueterverkehr());
                        GanglinieUtil.setLegendInZaehldatenStepline(ladeZaehldatenStepline, ChartLegendUtil.GUETERVERKEHR_ANTEIL_PROZENT);
                        GanglinieUtil.setRangeMaxPercentRoundedToTwoInZaehldatenStepline(ladeZaehldatenStepline, interval.getProzentGueterverkehr());
                    }

                    final var currentXAxisData = ladeZaehldatenStepline.getXAxisDataFirstChart();
                    final var newXAxisData = ZaehldatenProcessingUtil.checkAndAddToXAxisWhenNotAvailable(
                            currentXAxisData,
                            interval.getDatumUhrzeitVon().toLocalTime().toString());
                    ladeZaehldatenStepline.setXAxisDataFirstChart(newXAxisData);
                });

        ladeZaehldatenStepline.setSeriesEntriesFirstChart(seriesEntries.getChosenStepLineSeriesEntries(fahrzeugOptions));
        return ladeZaehldatenStepline;
    }

}
