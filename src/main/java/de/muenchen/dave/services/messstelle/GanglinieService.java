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

    private static final Integer ZERO = 0;

    private static final Integer ONE = 1;

    private static final Integer ROUNDING_VALUE = 20;

    private static final Integer ROUNDING_VALUE_PERCENT = 2;

    // Refactoring: Synergieeffekt mit ProcessZaehldatenSteplineService nutzen
    public LadeZaehldatenSteplineDTO ladeGanglinie(final List<IntervalDto> intervalle, final MessstelleOptionsDTO options) {
        log.debug("#ladeGanglinie");
        final LadeZaehldatenSteplineDTO ladeZaehldatenStepline = new LadeZaehldatenSteplineDTO();
        ladeZaehldatenStepline.setRangeMax(0);
        ladeZaehldatenStepline.setRangeMaxPercent(0);
        ladeZaehldatenStepline.setLegend(new ArrayList<>());
        ladeZaehldatenStepline.setXAxisDataFirstChart(new ArrayList<>());
        ladeZaehldatenStepline.setSeriesEntriesFirstChart(new ArrayList<>());

        final SeriesEntries seriesEntries = new SeriesEntries();
        final FahrzeugOptionsDTO fahrzeuge = options.getFahrzeuge();

        intervalle
                .forEach(intervall -> {
                    if (fahrzeuge.isPersonenkraftwagen()) {
                        setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryPkw());
                        seriesEntries.getSeriesEntryPkw().getYAxisData().add(intervall.getSummeAllePkw().intValue());
                        setLegendInZaehldatenStepline(ladeZaehldatenStepline, ChartLegendUtil.PKW);
                        setRangeMaxRoundedToHundredInZaehldatenStepline(ladeZaehldatenStepline, intervall.getSummeAllePkw().intValue());
                    }

                    if (fahrzeuge.isLastkraftwagen()) {
                        setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryLkw());
                        seriesEntries.getSeriesEntryLkw().getYAxisData().add(intervall.getAnzahlLkw().intValue());
                        setLegendInZaehldatenStepline(ladeZaehldatenStepline, ChartLegendUtil.LKW);
                        setRangeMaxRoundedToHundredInZaehldatenStepline(ladeZaehldatenStepline, intervall.getAnzahlLkw().intValue());
                    }
                    if (fahrzeuge.isLastzuege()) {
                        setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryLz());
                        seriesEntries.getSeriesEntryLz().getYAxisData().add(intervall.getSummeLastzug().intValue());
                        setLegendInZaehldatenStepline(ladeZaehldatenStepline, ChartLegendUtil.LASTZUEGE);
                        setRangeMaxRoundedToHundredInZaehldatenStepline(ladeZaehldatenStepline, intervall.getSummeLastzug().intValue());
                    }
                    if (fahrzeuge.isLieferwagen()) {
                        setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryLfw());
                        seriesEntries.getSeriesEntryLfw().getYAxisData().add(intervall.getAnzahlLfw().intValue());
                        setLegendInZaehldatenStepline(ladeZaehldatenStepline, ChartLegendUtil.LFW);
                        setRangeMaxRoundedToHundredInZaehldatenStepline(ladeZaehldatenStepline, intervall.getAnzahlLfw().intValue());
                    }
                    if (fahrzeuge.isBusse()) {
                        setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryBus());
                        seriesEntries.getSeriesEntryBus().getYAxisData().add(intervall.getAnzahlBus().intValue());
                        setLegendInZaehldatenStepline(ladeZaehldatenStepline, ChartLegendUtil.BUSSE);
                        setRangeMaxRoundedToHundredInZaehldatenStepline(ladeZaehldatenStepline, intervall.getAnzahlBus().intValue());
                    }
                    if (fahrzeuge.isKraftraeder()) {
                        setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryKrad());
                        seriesEntries.getSeriesEntryKrad().getYAxisData().add(intervall.getAnzahlKrad().intValue());
                        setLegendInZaehldatenStepline(ladeZaehldatenStepline, ChartLegendUtil.KRAFTRAEDER);
                        setRangeMaxRoundedToHundredInZaehldatenStepline(ladeZaehldatenStepline, intervall.getAnzahlKrad().intValue());
                    }
                    if (fahrzeuge.isRadverkehr()) {
                        setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryRad());
                        seriesEntries.getSeriesEntryRad().getYAxisData().add(intervall.getAnzahlRad().intValue());
                        setLegendInZaehldatenStepline(ladeZaehldatenStepline, ChartLegendUtil.RAD);
                        setRangeMaxRoundedToHundredInZaehldatenStepline(ladeZaehldatenStepline, intervall.getAnzahlRad().intValue());
                    }
                    if (fahrzeuge.isFussverkehr()) {
                        setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryFuss());
                        //seriesEntries.getSeriesEntryFuss().getYAxisData().add(intervall.getAnzahlFuss());
                        setLegendInZaehldatenStepline(ladeZaehldatenStepline, ChartLegendUtil.FUSSGAENGER);
                        //setRangeMaxRoundedToHundredInZaehldatenStepline(ladeZaehldatenStepline, intervall.getAnzahlFuss());
                    }
                    if (fahrzeuge.isKraftfahrzeugverkehr()) {
                        setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryKfz());
                        seriesEntries.getSeriesEntryKfz().getYAxisData().add(intervall.getSummeKraftfahrzeugverkehr().intValue());
                        setLegendInZaehldatenStepline(ladeZaehldatenStepline, ChartLegendUtil.KFZ);
                        setRangeMaxRoundedToHundredInZaehldatenStepline(ladeZaehldatenStepline, intervall.getSummeKraftfahrzeugverkehr().intValue());
                    }
                    if (fahrzeuge.isSchwerverkehr()) {
                        setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntrySv());
                        seriesEntries.getSeriesEntrySv().getYAxisData().add(intervall.getSummeSchwerverkehr().intValue());
                        setLegendInZaehldatenStepline(ladeZaehldatenStepline, ChartLegendUtil.SCHWERVERKEHR);
                        setRangeMaxRoundedToHundredInZaehldatenStepline(ladeZaehldatenStepline, intervall.getSummeSchwerverkehr().intValue());
                    }
                    if (fahrzeuge.isSchwerverkehrsanteilProzent()) {
                        setSeriesIndexForFirstChartPercent(seriesEntries.getSeriesEntrySvProzent());
                        seriesEntries.getSeriesEntrySvProzent().getYAxisData().add(BigDecimal.valueOf(intervall.getProzentSchwerverkehr().intValue()));
                        setLegendInZaehldatenStepline(ladeZaehldatenStepline, ChartLegendUtil.SCHWERVERKEHR_ANTEIL_PROZENT);
                        setRangeMaxPercentInZaehldatenStepline(ladeZaehldatenStepline, BigDecimal.valueOf(intervall.getProzentSchwerverkehr().intValue()));
                    }
                    if (fahrzeuge.isGueterverkehr()) {
                        setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryGv());
                        seriesEntries.getSeriesEntryGv().getYAxisData().add(intervall.getSummeGueterverkehr().intValue());
                        setLegendInZaehldatenStepline(ladeZaehldatenStepline, ChartLegendUtil.GUETERVERKEHR);
                        setRangeMaxRoundedToHundredInZaehldatenStepline(ladeZaehldatenStepline, intervall.getSummeGueterverkehr().intValue());
                    }
                    if (fahrzeuge.isGueterverkehrsanteilProzent()) {
                        setSeriesIndexForFirstChartPercent(seriesEntries.getSeriesEntryGvProzent());
                        seriesEntries.getSeriesEntryGvProzent().getYAxisData().add(intervall.getProzentGueterverkehr());
                        setLegendInZaehldatenStepline(ladeZaehldatenStepline, ChartLegendUtil.GUETERVERKEHR_ANTEIL_PROZENT);
                        setRangeMaxPercentInZaehldatenStepline(ladeZaehldatenStepline, intervall.getProzentGueterverkehr());
                    }

                    ladeZaehldatenStepline.setXAxisDataFirstChart(
                            ZaehldatenProcessingUtil.checkAndAddToXAxisWhenNotAvailable(
                                    ladeZaehldatenStepline.getXAxisDataFirstChart(),
                                    intervall.getDatumUhrzeitVon().toLocalTime().toString()));
                });
        ladeZaehldatenStepline.setSeriesEntriesFirstChart(seriesEntries.getChosenStepLineSeriesEntries(fahrzeuge));
        return ladeZaehldatenStepline;
    }

    protected static void setRangeMaxRoundedToHundredInZaehldatenStepline(final LadeZaehldatenSteplineDTO ladeZaehldatenStepline,
            final Integer value) {
        ladeZaehldatenStepline.setRangeMax(
                ZaehldatenProcessingUtil.getValueRounded(
                        Math.max(
                                ZaehldatenProcessingUtil.getZeroIfNull(value),
                                ladeZaehldatenStepline.getRangeMax()),
                        ROUNDING_VALUE));
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
        stepLineSeriesEntry.setXAxisIndex(ZERO);
        stepLineSeriesEntry.setYAxisIndex(ZERO);
    }

    protected static void setSeriesIndexForFirstChartPercent(final StepLineSeriesEntryBaseDTO stepLineSeriesEntry) {
        stepLineSeriesEntry.setXAxisIndex(ZERO);
        stepLineSeriesEntry.setYAxisIndex(ONE);
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

        private StepLineSeriesEntryIntegerDTO seriesEntryFuss;

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
            seriesEntryFuss = new StepLineSeriesEntryIntegerDTO();
            seriesEntryFuss.setName(ChartLegendUtil.FUSSGAENGER);
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
        public List<StepLineSeriesEntryBaseDTO> getChosenStepLineSeriesEntries(final FahrzeugOptionsDTO options) {
            final List<StepLineSeriesEntryBaseDTO> allEntries = new ArrayList<>();
            addSeriesToAllEntriesIfChosen(allEntries, seriesEntryPkw, options.isPersonenkraftwagen());
            addSeriesToAllEntriesIfChosen(allEntries, seriesEntryLkw, options.isLastkraftwagen());
            addSeriesToAllEntriesIfChosen(allEntries, seriesEntryLz, options.isLastzuege());
            addSeriesToAllEntriesIfChosen(allEntries, seriesEntryLfw, options.isLieferwagen());
            addSeriesToAllEntriesIfChosen(allEntries, seriesEntryBus, options.isBusse());
            addSeriesToAllEntriesIfChosen(allEntries, seriesEntryKrad, options.isKraftraeder());
            addSeriesToAllEntriesIfChosen(allEntries, seriesEntryRad, options.isRadverkehr());
            addSeriesToAllEntriesIfChosen(allEntries, seriesEntryFuss, options.isFussverkehr());
            addSeriesToAllEntriesIfChosen(allEntries, seriesEntryKfz, options.isKraftfahrzeugverkehr());
            addSeriesToAllEntriesIfChosen(allEntries, seriesEntrySv, options.isSchwerverkehr());
            addSeriesToAllEntriesIfChosen(allEntries, seriesEntrySvProzent, options.isSchwerverkehrsanteilProzent());
            addSeriesToAllEntriesIfChosen(allEntries, seriesEntryGv, options.isGueterverkehr());
            addSeriesToAllEntriesIfChosen(allEntries, seriesEntryGvProzent, options.isGueterverkehrsanteilProzent());
            return allEntries;
        }

    }

}
