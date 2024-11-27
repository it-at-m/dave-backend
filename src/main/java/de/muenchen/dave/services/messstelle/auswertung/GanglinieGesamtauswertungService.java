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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class GanglinieGesamtauswertungService {

    /**
     * Erstellt die Repräsentation der Zähldaten zur Gangliniendarstellung für eine Messstelle.
     *
     * @param auswertungMessstelle mit den Zähldaten.
     * @param fahrzeugOptions zur Steuerung der zu repräsentierenden Daten.
     * @return die Repräsentation der Zähldaten für die Gangliniendarstellung.
     */
    public LadeZaehldatenSteplineDTO createGanglinieForSingleMessstelle(
            final AuswertungMessstelle auswertungMessstelle,
            final FahrzeugOptionsDTO fahrzeugOptions) {
        log.debug("#createGanglinieForSingleMessstelle");

        final var auswertungenProZeitraum = CollectionUtils.emptyIfNull(auswertungMessstelle.getAuswertungenProZeitraum());
        final var zaehldatenStepline = GanglinieUtil.getInitialZaehldatenStepline();
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

        zaehldatenStepline.setSeriesEntriesFirstChart(seriesEntries.getChosenStepLineSeriesEntries(fahrzeugOptions));
        return zaehldatenStepline;

    }

    /**
     * Erstellt die Repräsentation der Zähldaten (Summe KFZ) zur Gangliniendarstellung für mehrere
     * Messstellen.
     *
     * @param auswertungMessstellen mit den Zähldaten.
     * @return die Repräsentation der Zähldaten (Summe KFZ) für die Gangliniendarstellung.
     */
    public LadeZaehldatenSteplineDTO createGanglinieForMultipleMessstellen(final List<AuswertungMessstelle> auswertungMessstellen) {
        log.debug("#createGanglinieForMultipleMessstellen");

        final var zaehldatenStepline = GanglinieUtil.getInitialZaehldatenStepline();
        final var auswertungByZeitraum = new HashMap<Zeitraum, AuswertungZeitraum>();

        // Gruppieren der Auswertungen nach Zeitraum
        // Jeder Zeitraum umfasst je Messstelle die Anzahl der KFZ
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
                            // Hinzufügen der Messstelle mit der Summe an KFZ.
                            auswertungByZeitraum
                                    .get(zeitraum)
                                    .getSummeKfzByMstId()
                                    .put(
                                            auswertungMessstelle.getMstId(),
                                            auswertung.getDaten().getSummeKraftfahrzeugverkehr());
                        }));

        // Aufbereitung der im oberen Abschnitt gruppierten Zähldaten für Gangliniendarstellung.
        auswertungByZeitraum
                .values()
                .stream()
                .sorted(Comparator.comparing(auswertungZeitraum -> auswertungZeitraum.getZeitraum().getStart()))
                .forEach(auswertungZeitraum -> {

                    final var stepLineSeriesEntryMessstelle = new StepLineSeriesEntryIntegerDTO();

                    auswertungZeitraum.summeKfzByMstId.forEach((mstId, summeKfz) -> {
                        stepLineSeriesEntryMessstelle.setName("MST " + mstId);
                        GanglinieUtil.setSeriesIndexForFirstChartValue(stepLineSeriesEntryMessstelle);
                        stepLineSeriesEntryMessstelle.getYAxisData().add(GanglinieUtil.getIntValueIfNotNull(summeKfz));
                        GanglinieUtil.setLegendInZaehldatenStepline(
                                zaehldatenStepline,
                                stepLineSeriesEntryMessstelle.getName());
                        GanglinieUtil.setRangeMaxRoundedToTwentyInZaehldatenStepline(
                                zaehldatenStepline,
                                GanglinieUtil.getIntValueIfNotNull(summeKfz));

                        zaehldatenStepline.getSeriesEntriesFirstChart().add(stepLineSeriesEntryMessstelle);
                    });

                    final var currentXAxisData = zaehldatenStepline.getXAxisDataFirstChart();
                    final var newXAxisData = ZaehldatenProcessingUtil.checkAndAddToXAxisWhenNotAvailable(
                            currentXAxisData,
                            getZeitraumForXaxis(auswertungZeitraum.getZeitraum()));
                    zaehldatenStepline.setXAxisDataFirstChart(newXAxisData);

                });

        return zaehldatenStepline;
    }

    /**
     * Gibt die String-Repräsentation des Zeitraums zurück.
     *
     * @param zeitraum zur Stringerstellung.
     * @return die String-Repräsentation des Zeitraums.
     */
    public String getZeitraumForXaxis(final Zeitraum zeitraum) {
        final var bezeichnerZeitraum = zeitraum.getAuswertungsZeitraum().getText();
        return bezeichnerZeitraum
                .concat(bezeichnerZeitraum.isEmpty() ? StringUtils.EMPTY : ".")
                .concat(String.valueOf(zeitraum.getStart().getYear()));
    }

    /**
     * Die Klasse repräsentiert für einen Zeitraum je Messstelle die Summe der Kfz.
     */
    @Data
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    private static class AuswertungZeitraum {

        private Zeitraum zeitraum;

        private HashMap<String, BigDecimal> summeKfzByMstId;
    }
}
