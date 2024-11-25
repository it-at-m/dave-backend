package de.muenchen.dave.services.messstelle.auswertung;

import de.muenchen.dave.domain.dtos.laden.LadeZaehldatenSteplineDTO;
import de.muenchen.dave.domain.dtos.laden.StepLineSeriesEntryIntegerDTO;
import de.muenchen.dave.domain.dtos.messstelle.FahrzeugOptionsDTO;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.AuswertungMessstelle;
import de.muenchen.dave.services.messstelle.Zeitraum;
import de.muenchen.dave.util.ChartLegendUtil;
import de.muenchen.dave.util.ZaehldatenProcessingUtil;
import de.muenchen.dave.util.messstelle.GanglinieUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class GanglinieGesamtauswertungService {

    private static final DateTimeFormatter YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern("MM.yyyy");

    public LadeZaehldatenSteplineDTO ladeGanglinieForSingleMessstelle(
            final AuswertungMessstelle auswertungMessstelle,
            final FahrzeugOptionsDTO fahrzeugOptions) {
        log.debug("#ladeGanglinieForSingleMessstelle");

        final var auswertungenProZeitraum = CollectionUtils.emptyIfNull(auswertungMessstelle.getAuswertungenProZeitraum());
        final var zaehldatenStepline = this.getInitialZaehldatenStepline();
        final var seriesEntries = new GanglinieUtil.SeriesEntries();

        auswertungenProZeitraum.forEach(auswertung -> {
            if (fahrzeugOptions.isPersonenkraftwagen()) {
                GanglinieUtil.setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryPkw());
                seriesEntries.getSeriesEntryPkw().getYAxisData().add(GanglinieUtil.getIntValueIfNotNull(auswertung.getDaten().getSummeAllePkw()));
                GanglinieUtil.setLegendInZaehldatenStepline(zaehldatenStepline, ChartLegendUtil.PKW);
                GanglinieUtil.setRangeMaxRoundedToTwentyInZaehldatenStepline(zaehldatenStepline,
                        GanglinieUtil.getIntValueIfNotNull(auswertung.getDaten().getSummeAllePkw()));
            }
            if (fahrzeugOptions.isLastkraftwagen()) {
                GanglinieUtil.setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryLkw());
                seriesEntries.getSeriesEntryLkw().getYAxisData().add(GanglinieUtil.getIntValueIfNotNull(auswertung.getDaten().getAnzahlLkw()));
                GanglinieUtil.setLegendInZaehldatenStepline(zaehldatenStepline, ChartLegendUtil.LKW);
                GanglinieUtil.setRangeMaxRoundedToTwentyInZaehldatenStepline(zaehldatenStepline,
                        GanglinieUtil.getIntValueIfNotNull(auswertung.getDaten().getAnzahlLkw()));
            }
            if (fahrzeugOptions.isLastzuege()) {
                GanglinieUtil.setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryLz());
                seriesEntries.getSeriesEntryLz().getYAxisData().add(GanglinieUtil.getIntValueIfNotNull(auswertung.getDaten().getSummeLastzug()));
                GanglinieUtil.setLegendInZaehldatenStepline(zaehldatenStepline, ChartLegendUtil.LASTZUEGE);
                GanglinieUtil.setRangeMaxRoundedToTwentyInZaehldatenStepline(zaehldatenStepline,
                        GanglinieUtil.getIntValueIfNotNull(auswertung.getDaten().getSummeLastzug()));
            }
            if (fahrzeugOptions.isLieferwagen()) {
                GanglinieUtil.setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryLfw());
                seriesEntries.getSeriesEntryLfw().getYAxisData().add(GanglinieUtil.getIntValueIfNotNull(auswertung.getDaten().getAnzahlLfw()));
                GanglinieUtil.setLegendInZaehldatenStepline(zaehldatenStepline, ChartLegendUtil.LFW);
                GanglinieUtil.setRangeMaxRoundedToTwentyInZaehldatenStepline(zaehldatenStepline,
                        GanglinieUtil.getIntValueIfNotNull(auswertung.getDaten().getAnzahlLfw()));
            }
            if (fahrzeugOptions.isBusse()) {
                GanglinieUtil.setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryBus());
                seriesEntries.getSeriesEntryBus().getYAxisData().add(GanglinieUtil.getIntValueIfNotNull(auswertung.getDaten().getAnzahlBus()));
                GanglinieUtil.setLegendInZaehldatenStepline(zaehldatenStepline, ChartLegendUtil.BUSSE);
                GanglinieUtil.setRangeMaxRoundedToTwentyInZaehldatenStepline(zaehldatenStepline,
                        GanglinieUtil.getIntValueIfNotNull(auswertung.getDaten().getAnzahlBus()));
            }
            if (fahrzeugOptions.isKraftraeder()) {
                GanglinieUtil.setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryKrad());
                seriesEntries.getSeriesEntryKrad().getYAxisData().add(GanglinieUtil.getIntValueIfNotNull(auswertung.getDaten().getAnzahlKrad()));
                GanglinieUtil.setLegendInZaehldatenStepline(zaehldatenStepline, ChartLegendUtil.KRAFTRAEDER);
                GanglinieUtil.setRangeMaxRoundedToTwentyInZaehldatenStepline(zaehldatenStepline,
                        GanglinieUtil.getIntValueIfNotNull(auswertung.getDaten().getAnzahlKrad()));
            }
            if (fahrzeugOptions.isRadverkehr()) {
                GanglinieUtil.setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryRad());
                seriesEntries.getSeriesEntryRad().getYAxisData().add(GanglinieUtil.getIntValueIfNotNull(auswertung.getDaten().getAnzahlRad()));
                GanglinieUtil.setLegendInZaehldatenStepline(zaehldatenStepline, ChartLegendUtil.RAD);
                GanglinieUtil.setRangeMaxRoundedToTwentyInZaehldatenStepline(zaehldatenStepline,
                        GanglinieUtil.getIntValueIfNotNull(auswertung.getDaten().getAnzahlRad()));
            }
            if (fahrzeugOptions.isKraftfahrzeugverkehr()) {
                GanglinieUtil.setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryKfz());
                seriesEntries.getSeriesEntryKfz().getYAxisData().add(GanglinieUtil.getIntValueIfNotNull(auswertung.getDaten().getSummeKraftfahrzeugverkehr()));
                GanglinieUtil.setLegendInZaehldatenStepline(zaehldatenStepline, ChartLegendUtil.KFZ);
                GanglinieUtil.setRangeMaxRoundedToTwentyInZaehldatenStepline(zaehldatenStepline,
                        GanglinieUtil.getIntValueIfNotNull(auswertung.getDaten().getSummeKraftfahrzeugverkehr()));
            }
            if (fahrzeugOptions.isSchwerverkehr()) {
                GanglinieUtil.setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntrySv());
                seriesEntries.getSeriesEntrySv().getYAxisData().add(GanglinieUtil.getIntValueIfNotNull(auswertung.getDaten().getSummeSchwerverkehr()));
                GanglinieUtil.setLegendInZaehldatenStepline(zaehldatenStepline, ChartLegendUtil.SCHWERVERKEHR);
                GanglinieUtil.setRangeMaxRoundedToTwentyInZaehldatenStepline(zaehldatenStepline,
                        GanglinieUtil.getIntValueIfNotNull(auswertung.getDaten().getSummeSchwerverkehr()));
            }
            if (fahrzeugOptions.isSchwerverkehrsanteilProzent()) {
                GanglinieUtil.setSeriesIndexForFirstChartPercent(seriesEntries.getSeriesEntrySvProzent());
                seriesEntries.getSeriesEntrySvProzent().getYAxisData().add(auswertung.getDaten().getProzentSchwerverkehr());
                GanglinieUtil.setLegendInZaehldatenStepline(zaehldatenStepline, ChartLegendUtil.SCHWERVERKEHR_ANTEIL_PROZENT);
                GanglinieUtil.setRangeMaxPercentRoundedToTwoInZaehldatenStepline(zaehldatenStepline, auswertung.getDaten().getProzentSchwerverkehr());
            }
            if (fahrzeugOptions.isGueterverkehr()) {
                GanglinieUtil.setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryGv());
                seriesEntries.getSeriesEntryGv().getYAxisData().add(GanglinieUtil.getIntValueIfNotNull(auswertung.getDaten().getSummeGueterverkehr()));
                GanglinieUtil.setLegendInZaehldatenStepline(zaehldatenStepline, ChartLegendUtil.GUETERVERKEHR);
                GanglinieUtil.setRangeMaxRoundedToTwentyInZaehldatenStepline(zaehldatenStepline,
                        GanglinieUtil.getIntValueIfNotNull(auswertung.getDaten().getSummeGueterverkehr()));
            }
            if (fahrzeugOptions.isGueterverkehrsanteilProzent()) {
                GanglinieUtil.setSeriesIndexForFirstChartPercent(seriesEntries.getSeriesEntryGvProzent());
                seriesEntries.getSeriesEntryGvProzent().getYAxisData().add(auswertung.getDaten().getProzentGueterverkehr());
                GanglinieUtil.setLegendInZaehldatenStepline(zaehldatenStepline, ChartLegendUtil.GUETERVERKEHR_ANTEIL_PROZENT);
                GanglinieUtil.setRangeMaxPercentRoundedToTwoInZaehldatenStepline(zaehldatenStepline, auswertung.getDaten().getProzentGueterverkehr());
            }

            final var currentXAxisData = zaehldatenStepline.getXAxisDataFirstChart();
            final var newXAxisData = ZaehldatenProcessingUtil.checkAndAddToXAxisWhenNotAvailable(
                    currentXAxisData,
                    getZeitraumForXaxis(auswertung.getZeitraum()));
            zaehldatenStepline.setXAxisDataFirstChart(newXAxisData);

        });
        return zaehldatenStepline;

    }

    public LadeZaehldatenSteplineDTO ladeGanglinieForMultipleMessstellen(final List<AuswertungMessstelle> auswertungMessstellen) {
        log.debug("#ladeGanglinieForMultipleMessstellen");

        final var zaehldatenStepline = this.getInitialZaehldatenStepline();
        final var auswertungByZeitraum = new HashMap<Zeitraum, AuswertungZeitraum>();

        CollectionUtils
                .emptyIfNull(auswertungMessstellen)
                .forEach(auswertungMessstelle -> CollectionUtils
                        .emptyIfNull(auswertungMessstelle.getAuswertungenProZeitraum())
                        .forEach(auswertung -> {
                            final var zeitraum = auswertung.getZeitraum();
                            if (!auswertungByZeitraum.containsKey(zeitraum)) {
                                auswertungByZeitraum.put(
                                        zeitraum,
                                        new AuswertungZeitraum(zeitraum, new HashMap<>()));
                            }
                            auswertungByZeitraum
                                    .get(zeitraum)
                                    .getSummeKfzByMstId()
                                    .put(
                                            auswertungMessstelle.getMstId(),
                                            auswertung.getDaten().getSummeKraftfahrzeugverkehr());
                        }));

        auswertungByZeitraum.values().forEach(auswertungZeitraum -> {
            auswertungZeitraum.summeKfzByMstId.forEach((mstId, summeKfz) -> {
                final var stepLineSeriesEntryMessstelle = new StepLineSeriesEntryIntegerDTO();
                stepLineSeriesEntryMessstelle.setName("MST " + mstId);
                GanglinieUtil.setSeriesIndexForFirstChartValue(stepLineSeriesEntryMessstelle);
                stepLineSeriesEntryMessstelle.getYAxisData().add(GanglinieUtil.getIntValueIfNotNull(summeKfz));
                GanglinieUtil.setLegendInZaehldatenStepline(
                        zaehldatenStepline,
                        stepLineSeriesEntryMessstelle.getName());
                GanglinieUtil.setRangeMaxRoundedToTwentyInZaehldatenStepline(
                        zaehldatenStepline,
                        GanglinieUtil.getIntValueIfNotNull(summeKfz));
            });

            final var currentXAxisData = zaehldatenStepline.getXAxisDataFirstChart();
            final var newXAxisData = ZaehldatenProcessingUtil.checkAndAddToXAxisWhenNotAvailable(
                    currentXAxisData,
                    getZeitraumForXaxis(auswertungZeitraum.getZeitraum()));
            zaehldatenStepline.setXAxisDataFirstChart(newXAxisData);

        });

        return zaehldatenStepline;

    }

    public String getZeitraumForXaxis(final Zeitraum zeitraum) {
        return new StringBuilder()
                .append(zeitraum.getStart().format(YEAR_MONTH_FORMATTER))
                .append(StringUtils.SPACE)
                .append("-")
                .append(StringUtils.SPACE)
                .append(zeitraum.getEnd().format(YEAR_MONTH_FORMATTER))
                .toString();
    }

    protected LadeZaehldatenSteplineDTO getInitialZaehldatenStepline() {
        final var ladeZaehldatenStepline = new LadeZaehldatenSteplineDTO();
        ladeZaehldatenStepline.setRangeMax(0);
        ladeZaehldatenStepline.setRangeMaxPercent(0);
        ladeZaehldatenStepline.setLegend(new ArrayList<>());
        ladeZaehldatenStepline.setXAxisDataFirstChart(new ArrayList<>());
        ladeZaehldatenStepline.setSeriesEntriesFirstChart(new ArrayList<>());
        return ladeZaehldatenStepline;
    }

    /**
     *
     */
    @Data
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    private static class AuswertungZeitraum {

        private Zeitraum zeitraum;

        private HashMap<String, BigDecimal> summeKfzByMstId;
    }
}
