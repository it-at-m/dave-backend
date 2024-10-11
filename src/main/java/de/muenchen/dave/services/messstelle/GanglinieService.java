/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.services.messstelle;

import de.muenchen.dave.domain.dtos.laden.LadeZaehldatenSteplineDTO;
import de.muenchen.dave.domain.dtos.laden.StepLineSeriesEntryBaseDTO;
import de.muenchen.dave.domain.dtos.laden.StepLineSeriesEntryBigDecimalDTO;
import de.muenchen.dave.domain.dtos.laden.StepLineSeriesEntryIntegerDTO;
import de.muenchen.dave.domain.dtos.messstelle.FahrzeugOptionsDTO;
import de.muenchen.dave.domain.dtos.messstelle.MessstelleOptionsDTO;
import de.muenchen.dave.geodateneai.gen.model.IntervalDto;
import de.muenchen.dave.util.ChartLegendUtil;
import de.muenchen.dave.util.ZaehldatenProcessingUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class GanglinieService {

    private static final Integer ZERO = 0;

    private static final Integer ONE = 1;

    private static final Integer ROUNDING_VALUE = 20;

    private static final Integer ROUNDING_VALUE_PERCENT = 2;

    protected static void setRangeMaxRoundedToTwentyInZaehldatenStepline(
            final LadeZaehldatenSteplineDTO ladeZaehldatenStepline,
            final Integer value) {
        final var currentValue = ladeZaehldatenStepline.getRangeMax();
        final Integer newRoundedValue;
        if (Objects.isNull(currentValue)) {
            newRoundedValue = ZaehldatenProcessingUtil.getValueRounded(
                    ZaehldatenProcessingUtil.getZeroIfNull(value),
                    ROUNDING_VALUE);
        } else {
            newRoundedValue = ZaehldatenProcessingUtil.getValueRounded(
                    Math.max(
                            ZaehldatenProcessingUtil.getZeroIfNull(value),
                            ladeZaehldatenStepline.getRangeMax()),
                    ROUNDING_VALUE);
        }
        ladeZaehldatenStepline.setRangeMax(newRoundedValue);
    }

    protected static void setRangeMaxPercentRoundedToTwoInZaehldatenStepline(
            final LadeZaehldatenSteplineDTO ladeZaehldatenStepline,
            final BigDecimal value) {

        final var currentValue = ladeZaehldatenStepline.getRangeMaxPercent();
        final Integer newRoundedValue;
        if (Objects.isNull(currentValue)) {
            newRoundedValue = ZaehldatenProcessingUtil.getValueRounded(
                    ZaehldatenProcessingUtil.getZeroIfNull(value),
                    ROUNDING_VALUE_PERCENT);
        } else {
            newRoundedValue = ZaehldatenProcessingUtil.getValueRounded(
                    BigDecimal.valueOf(currentValue).max(ZaehldatenProcessingUtil.getZeroIfNull(value)),
                    ROUNDING_VALUE_PERCENT);
        }
        ladeZaehldatenStepline.setRangeMaxPercent(newRoundedValue);
    }

    protected static void setLegendInZaehldatenStepline(
            final LadeZaehldatenSteplineDTO ladeZaehldatenStepline,
            final String legendEntry) {
        ladeZaehldatenStepline.setLegend(
                ChartLegendUtil.checkAndAddToLegendWhenNotAvailable(
                        ladeZaehldatenStepline.getLegend(),
                        legendEntry));
    }

    protected static void setSeriesIndexForFirstChartValue(final StepLineSeriesEntryBaseDTO stepLineSeriesEntry) {
        stepLineSeriesEntry.setXAxisIndex(ZERO);
        stepLineSeriesEntry.setYAxisIndex(ZERO);
    }

    protected static void setSeriesIndexForFirstChartPercent(final StepLineSeriesEntryBaseDTO stepLineSeriesEntry) {
        stepLineSeriesEntry.setXAxisIndex(ZERO);
        stepLineSeriesEntry.setYAxisIndex(ONE);
    }

    // Refactoring: Synergieeffekt mit ProcessZaehldatenSteplineService nutzen
    public LadeZaehldatenSteplineDTO ladeGanglinie(final List<IntervalDto> intervals, final MessstelleOptionsDTO options) {
        log.debug("#ladeGanglinie");
        final var ladeZaehldatenStepline = new LadeZaehldatenSteplineDTO();
        ladeZaehldatenStepline.setRangeMax(0);
        ladeZaehldatenStepline.setRangeMaxPercent(0);
        ladeZaehldatenStepline.setLegend(new ArrayList<>());
        ladeZaehldatenStepline.setXAxisDataFirstChart(new ArrayList<>());
        ladeZaehldatenStepline.setSeriesEntriesFirstChart(new ArrayList<>());

        final var seriesEntries = new SeriesEntries();
        final var fahrzeugOptions = options.getFahrzeuge();

        intervals
                .forEach(interval -> {
                    if (fahrzeugOptions.isPersonenkraftwagen()) {
                        setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryPkw());
                        seriesEntries.getSeriesEntryPkw().getYAxisData().add(getIntValueIfNotNull(interval.getSummeAllePkw()));
                        setLegendInZaehldatenStepline(ladeZaehldatenStepline, ChartLegendUtil.PKW);
                        setRangeMaxRoundedToTwentyInZaehldatenStepline(ladeZaehldatenStepline, getIntValueIfNotNull(interval.getSummeAllePkw()));
                    }
                    if (fahrzeugOptions.isLastkraftwagen()) {
                        setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryLkw());
                        seriesEntries.getSeriesEntryLkw().getYAxisData().add(getIntValueIfNotNull(interval.getAnzahlLkw()));
                        setLegendInZaehldatenStepline(ladeZaehldatenStepline, ChartLegendUtil.LKW);
                        setRangeMaxRoundedToTwentyInZaehldatenStepline(ladeZaehldatenStepline, getIntValueIfNotNull(interval.getAnzahlLkw()));
                    }
                    if (fahrzeugOptions.isLastzuege()) {
                        setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryLz());
                        seriesEntries.getSeriesEntryLz().getYAxisData().add(getIntValueIfNotNull(interval.getSummeLastzug()));
                        setLegendInZaehldatenStepline(ladeZaehldatenStepline, ChartLegendUtil.LASTZUEGE);
                        setRangeMaxRoundedToTwentyInZaehldatenStepline(ladeZaehldatenStepline, getIntValueIfNotNull(interval.getSummeLastzug()));
                    }
                    if (fahrzeugOptions.isLieferwagen()) {
                        setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryLfw());
                        seriesEntries.getSeriesEntryLfw().getYAxisData().add(getIntValueIfNotNull(interval.getAnzahlLfw()));
                        setLegendInZaehldatenStepline(ladeZaehldatenStepline, ChartLegendUtil.LFW);
                        setRangeMaxRoundedToTwentyInZaehldatenStepline(ladeZaehldatenStepline, getIntValueIfNotNull(interval.getAnzahlLfw()));
                    }
                    if (fahrzeugOptions.isBusse()) {
                        setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryBus());
                        seriesEntries.getSeriesEntryBus().getYAxisData().add(getIntValueIfNotNull(interval.getAnzahlBus()));
                        setLegendInZaehldatenStepline(ladeZaehldatenStepline, ChartLegendUtil.BUSSE);
                        setRangeMaxRoundedToTwentyInZaehldatenStepline(ladeZaehldatenStepline, getIntValueIfNotNull(interval.getAnzahlBus()));
                    }
                    if (fahrzeugOptions.isKraftraeder()) {
                        setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryKrad());
                        seriesEntries.getSeriesEntryKrad().getYAxisData().add(getIntValueIfNotNull(interval.getAnzahlKrad()));
                        setLegendInZaehldatenStepline(ladeZaehldatenStepline, ChartLegendUtil.KRAFTRAEDER);
                        setRangeMaxRoundedToTwentyInZaehldatenStepline(ladeZaehldatenStepline, getIntValueIfNotNull(interval.getAnzahlKrad()));
                    }
                    if (fahrzeugOptions.isRadverkehr()) {
                        setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryRad());
                        seriesEntries.getSeriesEntryRad().getYAxisData().add(getIntValueIfNotNull(interval.getAnzahlRad()));
                        setLegendInZaehldatenStepline(ladeZaehldatenStepline, ChartLegendUtil.RAD);
                        setRangeMaxRoundedToTwentyInZaehldatenStepline(ladeZaehldatenStepline, getIntValueIfNotNull(interval.getAnzahlRad()));
                    }
                    if (fahrzeugOptions.isKraftfahrzeugverkehr()) {
                        setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryKfz());
                        seriesEntries.getSeriesEntryKfz().getYAxisData().add(getIntValueIfNotNull(interval.getSummeKraftfahrzeugverkehr()));
                        setLegendInZaehldatenStepline(ladeZaehldatenStepline, ChartLegendUtil.KFZ);
                        setRangeMaxRoundedToTwentyInZaehldatenStepline(ladeZaehldatenStepline, getIntValueIfNotNull(interval.getSummeKraftfahrzeugverkehr()));
                    }
                    if (fahrzeugOptions.isSchwerverkehr()) {
                        setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntrySv());
                        seriesEntries.getSeriesEntrySv().getYAxisData().add(getIntValueIfNotNull(interval.getSummeSchwerverkehr()));
                        setLegendInZaehldatenStepline(ladeZaehldatenStepline, ChartLegendUtil.SCHWERVERKEHR);
                        setRangeMaxRoundedToTwentyInZaehldatenStepline(ladeZaehldatenStepline, getIntValueIfNotNull(interval.getSummeSchwerverkehr()));
                    }
                    if (fahrzeugOptions.isSchwerverkehrsanteilProzent()) {
                        setSeriesIndexForFirstChartPercent(seriesEntries.getSeriesEntrySvProzent());
                        seriesEntries.getSeriesEntrySvProzent().getYAxisData().add(interval.getProzentSchwerverkehr());
                        setLegendInZaehldatenStepline(ladeZaehldatenStepline, ChartLegendUtil.SCHWERVERKEHR_ANTEIL_PROZENT);
                        setRangeMaxPercentRoundedToTwoInZaehldatenStepline(ladeZaehldatenStepline, interval.getProzentSchwerverkehr());
                    }
                    if (fahrzeugOptions.isGueterverkehr()) {
                        setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryGv());
                        seriesEntries.getSeriesEntryGv().getYAxisData().add(getIntValueIfNotNull(interval.getSummeGueterverkehr()));
                        setLegendInZaehldatenStepline(ladeZaehldatenStepline, ChartLegendUtil.GUETERVERKEHR);
                        setRangeMaxRoundedToTwentyInZaehldatenStepline(ladeZaehldatenStepline, getIntValueIfNotNull(interval.getSummeGueterverkehr()));
                    }
                    if (fahrzeugOptions.isGueterverkehrsanteilProzent()) {
                        setSeriesIndexForFirstChartPercent(seriesEntries.getSeriesEntryGvProzent());
                        seriesEntries.getSeriesEntryGvProzent().getYAxisData().add(interval.getProzentGueterverkehr());
                        setLegendInZaehldatenStepline(ladeZaehldatenStepline, ChartLegendUtil.GUETERVERKEHR_ANTEIL_PROZENT);
                        setRangeMaxPercentRoundedToTwoInZaehldatenStepline(ladeZaehldatenStepline, interval.getProzentGueterverkehr());
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

    protected Integer getIntValueIfNotNull(final BigDecimal value) {
        return value == null
                ? null
                : value.intValue();
    }

    /**
     * Innere Helfer-Klasse welche {@link StepLineSeriesEntryIntegerDTO} und
     * {@link StepLineSeriesEntryBigDecimalDTO} nach Fahrzeugklasse und Fahrzeugkategorie
     * aufgliedert und vorhält.
     */
    @Getter
    @Setter
    private static class SeriesEntries {

        private StepLineSeriesEntryIntegerDTO seriesEntryPkw;

        private StepLineSeriesEntryIntegerDTO seriesEntryLkw;

        private StepLineSeriesEntryIntegerDTO seriesEntryLfw;

        private StepLineSeriesEntryIntegerDTO seriesEntryLz;

        private StepLineSeriesEntryIntegerDTO seriesEntryBus;

        private StepLineSeriesEntryIntegerDTO seriesEntryKrad;

        private StepLineSeriesEntryIntegerDTO seriesEntryRad;

        private StepLineSeriesEntryIntegerDTO seriesEntryKfz;

        private StepLineSeriesEntryIntegerDTO seriesEntrySv;

        private StepLineSeriesEntryBigDecimalDTO seriesEntrySvProzent;

        private StepLineSeriesEntryIntegerDTO seriesEntryGv;

        private StepLineSeriesEntryBigDecimalDTO seriesEntryGvProzent;

        public SeriesEntries() {
            seriesEntryPkw = new StepLineSeriesEntryIntegerDTO();
            seriesEntryPkw.setName(ChartLegendUtil.PKW);
            seriesEntryLkw = new StepLineSeriesEntryIntegerDTO();
            seriesEntryLkw.setName(ChartLegendUtil.LKW);
            seriesEntryLfw = new StepLineSeriesEntryIntegerDTO();
            seriesEntryLfw.setName(ChartLegendUtil.LFW);
            seriesEntryLz = new StepLineSeriesEntryIntegerDTO();
            seriesEntryLz.setName(ChartLegendUtil.LASTZUEGE);
            seriesEntryBus = new StepLineSeriesEntryIntegerDTO();
            seriesEntryBus.setName(ChartLegendUtil.BUSSE);
            seriesEntryKrad = new StepLineSeriesEntryIntegerDTO();
            seriesEntryKrad.setName(ChartLegendUtil.KRAFTRAEDER);
            seriesEntryRad = new StepLineSeriesEntryIntegerDTO();
            seriesEntryRad.setName(ChartLegendUtil.RAD);
            seriesEntryKfz = new StepLineSeriesEntryIntegerDTO();
            seriesEntryKfz.setName(ChartLegendUtil.KFZ);
            seriesEntrySv = new StepLineSeriesEntryIntegerDTO();
            seriesEntrySv.setName(ChartLegendUtil.SCHWERVERKEHR);
            seriesEntrySvProzent = new StepLineSeriesEntryBigDecimalDTO();
            seriesEntrySvProzent.setName(ChartLegendUtil.SCHWERVERKEHR_ANTEIL_PROZENT);
            seriesEntryGv = new StepLineSeriesEntryIntegerDTO();
            seriesEntryGv.setName(ChartLegendUtil.GUETERVERKEHR);
            seriesEntryGvProzent = new StepLineSeriesEntryBigDecimalDTO();
            seriesEntryGvProzent.setName(ChartLegendUtil.GUETERVERKEHR_ANTEIL_PROZENT);
        }

        private static void addSeriesToAllEntriesIfChosen(
                final List<StepLineSeriesEntryBaseDTO> allEntries,
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
        public List<StepLineSeriesEntryBaseDTO> getChosenStepLineSeriesEntries(final FahrzeugOptionsDTO options) {
            final List<StepLineSeriesEntryBaseDTO> allEntries = new ArrayList<>();
            addSeriesToAllEntriesIfChosen(allEntries, seriesEntryPkw, options.isPersonenkraftwagen());
            addSeriesToAllEntriesIfChosen(allEntries, seriesEntryLkw, options.isLastkraftwagen());
            addSeriesToAllEntriesIfChosen(allEntries, seriesEntryLz, options.isLastzuege());
            addSeriesToAllEntriesIfChosen(allEntries, seriesEntryLfw, options.isLieferwagen());
            addSeriesToAllEntriesIfChosen(allEntries, seriesEntryBus, options.isBusse());
            addSeriesToAllEntriesIfChosen(allEntries, seriesEntryKrad, options.isKraftraeder());
            addSeriesToAllEntriesIfChosen(allEntries, seriesEntryRad, options.isRadverkehr());
            addSeriesToAllEntriesIfChosen(allEntries, seriesEntryKfz, options.isKraftfahrzeugverkehr());
            addSeriesToAllEntriesIfChosen(allEntries, seriesEntrySv, options.isSchwerverkehr());
            addSeriesToAllEntriesIfChosen(allEntries, seriesEntrySvProzent, options.isSchwerverkehrsanteilProzent());
            addSeriesToAllEntriesIfChosen(allEntries, seriesEntryGv, options.isGueterverkehr());
            addSeriesToAllEntriesIfChosen(allEntries, seriesEntryGvProzent, options.isGueterverkehrsanteilProzent());
            return allEntries;
        }

    }

}
