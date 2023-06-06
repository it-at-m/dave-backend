/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.services.processzaehldaten;

import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatenSteplineDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatenTableDTO;
import de.muenchen.dave.domain.dtos.laden.StepLineSeriesEntryBaseDTO;
import de.muenchen.dave.domain.dtos.laden.StepLineSeriesEntryBigDecimalDTO;
import de.muenchen.dave.domain.dtos.laden.StepLineSeriesEntryIntegerDTO;
import de.muenchen.dave.domain.enums.Zaehldauer;
import de.muenchen.dave.domain.enums.Zeitblock;
import de.muenchen.dave.services.ladezaehldaten.LadeZaehldatenService;
import de.muenchen.dave.util.ChartLegendUtil;
import de.muenchen.dave.util.ZaehldatenProcessingUtil;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ProcessZaehldatenSteplineService {

    private static final Integer ROUNDING_VALUE = 20;

    private static final Integer ROUNDING_VALUE_PERCENT = 2;

    public static void setRangeMaxRoundedToHundredInZaehldatenStepline(final LadeZaehldatenSteplineDTO ladeZaehldatenStepline,
            final Integer value) {
        ladeZaehldatenStepline.setRangeMax(
                ZaehldatenProcessingUtil.getValueRounded(
                        Math.max(
                                ZaehldatenProcessingUtil.getZeroIfNull(value),
                                ladeZaehldatenStepline.getRangeMax()),
                        ROUNDING_VALUE));
    }

    public static void setRangeMaxRoundedToHundredInZaehldatenStepline(final LadeZaehldatenSteplineDTO ladeZaehldatenStepline,
            final BigDecimal value) {
        setRangeMaxRoundedToHundredInZaehldatenStepline(
                ladeZaehldatenStepline,
                ZaehldatenProcessingUtil.getZeroIfNull(value).intValue());
    }

    public static void setRangeMaxPercentInZaehldatenStepline(final LadeZaehldatenSteplineDTO ladeZaehldatenStepline,
            final BigDecimal value) {
        final int currentValue = ladeZaehldatenStepline.getRangeMaxPercent();
        ladeZaehldatenStepline.setRangeMaxPercent(
                ZaehldatenProcessingUtil.getValueRounded(
                        BigDecimal.valueOf(currentValue)
                                .max(ZaehldatenProcessingUtil.getZeroIfNull(value)),
                        ROUNDING_VALUE_PERCENT));
    }

    public static void setLegendInZaehldatenStepline(final LadeZaehldatenSteplineDTO ladeZaehldatenStepline,
            final String legendEntry) {
        ladeZaehldatenStepline.setLegend(
                ChartLegendUtil.checkAndAddToLegendWhenNotAvailable(
                        ladeZaehldatenStepline.getLegend(),
                        legendEntry));
    }

    /**
     * Falls sich in den options die Werte {@link Zeitblock#ZB_00_24} und
     * {@link Zaehldauer#DAUER_2_X_4_STUNDEN}
     * befinden, wird das Diagramm in zwei Unterdiagramme aufgeteilt.
     * Die Aufteilung der Daten für die beiden Unterdiagramme wird in der mitte der X-Achse
     * des Gesamtdiagramms vorgenommen.
     *
     * @param ladeZaehldatenStepline Die für ein Diagramm aufbereitete Daten.
     *            Die Unterteilung in Unterdiagramme ist noch nicht durchgeführt.
     * @param options Die {@link OptionsDTO} zur Prüfung auf {@link Zeitblock#ZB_00_24}
     *            und {@link Zaehldauer#DAUER_2_X_4_STUNDEN}.
     */
    public static void splitSeriesEntriesIntoFirstChartAndSecondChartIfNecessaryInLadeZaehldatenStepline(final LadeZaehldatenSteplineDTO ladeZaehldatenStepline,
            final OptionsDTO options) {
        if (options.getZeitblock().equals(Zeitblock.ZB_00_24)
                && options.getZaehldauer().equals(Zaehldauer.DAUER_2_X_4_STUNDEN)
                && !(StringUtils.equals(options.getZeitauswahl(), LadeZaehldatenService.ZEITAUSWAHL_SPITZENSTUNDE_KFZ)
                        || StringUtils.equals(options.getZeitauswahl(), LadeZaehldatenService.ZEITAUSWAHL_SPITZENSTUNDE_RAD)
                        || StringUtils.equals(options.getZeitauswahl(), LadeZaehldatenService.ZEITAUSWAHL_SPITZENSTUNDE_FUSS))) {
            final List<StepLineSeriesEntryBaseDTO> newSeriesEntriesFirstChart = new ArrayList<>();
            final List<StepLineSeriesEntryBaseDTO> newSeriesEntriesSecondChart = new ArrayList<>();
            final int splittedSize = ladeZaehldatenStepline.getXAxisDataFirstChart().size() / 2;

            // Split X axis data
            List<List<String>> splittetXAxisData = ListUtils.partition(
                    ladeZaehldatenStepline.getXAxisDataFirstChart(),
                    splittedSize);
            ladeZaehldatenStepline.setXAxisDataFirstChart(new ArrayList<>(splittetXAxisData.get(0)));
            ladeZaehldatenStepline.setXAxisDataSecondChart(new ArrayList<>(splittetXAxisData.get(1)));

            // Split StepLineSeriesEntries (Y axis data)
            ladeZaehldatenStepline.getSeriesEntriesFirstChart().forEach(serieEntryFirstChart -> {
                if (serieEntryFirstChart.getClass().equals(StepLineSeriesEntryIntegerDTO.class)) {
                    List<List<Integer>> splittedYAxisData = ListUtils.partition(
                            ((StepLineSeriesEntryIntegerDTO) serieEntryFirstChart).getYAxisData(),
                            splittedSize);
                    final StepLineSeriesEntryIntegerDTO stepLineSeriesEntryFirstChart = new StepLineSeriesEntryIntegerDTO();
                    stepLineSeriesEntryFirstChart.setName(serieEntryFirstChart.getName());
                    stepLineSeriesEntryFirstChart.setYAxisData(new ArrayList<>(splittedYAxisData.get(0)));
                    newSeriesEntriesFirstChart.add(stepLineSeriesEntryFirstChart);
                    final StepLineSeriesEntryIntegerDTO stepLineSeriesEntrySecondChart = new StepLineSeriesEntryIntegerDTO();
                    stepLineSeriesEntrySecondChart.setName(serieEntryFirstChart.getName());
                    stepLineSeriesEntrySecondChart.setYAxisData(new ArrayList<>(splittedYAxisData.get(1)));
                    newSeriesEntriesSecondChart.add(stepLineSeriesEntrySecondChart);
                    setSeriesIndexForChart(serieEntryFirstChart.getName(),
                            stepLineSeriesEntryFirstChart,
                            stepLineSeriesEntrySecondChart);
                } else {
                    List<List<BigDecimal>> splittedYAxisData = ListUtils.partition(
                            ((StepLineSeriesEntryBigDecimalDTO) serieEntryFirstChart).getYAxisData(),
                            splittedSize);
                    final StepLineSeriesEntryBigDecimalDTO stepLineSeriesEntryFirstChart = new StepLineSeriesEntryBigDecimalDTO();
                    stepLineSeriesEntryFirstChart.setName(serieEntryFirstChart.getName());
                    stepLineSeriesEntryFirstChart.setYAxisData(new ArrayList<>(splittedYAxisData.get(0)));
                    newSeriesEntriesFirstChart.add(stepLineSeriesEntryFirstChart);
                    final StepLineSeriesEntryBigDecimalDTO stepLineSeriesEntrySecondChart = new StepLineSeriesEntryBigDecimalDTO();
                    stepLineSeriesEntrySecondChart.setName(serieEntryFirstChart.getName());
                    stepLineSeriesEntrySecondChart.setYAxisData(new ArrayList<>(splittedYAxisData.get(1)));
                    newSeriesEntriesSecondChart.add(stepLineSeriesEntrySecondChart);
                    setSeriesIndexForChart(serieEntryFirstChart.getName(),
                            stepLineSeriesEntryFirstChart,
                            stepLineSeriesEntrySecondChart);
                }
            });
            ladeZaehldatenStepline.setSeriesEntriesFirstChart(newSeriesEntriesFirstChart);
            ladeZaehldatenStepline.setSeriesEntriesSecondChart(newSeriesEntriesSecondChart);
        }
    }

    public static void setSeriesIndexForChart(final String fahrzeugkategorie,
            final StepLineSeriesEntryBaseDTO seriesEntryFirstChart,
            final StepLineSeriesEntryBaseDTO seriesEntrySecondChart) {
        if (ChartLegendUtil.SCHWERVERKEHR_ANTEIL_PROZENT.equals(fahrzeugkategorie)
                || ChartLegendUtil.GUETERVERKEHR_ANTEIL_PROZENT.equals(fahrzeugkategorie)) {
            setSeriesIndexForFirstChartPercent(seriesEntryFirstChart);
            setSeriesIndexForSecondChartPercent(seriesEntrySecondChart);
        } else {
            setSeriesIndexForFirstChartValue(seriesEntryFirstChart);
            setSeriesIndexForSecondChartValue(seriesEntrySecondChart);
        }
    }

    public static void setSeriesIndexForFirstChartValue(final StepLineSeriesEntryBaseDTO stepLineSeriesEntry) {
        stepLineSeriesEntry.setXAxisIndex(0);
        stepLineSeriesEntry.setYAxisIndex(0);
    }

    public static void setSeriesIndexForFirstChartPercent(final StepLineSeriesEntryBaseDTO stepLineSeriesEntry) {
        stepLineSeriesEntry.setXAxisIndex(0);
        stepLineSeriesEntry.setYAxisIndex(1);
    }

    public static void setSeriesIndexForSecondChartValue(final StepLineSeriesEntryBaseDTO stepLineSeriesEntry) {
        stepLineSeriesEntry.setXAxisIndex(1);
        stepLineSeriesEntry.setYAxisIndex(2);
    }

    public static void setSeriesIndexForSecondChartPercent(final StepLineSeriesEntryBaseDTO stepLineSeriesEntry) {
        stepLineSeriesEntry.setXAxisIndex(1);
        stepLineSeriesEntry.setYAxisIndex(3);
    }

    /**
     * Diese Methode führt die Datenaufbereitung für das Stepline-Diagramm durch.
     * <p>
     * Sind in den options die Werte {@link Zeitblock#ZB_00_24} und
     * {@link Zaehldauer#DAUER_2_X_4_STUNDEN}
     * zu finden, so wird die Datenaufbereitung für zwei Unterdiagramme vorgenommen.
     * Ist diese Wertkombination nicht vorhanden, findet keine Aufteilung in zwei Unterdiagramme statt
     * und die Daten werden für ein Diagramm aufbereitet.
     * <p>
     * Falls keine Aufteilung in zwei Unterdiagrammme erforderlich ist, werden in der Klasse
     * {@link LadeZaehldatenSteplineDTO} neben den Variablen
     * {@link LadeZaehldatenSteplineDTO}#getLegend, {@link LadeZaehldatenSteplineDTO}#getRangeMax und
     * {@link LadeZaehldatenSteplineDTO}#getRangeMaxPercent nur die Variablen
     * {@link LadeZaehldatenSteplineDTO}#getXAxisDataFirstChart
     * sowie {@link LadeZaehldatenSteplineDTO}#getSeriesEntriesFirstChart gesetzt.
     * <p>
     * Ist eine Aufteilung notwendig, so werden auch die Variablen
     * {@link LadeZaehldatenSteplineDTO}#getXAxisDataSecondChart
     * sowie {@link LadeZaehldatenSteplineDTO}#getSeriesEntriesSecondChart gesetzt.
     *
     * @param zaehldatenTable Die Datengrundlage zur Aufbereitung des Stepline-Diagramms.
     * @param options Die durch den User im Frontend gewählten Optionen.
     * @return Die aufbreiteten Daten für das Stepline-Diagramm entsprechend der gewählten Optionen.
     */
    public LadeZaehldatenSteplineDTO ladeProcessedZaehldatenStepline(final LadeZaehldatenTableDTO zaehldatenTable,
            final OptionsDTO options) {
        final LadeZaehldatenSteplineDTO ladeZaehldatenStepline = new LadeZaehldatenSteplineDTO();
        ladeZaehldatenStepline.setRangeMax(0);
        ladeZaehldatenStepline.setRangeMaxPercent(0);
        ladeZaehldatenStepline.setLegend(new ArrayList<>());
        ladeZaehldatenStepline.setXAxisDataFirstChart(new ArrayList<>());
        ladeZaehldatenStepline.setSeriesEntriesFirstChart(new ArrayList<>());

        final SeriesEntries seriesEntries = new SeriesEntries();

        zaehldatenTable.getZaehldaten().stream()
                .filter(ladeZaehldatum -> ObjectUtils.isEmpty(ladeZaehldatum.getType()))
                .forEach(ladeZaehldatum -> {
                    if (options.getPersonenkraftwagen()) {
                        setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryPkw());
                        seriesEntries.getSeriesEntryPkw().getYAxisData().add(ladeZaehldatum.getPkw());
                        setLegendInZaehldatenStepline(ladeZaehldatenStepline, ChartLegendUtil.PKW);
                        setRangeMaxRoundedToHundredInZaehldatenStepline(ladeZaehldatenStepline, ladeZaehldatum.getPkw());
                    }
                    if (options.getLastkraftwagen()) {
                        setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryLkw());
                        seriesEntries.getSeriesEntryLkw().getYAxisData().add(ladeZaehldatum.getLkw());
                        setLegendInZaehldatenStepline(ladeZaehldatenStepline, ChartLegendUtil.LKW);
                        setRangeMaxRoundedToHundredInZaehldatenStepline(ladeZaehldatenStepline, ladeZaehldatum.getLkw());
                    }
                    if (options.getLastzuege()) {
                        setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryLz());
                        seriesEntries.getSeriesEntryLz().getYAxisData().add(ladeZaehldatum.getLastzuege());
                        setLegendInZaehldatenStepline(ladeZaehldatenStepline, ChartLegendUtil.LASTZUEGE);
                        setRangeMaxRoundedToHundredInZaehldatenStepline(ladeZaehldatenStepline, ladeZaehldatum.getLastzuege());
                    }
                    if (options.getBusse()) {
                        setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryBus());
                        seriesEntries.getSeriesEntryBus().getYAxisData().add(ladeZaehldatum.getBusse());
                        setLegendInZaehldatenStepline(ladeZaehldatenStepline, ChartLegendUtil.BUSSE);
                        setRangeMaxRoundedToHundredInZaehldatenStepline(ladeZaehldatenStepline, ladeZaehldatum.getBusse());
                    }
                    if (options.getKraftraeder()) {
                        setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryKrad());
                        seriesEntries.getSeriesEntryKrad().getYAxisData().add(ladeZaehldatum.getKraftraeder());
                        setLegendInZaehldatenStepline(ladeZaehldatenStepline, ChartLegendUtil.KRAFTRAEDER);
                        setRangeMaxRoundedToHundredInZaehldatenStepline(ladeZaehldatenStepline, ladeZaehldatum.getKraftraeder());
                    }
                    if (options.getRadverkehr()) {
                        setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryRad());
                        seriesEntries.getSeriesEntryRad().getYAxisData().add(ladeZaehldatum.getFahrradfahrer());
                        setLegendInZaehldatenStepline(ladeZaehldatenStepline, ChartLegendUtil.RAD);
                        setRangeMaxRoundedToHundredInZaehldatenStepline(ladeZaehldatenStepline, ladeZaehldatum.getFahrradfahrer());
                    }
                    if (options.getFussverkehr()) {
                        setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryFuß());
                        seriesEntries.getSeriesEntryFuß().getYAxisData().add(ladeZaehldatum.getFussgaenger());
                        setLegendInZaehldatenStepline(ladeZaehldatenStepline, ChartLegendUtil.FUSSGAENGER);
                        setRangeMaxRoundedToHundredInZaehldatenStepline(ladeZaehldatenStepline, ladeZaehldatum.getFussgaenger());
                    }
                    if (options.getKraftfahrzeugverkehr()) {
                        setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryKfz());
                        seriesEntries.getSeriesEntryKfz().getYAxisData().add(ladeZaehldatum.getKfz());
                        setLegendInZaehldatenStepline(ladeZaehldatenStepline, ChartLegendUtil.KFZ);
                        setRangeMaxRoundedToHundredInZaehldatenStepline(ladeZaehldatenStepline, ladeZaehldatum.getKfz());
                    }
                    if (options.getSchwerverkehr()) {
                        setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntrySv());
                        seriesEntries.getSeriesEntrySv().getYAxisData().add(ladeZaehldatum.getSchwerverkehr());
                        setLegendInZaehldatenStepline(ladeZaehldatenStepline, ChartLegendUtil.SCHWERVERKEHR);
                        setRangeMaxRoundedToHundredInZaehldatenStepline(ladeZaehldatenStepline, ladeZaehldatum.getSchwerverkehr());
                    }
                    if (options.getSchwerverkehrsanteilProzent()) {
                        setSeriesIndexForFirstChartPercent(seriesEntries.getSeriesEntrySvProzent());
                        seriesEntries.getSeriesEntrySvProzent().getYAxisData().add(ladeZaehldatum.getAnteilSchwerverkehrAnKfzProzent());
                        setLegendInZaehldatenStepline(ladeZaehldatenStepline, ChartLegendUtil.SCHWERVERKEHR_ANTEIL_PROZENT);
                        setRangeMaxPercentInZaehldatenStepline(ladeZaehldatenStepline, ladeZaehldatum.getAnteilSchwerverkehrAnKfzProzent());
                    }
                    if (options.getGueterverkehr()) {
                        setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryGv());
                        seriesEntries.getSeriesEntryGv().getYAxisData().add(ladeZaehldatum.getGueterverkehr());
                        setLegendInZaehldatenStepline(ladeZaehldatenStepline, ChartLegendUtil.GUETERVERKEHR);
                        setRangeMaxRoundedToHundredInZaehldatenStepline(ladeZaehldatenStepline, ladeZaehldatum.getGueterverkehr());
                    }
                    if (options.getGueterverkehrsanteilProzent()) {
                        setSeriesIndexForFirstChartPercent(seriesEntries.getSeriesEntryGvProzent());
                        seriesEntries.getSeriesEntryGvProzent().getYAxisData().add(ladeZaehldatum.getAnteilGueterverkehrAnKfzProzent());
                        setLegendInZaehldatenStepline(ladeZaehldatenStepline, ChartLegendUtil.GUETERVERKEHR_ANTEIL_PROZENT);
                        setRangeMaxPercentInZaehldatenStepline(ladeZaehldatenStepline, ladeZaehldatum.getAnteilGueterverkehrAnKfzProzent());
                    }
                    if (options.getPkwEinheiten()) {
                        setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryPkwEinheiten());
                        seriesEntries.getSeriesEntryPkwEinheiten().getYAxisData().add(ladeZaehldatum.getPkwEinheiten());
                        setLegendInZaehldatenStepline(ladeZaehldatenStepline, ChartLegendUtil.PKW_EINHEITEN);
                        setRangeMaxRoundedToHundredInZaehldatenStepline(ladeZaehldatenStepline, ladeZaehldatum.getPkwEinheiten());
                    }
                    ladeZaehldatenStepline.setXAxisDataFirstChart(
                            ZaehldatenProcessingUtil.checkAndAddToXAxisWhenNotAvailable(
                                    ladeZaehldatenStepline.getXAxisDataFirstChart(),
                                    ZaehldatenProcessingUtil.getStartUhrzeit(ladeZaehldatum)));
                });
        ladeZaehldatenStepline.setSeriesEntriesFirstChart(seriesEntries.getChosenStepLineSeriesEntries(options));
        splitSeriesEntriesIntoFirstChartAndSecondChartIfNecessaryInLadeZaehldatenStepline(ladeZaehldatenStepline, options);
        return ladeZaehldatenStepline;
    }

    /**
     * Innere Helfer-Klasse welche {@link StepLineSeriesEntryIntegerDTO} und
     * {@link StepLineSeriesEntryBigDecimalDTO}
     * nach Fahrzeugklasse und Fahrzeugkategorie aufgliedert und vorhält.
     */
    @Getter
    @Setter
    private static class SeriesEntries {

        private StepLineSeriesEntryIntegerDTO seriesEntryPkw;

        private StepLineSeriesEntryIntegerDTO seriesEntryLkw;

        private StepLineSeriesEntryIntegerDTO seriesEntryLz;

        private StepLineSeriesEntryIntegerDTO seriesEntryBus;

        private StepLineSeriesEntryIntegerDTO seriesEntryKrad;

        private StepLineSeriesEntryIntegerDTO seriesEntryRad;

        private StepLineSeriesEntryIntegerDTO seriesEntryFuß;

        private StepLineSeriesEntryBigDecimalDTO seriesEntryKfz;

        private StepLineSeriesEntryBigDecimalDTO seriesEntrySv;

        private StepLineSeriesEntryBigDecimalDTO seriesEntrySvProzent;

        private StepLineSeriesEntryBigDecimalDTO seriesEntryGv;

        private StepLineSeriesEntryBigDecimalDTO seriesEntryGvProzent;

        private StepLineSeriesEntryIntegerDTO seriesEntryPkwEinheiten;

        public SeriesEntries() {
            seriesEntryPkw = new StepLineSeriesEntryIntegerDTO();
            seriesEntryPkw.setName(ChartLegendUtil.PKW);
            seriesEntryLkw = new StepLineSeriesEntryIntegerDTO();
            seriesEntryLkw.setName(ChartLegendUtil.LKW);
            seriesEntryLz = new StepLineSeriesEntryIntegerDTO();
            seriesEntryLz.setName(ChartLegendUtil.LASTZUEGE);
            seriesEntryBus = new StepLineSeriesEntryIntegerDTO();
            seriesEntryBus.setName(ChartLegendUtil.BUSSE);
            seriesEntryKrad = new StepLineSeriesEntryIntegerDTO();
            seriesEntryKrad.setName(ChartLegendUtil.KRAFTRAEDER);
            seriesEntryRad = new StepLineSeriesEntryIntegerDTO();
            seriesEntryRad.setName(ChartLegendUtil.RAD);
            seriesEntryFuß = new StepLineSeriesEntryIntegerDTO();
            seriesEntryFuß.setName(ChartLegendUtil.FUSSGAENGER);
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
            seriesEntryPkwEinheiten = new StepLineSeriesEntryIntegerDTO();
            seriesEntryPkwEinheiten.setName(ChartLegendUtil.PKW_EINHEITEN);
        }

        private static void addSeriesToAllEntriesIfChosen(final List<StepLineSeriesEntryBaseDTO> allEntries,
                final StepLineSeriesEntryBaseDTO entry,
                final Boolean isChosen) {
            if (isChosen.booleanValue()) {
                allEntries.add(entry);
            }
        }

        /**
         * Gibt alle {@link StepLineSeriesEntryIntegerDTO} und {@link StepLineSeriesEntryBigDecimalDTO}
         * entsprechend der im Parameter options gewählten Fahrzeugklassen, Fahrzeugkategorien
         * und Prozentwerte als Liste zurück.
         *
         * @param options Das Objekt mit der Information bezüglich erwünschter oder nicht erwünschter
         *            Fahrzeugklassen, Fahrzeugkategorien oder Prozentwerte
         * @return Liste mit den erwünschten {@link StepLineSeriesEntryIntegerDTO}
         *         und {@link StepLineSeriesEntryBigDecimalDTO}.
         */
        public List<StepLineSeriesEntryBaseDTO> getChosenStepLineSeriesEntries(final OptionsDTO options) {
            final List<StepLineSeriesEntryBaseDTO> allEntries = new ArrayList<>();
            addSeriesToAllEntriesIfChosen(allEntries, seriesEntryPkw, options.getPersonenkraftwagen());
            addSeriesToAllEntriesIfChosen(allEntries, seriesEntryLkw, options.getLastkraftwagen());
            addSeriesToAllEntriesIfChosen(allEntries, seriesEntryLz, options.getLastzuege());
            addSeriesToAllEntriesIfChosen(allEntries, seriesEntryBus, options.getBusse());
            addSeriesToAllEntriesIfChosen(allEntries, seriesEntryKrad, options.getKraftraeder());
            addSeriesToAllEntriesIfChosen(allEntries, seriesEntryRad, options.getRadverkehr());
            addSeriesToAllEntriesIfChosen(allEntries, seriesEntryFuß, options.getFussverkehr());
            addSeriesToAllEntriesIfChosen(allEntries, seriesEntryKfz, options.getKraftfahrzeugverkehr());
            addSeriesToAllEntriesIfChosen(allEntries, seriesEntrySv, options.getSchwerverkehr());
            addSeriesToAllEntriesIfChosen(allEntries, seriesEntrySvProzent, options.getSchwerverkehrsanteilProzent());
            addSeriesToAllEntriesIfChosen(allEntries, seriesEntryGv, options.getGueterverkehr());
            addSeriesToAllEntriesIfChosen(allEntries, seriesEntryGvProzent, options.getGueterverkehrsanteilProzent());
            addSeriesToAllEntriesIfChosen(allEntries, seriesEntryPkwEinheiten, options.getPkwEinheiten());
            return allEntries;
        }

    }

}
