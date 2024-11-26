package de.muenchen.dave.util.messstelle;

import de.muenchen.dave.domain.dtos.laden.LadeZaehldatenSteplineDTO;
import de.muenchen.dave.domain.dtos.laden.StepLineSeriesEntryBaseDTO;
import de.muenchen.dave.domain.dtos.laden.StepLineSeriesEntryBigDecimalDTO;
import de.muenchen.dave.domain.dtos.laden.StepLineSeriesEntryIntegerDTO;
import de.muenchen.dave.domain.dtos.messstelle.FahrzeugOptionsDTO;
import de.muenchen.dave.util.ChartLegendUtil;
import de.muenchen.dave.util.ZaehldatenProcessingUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GanglinieUtil {

    public static final Integer ZERO = 0;

    public static final Integer ONE = 1;

    public static final Integer ROUNDING_VALUE = 20;

    public static final Integer ROUNDING_VALUE_PERCENT = 2;

    public static void setRangeMaxRoundedToTwentyInZaehldatenStepline(
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

    public static void setRangeMaxPercentRoundedToTwoInZaehldatenStepline(
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

    public static void setLegendInZaehldatenStepline(
            final LadeZaehldatenSteplineDTO ladeZaehldatenStepline,
            final String legendEntry) {
        ladeZaehldatenStepline.setLegend(
                ChartLegendUtil.checkAndAddToLegendWhenNotAvailable(
                        ladeZaehldatenStepline.getLegend(),
                        legendEntry));
    }

    public static void setSeriesIndexForFirstChartValue(final StepLineSeriesEntryBaseDTO stepLineSeriesEntry) {
        stepLineSeriesEntry.setXAxisIndex(ZERO);
        stepLineSeriesEntry.setYAxisIndex(ZERO);
    }

    public static void setSeriesIndexForFirstChartPercent(final StepLineSeriesEntryBaseDTO stepLineSeriesEntry) {
        stepLineSeriesEntry.setXAxisIndex(ZERO);
        stepLineSeriesEntry.setYAxisIndex(ONE);
    }

    public static Integer getIntValueIfNotNull(final BigDecimal value) {
        return value == null
                ? null
                : value.intValue();
    }

    public static LadeZaehldatenSteplineDTO getInitialZaehldatenStepline() {
        final var ladeZaehldatenStepline = new LadeZaehldatenSteplineDTO();
        ladeZaehldatenStepline.setRangeMax(0);
        ladeZaehldatenStepline.setRangeMaxPercent(0);
        ladeZaehldatenStepline.setLegend(new ArrayList<>());
        ladeZaehldatenStepline.setXAxisDataFirstChart(new ArrayList<>());
        ladeZaehldatenStepline.setSeriesEntriesFirstChart(new ArrayList<>());
        return ladeZaehldatenStepline;
    }

    /**
     * Helfer-Klasse welche {@link StepLineSeriesEntryIntegerDTO} und
     * {@link StepLineSeriesEntryBigDecimalDTO} nach Fahrzeugklasse und Fahrzeugkategorie
     * aufgliedert und vorhält.
     */
    @Getter
    @Setter
    public static class SeriesEntries {

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
