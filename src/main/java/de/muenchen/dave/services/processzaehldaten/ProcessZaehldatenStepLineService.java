package de.muenchen.dave.services.processzaehldaten;

import de.muenchen.dave.domain.dtos.OptionsDto;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatenStepLineDto;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatenTableDto;
import de.muenchen.dave.domain.dtos.laden.StepLineSeriesEntryBaseDto;
import de.muenchen.dave.domain.dtos.laden.StepLineSeriesEntryBigDecimalDto;
import de.muenchen.dave.domain.dtos.laden.StepLineSeriesEntryIntegerDto;
import de.muenchen.dave.domain.enums.Zaehldauer;
import de.muenchen.dave.domain.enums.Zeitblock;
import de.muenchen.dave.services.ladezaehldaten.LadeZaehldatenService;
import de.muenchen.dave.util.ChartLegendUtil;
import de.muenchen.dave.util.ZaehldatenProcessingUtil;
import de.muenchen.dave.util.messstelle.GanglinieUtil;
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
public class ProcessZaehldatenStepLineService {

    // Refactoring: Synergieeffekte mit GanglinienService nutzen

    private static final Integer ROUNDING_VALUE = 20;

    private static final Integer ROUNDING_VALUE_PERCENT = 2;

    public static void setRangeMaxRoundedInZaehldatenStepline(
            final LadeZaehldatenStepLineDto ladeZaehldatenStepline,
            final Integer value) {
        ladeZaehldatenStepline.setRangeMax(
                ZaehldatenProcessingUtil.getValueRounded(
                        Math.max(
                                ZaehldatenProcessingUtil.getZeroIfNull(value),
                                ladeZaehldatenStepline.getRangeMax()),
                        ROUNDING_VALUE));
    }

    public static void setRangeMaxRoundedInZaehldatenStepline(
            final LadeZaehldatenStepLineDto ladeZaehldatenStepline,
            final BigDecimal value) {
        setRangeMaxRoundedInZaehldatenStepline(
                ladeZaehldatenStepline,
                ZaehldatenProcessingUtil.getZeroIfNull(value).intValue());
    }

    public static void setRangeMaxPercentInZaehldatenStepline(
            final LadeZaehldatenStepLineDto ladeZaehldatenStepline,
            final BigDecimal value) {
        final int currentValue = ladeZaehldatenStepline.getRangeMaxPercent();
        ladeZaehldatenStepline.setRangeMaxPercent(
                ZaehldatenProcessingUtil.getValueRounded(
                        BigDecimal.valueOf(currentValue)
                                .max(ZaehldatenProcessingUtil.getZeroIfNull(value)),
                        ROUNDING_VALUE_PERCENT));
    }

    /**
     * Falls sich in den options die Werte {@link Zeitblock#ZB_00_24} und
     * {@link Zaehldauer#DAUER_2_X_4_STUNDEN} befinden, wird das Diagramm in zwei
     * Unterdiagramme aufgeteilt. Die Aufteilung der Daten für die beiden Unterdiagramme wird in der
     * mitte der X-Achse des Gesamtdiagramms vorgenommen.
     *
     * @param ladeZaehldatenStepline Die für ein Diagramm aufbereitete Daten. Die Unterteilung in
     *            Unterdiagramme ist noch nicht durchgeführt.
     * @param options Die {@link OptionsDto} zur Prüfung auf {@link Zeitblock#ZB_00_24} und
     *            {@link Zaehldauer#DAUER_2_X_4_STUNDEN}.
     */
    public static void splitSeriesEntriesIntoFirstChartAndSecondChartIfNecessaryInLadeZaehldatenStepline(
            final LadeZaehldatenStepLineDto ladeZaehldatenStepline,
            final OptionsDto options) {
        if (options.getZeitblock().equals(Zeitblock.ZB_00_24)
                && options.getZaehldauer().equals(Zaehldauer.DAUER_2_X_4_STUNDEN)
                && !(StringUtils.equals(options.getZeitauswahl(), LadeZaehldatenService.ZEITAUSWAHL_SPITZENSTUNDE_KFZ)
                        || StringUtils.equals(options.getZeitauswahl(), LadeZaehldatenService.ZEITAUSWAHL_SPITZENSTUNDE_RAD)
                        || StringUtils.equals(options.getZeitauswahl(), LadeZaehldatenService.ZEITAUSWAHL_SPITZENSTUNDE_FUSS))) {
            final List<StepLineSeriesEntryBaseDto> newSeriesEntriesFirstChart = new ArrayList<>();
            final List<StepLineSeriesEntryBaseDto> newSeriesEntriesSecondChart = new ArrayList<>();
            final int splittedSize = ladeZaehldatenStepline.getXAxisDataFirstChart().size() / 2;

            // Split X axis data
            if (ladeZaehldatenStepline.getXAxisDataFirstChart().size() < 2) {
                return;
            }
            List<List<String>> splittetXAxisData = ListUtils.partition(
                    ladeZaehldatenStepline.getXAxisDataFirstChart(),
                    splittedSize);
            ladeZaehldatenStepline.setXAxisDataFirstChart(new ArrayList<>(splittetXAxisData.get(0)));
            ladeZaehldatenStepline.setXAxisDataSecondChart(new ArrayList<>(splittetXAxisData.get(1)));

            // Split StepLineSeriesEntries (Y axis data)
            ladeZaehldatenStepline.getSeriesEntriesFirstChart().forEach(serieEntryFirstChart -> {
                if (serieEntryFirstChart.getClass().equals(StepLineSeriesEntryIntegerDto.class)) {
                    List<List<Integer>> splittedYAxisData = ListUtils.partition(
                            ((StepLineSeriesEntryIntegerDto) serieEntryFirstChart).getYAxisData(),
                            splittedSize);
                    final StepLineSeriesEntryIntegerDto stepLineSeriesEntryFirstChart = new StepLineSeriesEntryIntegerDto();
                    stepLineSeriesEntryFirstChart.setName(serieEntryFirstChart.getName());
                    stepLineSeriesEntryFirstChart.setYAxisData(new ArrayList<>(splittedYAxisData.get(0)));
                    newSeriesEntriesFirstChart.add(stepLineSeriesEntryFirstChart);
                    final StepLineSeriesEntryIntegerDto stepLineSeriesEntrySecondChart = new StepLineSeriesEntryIntegerDto();
                    stepLineSeriesEntrySecondChart.setName(serieEntryFirstChart.getName());
                    stepLineSeriesEntrySecondChart.setYAxisData(new ArrayList<>(splittedYAxisData.get(1)));
                    newSeriesEntriesSecondChart.add(stepLineSeriesEntrySecondChart);
                    setSeriesIndexForChart(serieEntryFirstChart.getName(),
                            stepLineSeriesEntryFirstChart,
                            stepLineSeriesEntrySecondChart);
                } else {
                    List<List<BigDecimal>> splittedYAxisData = ListUtils.partition(
                            ((StepLineSeriesEntryBigDecimalDto) serieEntryFirstChart).getYAxisData(),
                            splittedSize);
                    final StepLineSeriesEntryBigDecimalDto stepLineSeriesEntryFirstChart = new StepLineSeriesEntryBigDecimalDto();
                    stepLineSeriesEntryFirstChart.setName(serieEntryFirstChart.getName());
                    stepLineSeriesEntryFirstChart.setYAxisData(new ArrayList<>(splittedYAxisData.get(0)));
                    newSeriesEntriesFirstChart.add(stepLineSeriesEntryFirstChart);
                    final StepLineSeriesEntryBigDecimalDto stepLineSeriesEntrySecondChart = new StepLineSeriesEntryBigDecimalDto();
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

    public static void setSeriesIndexForChart(
            final String fahrzeugkategorie,
            final StepLineSeriesEntryBaseDto seriesEntryFirstChart,
            final StepLineSeriesEntryBaseDto seriesEntrySecondChart) {
        if (ChartLegendUtil.SCHWERVERKEHR_ANTEIL_PROZENT.equals(fahrzeugkategorie)
                || ChartLegendUtil.GUETERVERKEHR_ANTEIL_PROZENT.equals(fahrzeugkategorie)) {
            GanglinieUtil.setSeriesIndexForFirstChartPercent(seriesEntryFirstChart);
            GanglinieUtil.setSeriesIndexForSecondChartPercent(seriesEntrySecondChart);
        } else {
            GanglinieUtil.setSeriesIndexForFirstChartValue(seriesEntryFirstChart);
            GanglinieUtil.setSeriesIndexForSecondChartValue(seriesEntrySecondChart);
        }
    }

    /**
     * Diese Methode führt die Datenaufbereitung für das Stepline-Diagramm durch.
     * <p>
     * Sind in den options die Werte {@link Zeitblock#ZB_00_24} und
     * {@link Zaehldauer#DAUER_2_X_4_STUNDEN} zu finden, so wird die Datenaufbereitung für zwei
     * Unterdiagramme vorgenommen. Ist diese Wertkombination nicht vorhanden, findet keine Aufteilung in
     * zwei Unterdiagramme statt und die Daten werden für ein
     * Diagramm aufbereitet.
     * <p>
     * Falls keine Aufteilung in zwei Unterdiagrammme erforderlich ist, werden in der Klasse
     * {@link LadeZaehldatenStepLineDto} neben den Variablen
     * {@link LadeZaehldatenStepLineDto}#getLegend, {@link LadeZaehldatenStepLineDto}#getRangeMax und
     * {@link LadeZaehldatenStepLineDto}#getRangeMaxPercent nur
     * die Variablen {@link LadeZaehldatenStepLineDto}#getXAxisDataFirstChart sowie
     * {@link LadeZaehldatenStepLineDto}#getSeriesEntriesFirstChart gesetzt.
     * <p>
     * Ist eine Aufteilung notwendig, so werden auch die Variablen
     * {@link LadeZaehldatenStepLineDto}#getXAxisDataSecondChart sowie
     * {@link LadeZaehldatenStepLineDto}#getSeriesEntriesSecondChart gesetzt.
     *
     * @param zaehldatenTable Die Datengrundlage zur Aufbereitung des Stepline-Diagramms.
     * @param options Die durch den User im Frontend gewählten Optionen.
     * @return Die aufbreiteten Daten für das Stepline-Diagramm entsprechend der gewählten Optionen.
     */
    public LadeZaehldatenStepLineDto ladeProcessedZaehldatenStepline(
            final LadeZaehldatenTableDto zaehldatenTable,
            final OptionsDto options) {
        final var zaehldatenStepline = GanglinieUtil.getInitialZaehldatenStepline();

        final var seriesEntries = new SeriesEntries();

        zaehldatenTable.getZaehldaten().stream()
                .filter(ladeZaehldatum -> ObjectUtils.isEmpty(ladeZaehldatum.getType()))
                .forEach(ladeZaehldatum -> {
                    if (options.getPersonenkraftwagen()) {
                        GanglinieUtil.setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryPkw());
                        seriesEntries.getSeriesEntryPkw().getYAxisData().add(ladeZaehldatum.getPkw());
                        GanglinieUtil.setLegendInZaehldatenStepline(zaehldatenStepline, ChartLegendUtil.PKW);
                        setRangeMaxRoundedInZaehldatenStepline(zaehldatenStepline, ladeZaehldatum.getPkw());
                    }
                    if (options.getLastkraftwagen()) {
                        GanglinieUtil.setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryLkw());
                        seriesEntries.getSeriesEntryLkw().getYAxisData().add(ladeZaehldatum.getLkw());
                        GanglinieUtil.setLegendInZaehldatenStepline(zaehldatenStepline, ChartLegendUtil.LKW);
                        setRangeMaxRoundedInZaehldatenStepline(zaehldatenStepline, ladeZaehldatum.getLkw());
                    }
                    if (options.getLastzuege()) {
                        GanglinieUtil.setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryLz());
                        seriesEntries.getSeriesEntryLz().getYAxisData().add(ladeZaehldatum.getLastzuege());
                        GanglinieUtil.setLegendInZaehldatenStepline(zaehldatenStepline, ChartLegendUtil.LASTZUEGE);
                        setRangeMaxRoundedInZaehldatenStepline(zaehldatenStepline, ladeZaehldatum.getLastzuege());
                    }
                    if (options.getBusse()) {
                        GanglinieUtil.setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryBus());
                        seriesEntries.getSeriesEntryBus().getYAxisData().add(ladeZaehldatum.getBusse());
                        GanglinieUtil.setLegendInZaehldatenStepline(zaehldatenStepline, ChartLegendUtil.BUSSE);
                        setRangeMaxRoundedInZaehldatenStepline(zaehldatenStepline, ladeZaehldatum.getBusse());
                    }
                    if (options.getKraftraeder()) {
                        GanglinieUtil.setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryKrad());
                        seriesEntries.getSeriesEntryKrad().getYAxisData().add(ladeZaehldatum.getKraftraeder());
                        GanglinieUtil.setLegendInZaehldatenStepline(zaehldatenStepline, ChartLegendUtil.KRAFTRAEDER);
                        setRangeMaxRoundedInZaehldatenStepline(zaehldatenStepline, ladeZaehldatum.getKraftraeder());
                    }
                    if (options.getRadverkehr()) {
                        GanglinieUtil.setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryRad());
                        seriesEntries.getSeriesEntryRad().getYAxisData().add(ladeZaehldatum.getFahrradfahrer());
                        GanglinieUtil.setLegendInZaehldatenStepline(zaehldatenStepline, ChartLegendUtil.RAD);
                        setRangeMaxRoundedInZaehldatenStepline(zaehldatenStepline, ladeZaehldatum.getFahrradfahrer());
                    }
                    if (options.getFussverkehr()) {
                        GanglinieUtil.setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryFuß());
                        seriesEntries.getSeriesEntryFuß().getYAxisData().add(ladeZaehldatum.getFussgaenger());
                        GanglinieUtil.setLegendInZaehldatenStepline(zaehldatenStepline, ChartLegendUtil.FUSSGAENGER);
                        setRangeMaxRoundedInZaehldatenStepline(zaehldatenStepline, ladeZaehldatum.getFussgaenger());
                    }
                    if (options.getKraftfahrzeugverkehr()) {
                        GanglinieUtil.setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryKfz());
                        seriesEntries.getSeriesEntryKfz().getYAxisData().add(ladeZaehldatum.getKfz());
                        GanglinieUtil.setLegendInZaehldatenStepline(zaehldatenStepline, ChartLegendUtil.KFZ);
                        setRangeMaxRoundedInZaehldatenStepline(zaehldatenStepline, ladeZaehldatum.getKfz());
                    }
                    if (options.getSchwerverkehr()) {
                        GanglinieUtil.setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntrySv());
                        seriesEntries.getSeriesEntrySv().getYAxisData().add(ladeZaehldatum.getSchwerverkehr());
                        GanglinieUtil.setLegendInZaehldatenStepline(zaehldatenStepline, ChartLegendUtil.SCHWERVERKEHR);
                        setRangeMaxRoundedInZaehldatenStepline(zaehldatenStepline, ladeZaehldatum.getSchwerverkehr());
                    }
                    if (options.getSchwerverkehrsanteilProzent()) {
                        GanglinieUtil.setSeriesIndexForFirstChartPercent(seriesEntries.getSeriesEntrySvProzent());
                        seriesEntries.getSeriesEntrySvProzent().getYAxisData().add(ladeZaehldatum.getAnteilSchwerverkehrAnKfzProzent());
                        GanglinieUtil.setLegendInZaehldatenStepline(zaehldatenStepline, ChartLegendUtil.SCHWERVERKEHR_ANTEIL_PROZENT);
                        setRangeMaxPercentInZaehldatenStepline(zaehldatenStepline, ladeZaehldatum.getAnteilSchwerverkehrAnKfzProzent());
                    }
                    if (options.getGueterverkehr()) {
                        GanglinieUtil.setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryGv());
                        seriesEntries.getSeriesEntryGv().getYAxisData().add(ladeZaehldatum.getGueterverkehr());
                        GanglinieUtil.setLegendInZaehldatenStepline(zaehldatenStepline, ChartLegendUtil.GUETERVERKEHR);
                        setRangeMaxRoundedInZaehldatenStepline(zaehldatenStepline, ladeZaehldatum.getGueterverkehr());
                    }
                    if (options.getGueterverkehrsanteilProzent()) {
                        GanglinieUtil.setSeriesIndexForFirstChartPercent(seriesEntries.getSeriesEntryGvProzent());
                        seriesEntries.getSeriesEntryGvProzent().getYAxisData().add(ladeZaehldatum.getAnteilGueterverkehrAnKfzProzent());
                        GanglinieUtil.setLegendInZaehldatenStepline(zaehldatenStepline, ChartLegendUtil.GUETERVERKEHR_ANTEIL_PROZENT);
                        setRangeMaxPercentInZaehldatenStepline(zaehldatenStepline, ladeZaehldatum.getAnteilGueterverkehrAnKfzProzent());
                    }
                    if (options.getPkwEinheiten()) {
                        GanglinieUtil.setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryPkwEinheiten());
                        seriesEntries.getSeriesEntryPkwEinheiten().getYAxisData().add(ladeZaehldatum.getPkwEinheiten());
                        GanglinieUtil.setLegendInZaehldatenStepline(zaehldatenStepline, ChartLegendUtil.PKW_EINHEITEN);
                        setRangeMaxRoundedInZaehldatenStepline(zaehldatenStepline, ladeZaehldatum.getPkwEinheiten());
                    }
                    zaehldatenStepline.setXAxisDataFirstChart(
                            ZaehldatenProcessingUtil.checkAndAddToXAxisWhenNotAvailable(
                                    zaehldatenStepline.getXAxisDataFirstChart(),
                                    ZaehldatenProcessingUtil.getStartUhrzeit(ladeZaehldatum)));
                });

        zaehldatenStepline.setSeriesEntriesFirstChart(seriesEntries.getChosenStepLineSeriesEntries(options));
        splitSeriesEntriesIntoFirstChartAndSecondChartIfNecessaryInLadeZaehldatenStepline(zaehldatenStepline, options);
        return zaehldatenStepline;
    }

    /**
     * Innere Helfer-Klasse welche {@link StepLineSeriesEntryIntegerDto} und
     * {@link StepLineSeriesEntryBigDecimalDto} nach Fahrzeugklasse und Fahrzeugkategorie
     * aufgliedert und vorhält.
     */
    @Getter
    @Setter
    private static class SeriesEntries {

        private StepLineSeriesEntryIntegerDto seriesEntryPkw;

        private StepLineSeriesEntryIntegerDto seriesEntryLkw;

        private StepLineSeriesEntryIntegerDto seriesEntryLz;

        private StepLineSeriesEntryIntegerDto seriesEntryBus;

        private StepLineSeriesEntryIntegerDto seriesEntryKrad;

        private StepLineSeriesEntryIntegerDto seriesEntryRad;

        private StepLineSeriesEntryIntegerDto seriesEntryFuß;

        private StepLineSeriesEntryBigDecimalDto seriesEntryKfz;

        private StepLineSeriesEntryBigDecimalDto seriesEntrySv;

        private StepLineSeriesEntryBigDecimalDto seriesEntrySvProzent;

        private StepLineSeriesEntryBigDecimalDto seriesEntryGv;

        private StepLineSeriesEntryBigDecimalDto seriesEntryGvProzent;

        private StepLineSeriesEntryIntegerDto seriesEntryPkwEinheiten;

        public SeriesEntries() {
            seriesEntryPkw = new StepLineSeriesEntryIntegerDto();
            seriesEntryPkw.setName(ChartLegendUtil.PKW);
            seriesEntryLkw = new StepLineSeriesEntryIntegerDto();
            seriesEntryLkw.setName(ChartLegendUtil.LKW);
            seriesEntryLz = new StepLineSeriesEntryIntegerDto();
            seriesEntryLz.setName(ChartLegendUtil.LASTZUEGE);
            seriesEntryBus = new StepLineSeriesEntryIntegerDto();
            seriesEntryBus.setName(ChartLegendUtil.BUSSE);
            seriesEntryKrad = new StepLineSeriesEntryIntegerDto();
            seriesEntryKrad.setName(ChartLegendUtil.KRAFTRAEDER);
            seriesEntryRad = new StepLineSeriesEntryIntegerDto();
            seriesEntryRad.setName(ChartLegendUtil.RAD);
            seriesEntryFuß = new StepLineSeriesEntryIntegerDto();
            seriesEntryFuß.setName(ChartLegendUtil.FUSSGAENGER);
            seriesEntryKfz = new StepLineSeriesEntryBigDecimalDto();
            seriesEntryKfz.setName(ChartLegendUtil.KFZ);
            seriesEntrySv = new StepLineSeriesEntryBigDecimalDto();
            seriesEntrySv.setName(ChartLegendUtil.SCHWERVERKEHR);
            seriesEntrySvProzent = new StepLineSeriesEntryBigDecimalDto();
            seriesEntrySvProzent.setName(ChartLegendUtil.SCHWERVERKEHR_ANTEIL_PROZENT);
            seriesEntryGv = new StepLineSeriesEntryBigDecimalDto();
            seriesEntryGv.setName(ChartLegendUtil.GUETERVERKEHR);
            seriesEntryGvProzent = new StepLineSeriesEntryBigDecimalDto();
            seriesEntryGvProzent.setName(ChartLegendUtil.GUETERVERKEHR_ANTEIL_PROZENT);
            seriesEntryPkwEinheiten = new StepLineSeriesEntryIntegerDto();
            seriesEntryPkwEinheiten.setName(ChartLegendUtil.PKW_EINHEITEN);
        }

        private static void addSeriesToAllEntriesIfChosen(final List<StepLineSeriesEntryBaseDto> allEntries,
                final StepLineSeriesEntryBaseDto entry,
                final Boolean isChosen) {
            if (isChosen.booleanValue()) {
                allEntries.add(entry);
            }
        }

        /**
         * Gibt alle {@link StepLineSeriesEntryIntegerDto} und {@link StepLineSeriesEntryBigDecimalDto}
         * entsprechend der im Parameter options gewählten
         * Fahrzeugklassen, Fahrzeugkategorien und Prozentwerte als Liste zurück.
         *
         * @param options Das Objekt mit der Information bezüglich erwünschter oder nicht erwünschter
         *            Fahrzeugklassen, Fahrzeugkategorien oder Prozentwerte
         * @return Liste mit den erwünschten {@link StepLineSeriesEntryIntegerDto} und
         *         {@link StepLineSeriesEntryBigDecimalDto}.
         */
        public List<StepLineSeriesEntryBaseDto> getChosenStepLineSeriesEntries(final OptionsDto options) {
            final List<StepLineSeriesEntryBaseDto> allEntries = new ArrayList<>();
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
