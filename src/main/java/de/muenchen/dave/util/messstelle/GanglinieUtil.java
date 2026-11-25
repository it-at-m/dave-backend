package de.muenchen.dave.util.messstelle;

import de.muenchen.dave.domain.dtos.laden.LadeZaehldatenStepLineDto;
import de.muenchen.dave.domain.dtos.laden.StepLineSeriesEntryBaseDto;
import de.muenchen.dave.domain.dtos.laden.StepLineSeriesEntryBigDecimalDto;
import de.muenchen.dave.domain.dtos.laden.StepLineSeriesEntryIntegerDto;
import de.muenchen.dave.domain.dtos.messstelle.FahrzeugOptionsDto;
import de.muenchen.dave.util.ChartLegendUtil;
import de.muenchen.dave.util.ZaehldatenProcessingUtil;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GanglinieUtil {

    public static final Integer ZERO = 0;

    public static final Integer ONE = 1;

    public static final Integer TWO = 2;

    public static final Integer THREE = 3;

    public static final Integer ROUNDING_VALUE = 20;

    public static final Integer ROUNDING_VALUE_PERCENT = 2;

    public static void setRangeMaxRoundedToTwentyInZaehldatenStepline(
            final LadeZaehldatenStepLineDto ladeZaehldatenStepline,
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
            final LadeZaehldatenStepLineDto ladeZaehldatenStepline,
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
            final LadeZaehldatenStepLineDto ladeZaehldatenStepline,
            final String legendEntry) {
        ladeZaehldatenStepline.setLegend(
                ChartLegendUtil.checkAndAddToLegendWhenNotAvailable(
                        ladeZaehldatenStepline.getLegend(),
                        legendEntry));
    }

    public static void setSeriesIndexForFirstChartValue(final StepLineSeriesEntryBaseDto stepLineSeriesEntry) {
        stepLineSeriesEntry.setXAxisIndex(ZERO);
        stepLineSeriesEntry.setYAxisIndex(ZERO);
    }

    public static void setSeriesIndexForFirstChartPercent(final StepLineSeriesEntryBaseDto stepLineSeriesEntry) {
        stepLineSeriesEntry.setXAxisIndex(ZERO);
        stepLineSeriesEntry.setYAxisIndex(ONE);
    }

    public static void setSeriesIndexForSecondChartValue(final StepLineSeriesEntryBaseDto stepLineSeriesEntry) {
        stepLineSeriesEntry.setXAxisIndex(ONE);
        stepLineSeriesEntry.setYAxisIndex(TWO);
    }

    public static void setSeriesIndexForSecondChartPercent(final StepLineSeriesEntryBaseDto stepLineSeriesEntry) {
        stepLineSeriesEntry.setXAxisIndex(ONE);
        stepLineSeriesEntry.setYAxisIndex(THREE);
    }

    public static Integer getIntValueIfNotNull(final BigDecimal value) {
        return value == null
                ? null
                : value.intValue();
    }

    public static LadeZaehldatenStepLineDto getInitialZaehldatenStepline() {
        final var ladeZaehldatenStepline = new LadeZaehldatenStepLineDto();
        ladeZaehldatenStepline.setRangeMax(0);
        ladeZaehldatenStepline.setRangeMaxPercent(0);
        ladeZaehldatenStepline.setLegend(new ArrayList<>());
        ladeZaehldatenStepline.setXAxisDataFirstChart(new ArrayList<>());
        ladeZaehldatenStepline.setSeriesEntriesFirstChart(new ArrayList<>());
        return ladeZaehldatenStepline;
    }

    /**
     * Helfer-Klasse welche {@link StepLineSeriesEntryIntegerDto} und
     * {@link StepLineSeriesEntryBigDecimalDto} nach Fahrzeugklasse und Fahrzeugkategorie
     * aufgliedert und vorhält.
     */
    @Getter
    @Setter
    public static class SeriesEntries {

        private StepLineSeriesEntryIntegerDto seriesEntryPkw;

        private StepLineSeriesEntryIntegerDto seriesEntryLkw;

        private StepLineSeriesEntryIntegerDto seriesEntryLfw;

        private StepLineSeriesEntryIntegerDto seriesEntryLz;

        private StepLineSeriesEntryIntegerDto seriesEntryBus;

        private StepLineSeriesEntryIntegerDto seriesEntryKrad;

        private StepLineSeriesEntryIntegerDto seriesEntryRad;

        private StepLineSeriesEntryIntegerDto seriesEntryKfz;

        private StepLineSeriesEntryIntegerDto seriesEntrySv;

        private StepLineSeriesEntryBigDecimalDto seriesEntrySvProzent;

        private StepLineSeriesEntryIntegerDto seriesEntryGv;

        private StepLineSeriesEntryBigDecimalDto seriesEntryGvProzent;

        public SeriesEntries() {
            seriesEntryPkw = new StepLineSeriesEntryIntegerDto();
            seriesEntryPkw.setName(ChartLegendUtil.PKW);
            seriesEntryLkw = new StepLineSeriesEntryIntegerDto();
            seriesEntryLkw.setName(ChartLegendUtil.LKW);
            seriesEntryLfw = new StepLineSeriesEntryIntegerDto();
            seriesEntryLfw.setName(ChartLegendUtil.LFW);
            seriesEntryLz = new StepLineSeriesEntryIntegerDto();
            seriesEntryLz.setName(ChartLegendUtil.LASTZUEGE);
            seriesEntryBus = new StepLineSeriesEntryIntegerDto();
            seriesEntryBus.setName(ChartLegendUtil.BUSSE);
            seriesEntryKrad = new StepLineSeriesEntryIntegerDto();
            seriesEntryKrad.setName(ChartLegendUtil.KRAFTRAEDER);
            seriesEntryRad = new StepLineSeriesEntryIntegerDto();
            seriesEntryRad.setName(ChartLegendUtil.RAD);
            seriesEntryKfz = new StepLineSeriesEntryIntegerDto();
            seriesEntryKfz.setName(ChartLegendUtil.KFZ);
            seriesEntrySv = new StepLineSeriesEntryIntegerDto();
            seriesEntrySv.setName(ChartLegendUtil.SCHWERVERKEHR);
            seriesEntrySvProzent = new StepLineSeriesEntryBigDecimalDto();
            seriesEntrySvProzent.setName(ChartLegendUtil.SCHWERVERKEHR_ANTEIL_PROZENT);
            seriesEntryGv = new StepLineSeriesEntryIntegerDto();
            seriesEntryGv.setName(ChartLegendUtil.GUETERVERKEHR);
            seriesEntryGvProzent = new StepLineSeriesEntryBigDecimalDto();
            seriesEntryGvProzent.setName(ChartLegendUtil.GUETERVERKEHR_ANTEIL_PROZENT);
        }

        private static void addSeriesToAllEntriesIfChosen(
                final List<StepLineSeriesEntryBaseDto> allEntries,
                final StepLineSeriesEntryBaseDto entry,
                final Boolean isChosen) {
            if (isChosen) {
                allEntries.add(entry);
            }
        }

        /**
         * Gibt alle {@link StepLineSeriesEntryIntegerDto} und {@link StepLineSeriesEntryBigDecimalDto}
         * entsprechend der im Parameter options gewählten
         * Fahrzeugklassen, Fahrzeugkategorien und Prozentwerte als Liste zurück.
         *
         * @return Liste mit den erwünschten {@link StepLineSeriesEntryIntegerDto} und
         *         {@link StepLineSeriesEntryBigDecimalDto}.
         */
        public List<StepLineSeriesEntryBaseDto> getChosenStepLineSeriesEntries(final FahrzeugOptionsDto options) {
            final List<StepLineSeriesEntryBaseDto> allEntries = new ArrayList<>();
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
