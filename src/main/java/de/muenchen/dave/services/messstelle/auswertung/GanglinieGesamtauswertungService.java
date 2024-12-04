package de.muenchen.dave.services.messstelle.auswertung;

import de.muenchen.dave.domain.dtos.laden.LadeZaehldatenSteplineDTO;
import de.muenchen.dave.domain.dtos.laden.StepLineSeriesEntryIntegerDTO;
import de.muenchen.dave.domain.dtos.messstelle.FahrzeugOptionsDTO;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.AuswertungMessstelle;
import de.muenchen.dave.services.messstelle.Zeitraum;
import de.muenchen.dave.util.ZaehldatenProcessingUtil;
import de.muenchen.dave.util.messstelle.GanglinieUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class GanglinieGesamtauswertungService {

    public final String PREFIX_MESSSTELLE = "MST";

    /**
     * Erstellt die Repräsentation der Zähldaten zur Gangliniendarstellung für eine Messstelle.
     *
     * @param auswertungMessstelle mit den Zähldaten einer Messstelle.
     * @param fahrzeugOptions zur Steuerung der zu repräsentierenden Daten.
     * @return die Repräsentation der Zähldaten für die Gangliniendarstellung.
     */
    public LadeZaehldatenSteplineDTO createGanglinieForSingleMessstelle(
            final AuswertungMessstelle auswertungMessstelle,
            final FahrzeugOptionsDTO fahrzeugOptions) {
        log.debug("#createGanglinieForSingleMessstelle");

        final var zaehldatenStepline = GanglinieUtil.getInitialZaehldatenStepline();
        final var seriesEntries = new GanglinieUtil.SeriesEntries();

        CollectionUtils
                .emptyIfNull(auswertungMessstelle.getAuswertungenProZeitraum())
                .stream()
                .sorted(Comparator.comparing(auswertungZeitraum -> auswertungZeitraum.getZeitraum().getStart()))
                .forEach(auswertung -> {
                    if (fahrzeugOptions.isPersonenkraftwagen()) {
                        GanglinieUtil.setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryPkw());
                        seriesEntries.getSeriesEntryPkw().getYAxisData().add(GanglinieUtil.getIntValueIfNotNull(auswertung.getDaten().getSummeAllePkw()));
                        GanglinieUtil.setLegendInZaehldatenStepline(zaehldatenStepline, seriesEntries.getSeriesEntryPkw().getName());
                        GanglinieUtil.setRangeMaxRoundedToTwentyInZaehldatenStepline(zaehldatenStepline,
                                GanglinieUtil.getIntValueIfNotNull(auswertung.getDaten().getSummeAllePkw()));
                    }
                    if (fahrzeugOptions.isLastkraftwagen()) {
                        GanglinieUtil.setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryLkw());
                        seriesEntries.getSeriesEntryLkw().getYAxisData().add(GanglinieUtil.getIntValueIfNotNull(auswertung.getDaten().getAnzahlLkw()));
                        GanglinieUtil.setLegendInZaehldatenStepline(zaehldatenStepline, seriesEntries.getSeriesEntryLkw().getName());
                        GanglinieUtil.setRangeMaxRoundedToTwentyInZaehldatenStepline(zaehldatenStepline,
                                GanglinieUtil.getIntValueIfNotNull(auswertung.getDaten().getAnzahlLkw()));
                    }
                    if (fahrzeugOptions.isLastzuege()) {
                        GanglinieUtil.setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryLz());
                        seriesEntries.getSeriesEntryLz().getYAxisData().add(GanglinieUtil.getIntValueIfNotNull(auswertung.getDaten().getSummeLastzug()));
                        GanglinieUtil.setLegendInZaehldatenStepline(zaehldatenStepline, seriesEntries.getSeriesEntryLz().getName());
                        GanglinieUtil.setRangeMaxRoundedToTwentyInZaehldatenStepline(zaehldatenStepline,
                                GanglinieUtil.getIntValueIfNotNull(auswertung.getDaten().getSummeLastzug()));
                    }
                    if (fahrzeugOptions.isLieferwagen()) {
                        GanglinieUtil.setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryLfw());
                        seriesEntries.getSeriesEntryLfw().getYAxisData().add(GanglinieUtil.getIntValueIfNotNull(auswertung.getDaten().getAnzahlLfw()));
                        GanglinieUtil.setLegendInZaehldatenStepline(zaehldatenStepline, seriesEntries.getSeriesEntryLfw().getName());
                        GanglinieUtil.setRangeMaxRoundedToTwentyInZaehldatenStepline(zaehldatenStepline,
                                GanglinieUtil.getIntValueIfNotNull(auswertung.getDaten().getAnzahlLfw()));
                    }
                    if (fahrzeugOptions.isBusse()) {
                        GanglinieUtil.setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryBus());
                        seriesEntries.getSeriesEntryBus().getYAxisData().add(GanglinieUtil.getIntValueIfNotNull(auswertung.getDaten().getAnzahlBus()));
                        GanglinieUtil.setLegendInZaehldatenStepline(zaehldatenStepline, seriesEntries.getSeriesEntryBus().getName());
                        GanglinieUtil.setRangeMaxRoundedToTwentyInZaehldatenStepline(zaehldatenStepline,
                                GanglinieUtil.getIntValueIfNotNull(auswertung.getDaten().getAnzahlBus()));
                    }
                    if (fahrzeugOptions.isKraftraeder()) {
                        GanglinieUtil.setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryKrad());
                        seriesEntries.getSeriesEntryKrad().getYAxisData().add(GanglinieUtil.getIntValueIfNotNull(auswertung.getDaten().getAnzahlKrad()));
                        GanglinieUtil.setLegendInZaehldatenStepline(zaehldatenStepline, seriesEntries.getSeriesEntryKrad().getName());
                        GanglinieUtil.setRangeMaxRoundedToTwentyInZaehldatenStepline(zaehldatenStepline,
                                GanglinieUtil.getIntValueIfNotNull(auswertung.getDaten().getAnzahlKrad()));
                    }
                    if (fahrzeugOptions.isRadverkehr()) {
                        GanglinieUtil.setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryRad());
                        seriesEntries.getSeriesEntryRad().getYAxisData().add(GanglinieUtil.getIntValueIfNotNull(auswertung.getDaten().getAnzahlRad()));
                        GanglinieUtil.setLegendInZaehldatenStepline(zaehldatenStepline, seriesEntries.getSeriesEntryRad().getName());
                        GanglinieUtil.setRangeMaxRoundedToTwentyInZaehldatenStepline(zaehldatenStepline,
                                GanglinieUtil.getIntValueIfNotNull(auswertung.getDaten().getAnzahlRad()));
                    }
                    if (fahrzeugOptions.isKraftfahrzeugverkehr()) {
                        GanglinieUtil.setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryKfz());
                        seriesEntries.getSeriesEntryKfz().getYAxisData()
                                .add(GanglinieUtil.getIntValueIfNotNull(auswertung.getDaten().getSummeKraftfahrzeugverkehr()));
                        GanglinieUtil.setLegendInZaehldatenStepline(zaehldatenStepline, seriesEntries.getSeriesEntryKfz().getName());
                        GanglinieUtil.setRangeMaxRoundedToTwentyInZaehldatenStepline(zaehldatenStepline,
                                GanglinieUtil.getIntValueIfNotNull(auswertung.getDaten().getSummeKraftfahrzeugverkehr()));
                    }
                    if (fahrzeugOptions.isSchwerverkehr()) {
                        GanglinieUtil.setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntrySv());
                        seriesEntries.getSeriesEntrySv().getYAxisData().add(GanglinieUtil.getIntValueIfNotNull(auswertung.getDaten().getSummeSchwerverkehr()));
                        GanglinieUtil.setLegendInZaehldatenStepline(zaehldatenStepline, seriesEntries.getSeriesEntrySv().getName());
                        GanglinieUtil.setRangeMaxRoundedToTwentyInZaehldatenStepline(zaehldatenStepline,
                                GanglinieUtil.getIntValueIfNotNull(auswertung.getDaten().getSummeSchwerverkehr()));
                    }
                    if (fahrzeugOptions.isSchwerverkehrsanteilProzent()) {
                        GanglinieUtil.setSeriesIndexForFirstChartPercent(seriesEntries.getSeriesEntrySvProzent());
                        seriesEntries.getSeriesEntrySvProzent().getYAxisData().add(auswertung.getDaten().getProzentSchwerverkehr());
                        GanglinieUtil.setLegendInZaehldatenStepline(zaehldatenStepline, seriesEntries.getSeriesEntrySvProzent().getName());
                        GanglinieUtil.setRangeMaxPercentRoundedToTwoInZaehldatenStepline(zaehldatenStepline, auswertung.getDaten().getProzentSchwerverkehr());
                    }
                    if (fahrzeugOptions.isGueterverkehr()) {
                        GanglinieUtil.setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryGv());
                        seriesEntries.getSeriesEntryGv().getYAxisData().add(GanglinieUtil.getIntValueIfNotNull(auswertung.getDaten().getSummeGueterverkehr()));
                        GanglinieUtil.setLegendInZaehldatenStepline(zaehldatenStepline, seriesEntries.getSeriesEntryGv().getName());
                        GanglinieUtil.setRangeMaxRoundedToTwentyInZaehldatenStepline(zaehldatenStepline,
                                GanglinieUtil.getIntValueIfNotNull(auswertung.getDaten().getSummeGueterverkehr()));
                    }
                    if (fahrzeugOptions.isGueterverkehrsanteilProzent()) {
                        GanglinieUtil.setSeriesIndexForFirstChartPercent(seriesEntries.getSeriesEntryGvProzent());
                        seriesEntries.getSeriesEntryGvProzent().getYAxisData().add(auswertung.getDaten().getProzentGueterverkehr());
                        GanglinieUtil.setLegendInZaehldatenStepline(zaehldatenStepline, seriesEntries.getSeriesEntryGvProzent().getName());
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
     * Erstellt die Repräsentation der Zähldaten zur Gangliniendarstellung für mehrere Messstellen.
     *
     * @param auswertungMessstellen mit den Zähldaten der Messstellen.
     * @param fahrzeugOptions zur Steuerung der zu repräsentierenden Daten.
     * @return die Repräsentation der Zähldaten für die Gangliniendarstellung.
     */
    public LadeZaehldatenSteplineDTO createGanglinieForMultipleMessstellen(
            final List<AuswertungMessstelle> auswertungMessstellen,
            final FahrzeugOptionsDTO fahrzeugOptions) {
        log.debug("#createGanglinieForMultipleMessstellen");

        final var zaehldatenStepline = GanglinieUtil.getInitialZaehldatenStepline();
        zaehldatenStepline.setSeriesEntriesFirstChart(new ArrayList<>());

        CollectionUtils
                .emptyIfNull(auswertungMessstellen)
                .forEach(auswertungMessstelle -> {

                    final var seriesEntries = new GanglinieUtil.SeriesEntries();
                    prependMstIdBeforeNameOfSeriesEntries(auswertungMessstelle.getMstId(), seriesEntries);

                    CollectionUtils
                            .emptyIfNull(auswertungMessstelle.getAuswertungenProZeitraum())
                            .stream()
                            .sorted(Comparator.comparing(auswertungZeitraum -> auswertungZeitraum.getZeitraum().getStart()))
                            .forEach(auswertung -> {
                                if (fahrzeugOptions.isPersonenkraftwagen()) {
                                    GanglinieUtil.setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryPkw());
                                    seriesEntries.getSeriesEntryPkw().getYAxisData()
                                            .add(GanglinieUtil.getIntValueIfNotNull(auswertung.getDaten().getSummeAllePkw()));
                                    GanglinieUtil.setLegendInZaehldatenStepline(zaehldatenStepline, seriesEntries.getSeriesEntryPkw().getName());
                                    GanglinieUtil.setRangeMaxRoundedToTwentyInZaehldatenStepline(zaehldatenStepline,
                                            GanglinieUtil.getIntValueIfNotNull(auswertung.getDaten().getSummeAllePkw()));
                                }
                                if (fahrzeugOptions.isLastkraftwagen()) {
                                    GanglinieUtil.setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryLkw());
                                    seriesEntries.getSeriesEntryLkw().getYAxisData()
                                            .add(GanglinieUtil.getIntValueIfNotNull(auswertung.getDaten().getAnzahlLkw()));
                                    GanglinieUtil.setLegendInZaehldatenStepline(zaehldatenStepline, seriesEntries.getSeriesEntryLkw().getName());
                                    GanglinieUtil.setRangeMaxRoundedToTwentyInZaehldatenStepline(zaehldatenStepline,
                                            GanglinieUtil.getIntValueIfNotNull(auswertung.getDaten().getAnzahlLkw()));
                                }
                                if (fahrzeugOptions.isLastzuege()) {
                                    GanglinieUtil.setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryLz());
                                    seriesEntries.getSeriesEntryLz().getYAxisData()
                                            .add(GanglinieUtil.getIntValueIfNotNull(auswertung.getDaten().getSummeLastzug()));
                                    GanglinieUtil.setLegendInZaehldatenStepline(zaehldatenStepline, seriesEntries.getSeriesEntryLz().getName());
                                    GanglinieUtil.setRangeMaxRoundedToTwentyInZaehldatenStepline(zaehldatenStepline,
                                            GanglinieUtil.getIntValueIfNotNull(auswertung.getDaten().getSummeLastzug()));
                                }
                                if (fahrzeugOptions.isLieferwagen()) {
                                    GanglinieUtil.setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryLfw());
                                    seriesEntries.getSeriesEntryLfw().getYAxisData()
                                            .add(GanglinieUtil.getIntValueIfNotNull(auswertung.getDaten().getAnzahlLfw()));
                                    GanglinieUtil.setLegendInZaehldatenStepline(zaehldatenStepline, seriesEntries.getSeriesEntryLfw().getName());
                                    GanglinieUtil.setRangeMaxRoundedToTwentyInZaehldatenStepline(zaehldatenStepline,
                                            GanglinieUtil.getIntValueIfNotNull(auswertung.getDaten().getAnzahlLfw()));
                                }
                                if (fahrzeugOptions.isBusse()) {
                                    GanglinieUtil.setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryBus());
                                    seriesEntries.getSeriesEntryBus().getYAxisData()
                                            .add(GanglinieUtil.getIntValueIfNotNull(auswertung.getDaten().getAnzahlBus()));
                                    GanglinieUtil.setLegendInZaehldatenStepline(zaehldatenStepline, seriesEntries.getSeriesEntryBus().getName());
                                    GanglinieUtil.setRangeMaxRoundedToTwentyInZaehldatenStepline(zaehldatenStepline,
                                            GanglinieUtil.getIntValueIfNotNull(auswertung.getDaten().getAnzahlBus()));
                                }
                                if (fahrzeugOptions.isKraftraeder()) {
                                    GanglinieUtil.setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryKrad());
                                    seriesEntries.getSeriesEntryKrad().getYAxisData()
                                            .add(GanglinieUtil.getIntValueIfNotNull(auswertung.getDaten().getAnzahlKrad()));
                                    GanglinieUtil.setLegendInZaehldatenStepline(zaehldatenStepline, seriesEntries.getSeriesEntryKrad().getName());
                                    GanglinieUtil.setRangeMaxRoundedToTwentyInZaehldatenStepline(zaehldatenStepline,
                                            GanglinieUtil.getIntValueIfNotNull(auswertung.getDaten().getAnzahlKrad()));
                                }
                                if (fahrzeugOptions.isRadverkehr()) {
                                    GanglinieUtil.setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryRad());
                                    seriesEntries.getSeriesEntryRad().getYAxisData()
                                            .add(GanglinieUtil.getIntValueIfNotNull(auswertung.getDaten().getAnzahlRad()));
                                    GanglinieUtil.setLegendInZaehldatenStepline(zaehldatenStepline, seriesEntries.getSeriesEntryRad().getName());
                                    GanglinieUtil.setRangeMaxRoundedToTwentyInZaehldatenStepline(zaehldatenStepline,
                                            GanglinieUtil.getIntValueIfNotNull(auswertung.getDaten().getAnzahlRad()));
                                }
                                if (fahrzeugOptions.isKraftfahrzeugverkehr()) {
                                    GanglinieUtil.setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryKfz());
                                    seriesEntries.getSeriesEntryKfz().getYAxisData()
                                            .add(GanglinieUtil.getIntValueIfNotNull(auswertung.getDaten().getSummeKraftfahrzeugverkehr()));
                                    GanglinieUtil.setLegendInZaehldatenStepline(zaehldatenStepline, seriesEntries.getSeriesEntryKfz().getName());
                                    GanglinieUtil.setRangeMaxRoundedToTwentyInZaehldatenStepline(zaehldatenStepline,
                                            GanglinieUtil.getIntValueIfNotNull(auswertung.getDaten().getSummeKraftfahrzeugverkehr()));
                                }
                                if (fahrzeugOptions.isSchwerverkehr()) {
                                    GanglinieUtil.setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntrySv());
                                    seriesEntries.getSeriesEntrySv().getYAxisData()
                                            .add(GanglinieUtil.getIntValueIfNotNull(auswertung.getDaten().getSummeSchwerverkehr()));
                                    GanglinieUtil.setLegendInZaehldatenStepline(zaehldatenStepline, seriesEntries.getSeriesEntrySv().getName());
                                    GanglinieUtil.setRangeMaxRoundedToTwentyInZaehldatenStepline(zaehldatenStepline,
                                            GanglinieUtil.getIntValueIfNotNull(auswertung.getDaten().getSummeSchwerverkehr()));
                                }
                                if (fahrzeugOptions.isSchwerverkehrsanteilProzent()) {
                                    GanglinieUtil.setSeriesIndexForFirstChartPercent(seriesEntries.getSeriesEntrySvProzent());
                                    seriesEntries.getSeriesEntrySvProzent().getYAxisData().add(auswertung.getDaten().getProzentSchwerverkehr());
                                    GanglinieUtil.setLegendInZaehldatenStepline(zaehldatenStepline, seriesEntries.getSeriesEntrySvProzent().getName());
                                    GanglinieUtil.setRangeMaxPercentRoundedToTwoInZaehldatenStepline(zaehldatenStepline,
                                            auswertung.getDaten().getProzentSchwerverkehr());
                                }
                                if (fahrzeugOptions.isGueterverkehr()) {
                                    GanglinieUtil.setSeriesIndexForFirstChartValue(seriesEntries.getSeriesEntryGv());
                                    seriesEntries.getSeriesEntryGv().getYAxisData()
                                            .add(GanglinieUtil.getIntValueIfNotNull(auswertung.getDaten().getSummeGueterverkehr()));
                                    GanglinieUtil.setLegendInZaehldatenStepline(zaehldatenStepline, seriesEntries.getSeriesEntryGv().getName());
                                    GanglinieUtil.setRangeMaxRoundedToTwentyInZaehldatenStepline(zaehldatenStepline,
                                            GanglinieUtil.getIntValueIfNotNull(auswertung.getDaten().getSummeGueterverkehr()));
                                }
                                if (fahrzeugOptions.isGueterverkehrsanteilProzent()) {
                                    GanglinieUtil.setSeriesIndexForFirstChartPercent(seriesEntries.getSeriesEntryGvProzent());
                                    seriesEntries.getSeriesEntryGvProzent().getYAxisData().add(auswertung.getDaten().getProzentGueterverkehr());
                                    GanglinieUtil.setLegendInZaehldatenStepline(zaehldatenStepline, seriesEntries.getSeriesEntryGvProzent().getName());
                                    GanglinieUtil.setRangeMaxPercentRoundedToTwoInZaehldatenStepline(zaehldatenStepline,
                                            auswertung.getDaten().getProzentGueterverkehr());
                                }

                                final var currentXAxisData = zaehldatenStepline.getXAxisDataFirstChart();
                                final var newXAxisData = ZaehldatenProcessingUtil.checkAndAddToXAxisWhenNotAvailable(
                                        currentXAxisData,
                                        getZeitraumForXaxis(auswertung.getZeitraum()));
                                zaehldatenStepline.setXAxisDataFirstChart(newXAxisData);

                            });

                    zaehldatenStepline.getSeriesEntriesFirstChart().addAll(seriesEntries.getChosenStepLineSeriesEntries(fahrzeugOptions));
                });

        return zaehldatenStepline;
    }

    /**
     * Gibt die Textuelle-Repräsentation des Zeitraums zurück.
     *
     * @param zeitraum zur Stringerstellung.
     * @return die String-Repräsentation des Zeitraums.
     */
    protected String getZeitraumForXaxis(final Zeitraum zeitraum) {
        final var bezeichnerZeitraum = zeitraum.getAuswertungsZeitraum().getText();
        return bezeichnerZeitraum
                .concat(bezeichnerZeitraum.isEmpty() ? StringUtils.EMPTY : ".")
                .concat(String.valueOf(zeitraum.getStart().getYear()));
    }

    /**
     * Fügt dem Namensattribut eines jeden {@link StepLineSeriesEntryIntegerDTO}
     * in den {@link GanglinieUtil.SeriesEntries} die Messtellen-Id als Prefix hinzu.
     *
     * @param mstId als ID der Messstelle.
     * @param seriesEntries mit den darin enthaltenen.
     */
    protected void prependMstIdBeforeNameOfSeriesEntries(final String mstId, final GanglinieUtil.SeriesEntries seriesEntries) {
        final var prefixMstId = PREFIX_MESSSTELLE + StringUtils.SPACE + mstId + StringUtils.SPACE;

        String nameSeriesEntry = seriesEntries.getSeriesEntryPkw().getName();
        nameSeriesEntry = StringUtils.prependIfMissing(nameSeriesEntry, prefixMstId);
        seriesEntries.getSeriesEntryPkw().setName(nameSeriesEntry);

        nameSeriesEntry = seriesEntries.getSeriesEntryLkw().getName();
        nameSeriesEntry = StringUtils.prependIfMissing(nameSeriesEntry, prefixMstId);
        seriesEntries.getSeriesEntryLkw().setName(nameSeriesEntry);

        nameSeriesEntry = seriesEntries.getSeriesEntryLfw().getName();
        nameSeriesEntry = StringUtils.prependIfMissing(nameSeriesEntry, prefixMstId);
        seriesEntries.getSeriesEntryLfw().setName(nameSeriesEntry);

        nameSeriesEntry = seriesEntries.getSeriesEntryLz().getName();
        nameSeriesEntry = StringUtils.prependIfMissing(nameSeriesEntry, prefixMstId);
        seriesEntries.getSeriesEntryLz().setName(nameSeriesEntry);

        nameSeriesEntry = seriesEntries.getSeriesEntryBus().getName();
        nameSeriesEntry = StringUtils.prependIfMissing(nameSeriesEntry, prefixMstId);
        seriesEntries.getSeriesEntryBus().setName(nameSeriesEntry);

        nameSeriesEntry = seriesEntries.getSeriesEntryBus().getName();
        nameSeriesEntry = StringUtils.prependIfMissing(nameSeriesEntry, prefixMstId);
        seriesEntries.getSeriesEntryBus().setName(nameSeriesEntry);

        nameSeriesEntry = seriesEntries.getSeriesEntryKrad().getName();
        nameSeriesEntry = StringUtils.prependIfMissing(nameSeriesEntry, prefixMstId);
        seriesEntries.getSeriesEntryKrad().setName(nameSeriesEntry);

        nameSeriesEntry = seriesEntries.getSeriesEntryRad().getName();
        nameSeriesEntry = StringUtils.prependIfMissing(nameSeriesEntry, prefixMstId);
        seriesEntries.getSeriesEntryRad().setName(nameSeriesEntry);

        nameSeriesEntry = seriesEntries.getSeriesEntryKfz().getName();
        nameSeriesEntry = StringUtils.prependIfMissing(nameSeriesEntry, prefixMstId);
        seriesEntries.getSeriesEntryKfz().setName(nameSeriesEntry);

        nameSeriesEntry = seriesEntries.getSeriesEntrySv().getName();
        nameSeriesEntry = StringUtils.prependIfMissing(nameSeriesEntry, prefixMstId);
        seriesEntries.getSeriesEntrySv().setName(nameSeriesEntry);

        nameSeriesEntry = seriesEntries.getSeriesEntrySvProzent().getName();
        nameSeriesEntry = StringUtils.prependIfMissing(nameSeriesEntry, prefixMstId);
        seriesEntries.getSeriesEntrySvProzent().setName(nameSeriesEntry);

        nameSeriesEntry = seriesEntries.getSeriesEntryGv().getName();
        nameSeriesEntry = StringUtils.prependIfMissing(nameSeriesEntry, prefixMstId);
        seriesEntries.getSeriesEntryGv().setName(nameSeriesEntry);

        nameSeriesEntry = seriesEntries.getSeriesEntryGvProzent().getName();
        nameSeriesEntry = StringUtils.prependIfMissing(nameSeriesEntry, prefixMstId);
        seriesEntries.getSeriesEntryGvProzent().setName(nameSeriesEntry);
    }

}
