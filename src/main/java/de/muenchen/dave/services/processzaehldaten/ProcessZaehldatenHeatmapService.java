/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.services.processzaehldaten;

import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatenHeatmapDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatenTableDTO;
import de.muenchen.dave.domain.enums.Zaehldauer;
import de.muenchen.dave.domain.enums.Zeitblock;
import de.muenchen.dave.services.ladezaehldaten.LadeZaehldatenService;
import de.muenchen.dave.util.ChartLegendUtil;
import de.muenchen.dave.util.ZaehldatenProcessingUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


@Service
@Slf4j
public class ProcessZaehldatenHeatmapService {

    private static final int SPLIT_DIVISOR = 2;

    /**
     * Diese Methode fügt einen einzelnen in der Heatmap darzustellenden Wert in
     * das im Parameter ladeZaehldatenHeatmap übergebenen Objekt ein.
     * Des Weiteren wird ein Legendeneintrag für den Parameter legendEntry gesetzt,
     * falls dieser noch nicht vorhanden ist.
     * Zusätzlich werden die Variablen RangeMin und RangeMax gesetzt.
     *
     * @param ladeZaehldatenHeatmap  Das Objekt in welchem die aufbereiteten Daten vorgehalten werden.
     * @param heatMapEntryIndex      Spaltenindex der X-Achse zur Positionierung des Wertes aus Parameter value in Heatmap.
     * @param klassenKategorienIndex Zeilenindex der Y-Achse zur Positionierung des Wertes aus Parameter value in Heatmap.
     * @param value                  Der Wert welcher an der Position, definiert durch Spaltenindex und Zeilenindex,
     *                               in der Heatmap dargestellt werde soll. Des Weiteren wird dieser Wert zur Ermittlung von
     *                               {@link LadeZaehldatenHeatmapDTO}#getRangeMax() und
     *                               {@link LadeZaehldatenHeatmapDTO}#getRangeMin() herangezogen.
     * @param legendEntry            Der Legendeneintrag welcher in {@link LadeZaehldatenHeatmapDTO}#getLegend() hinterlegt wird.
     */
    public static void insertSingleHeatmapDataIntoLadeZaehldatenHeatmap(final LadeZaehldatenHeatmapDTO ladeZaehldatenHeatmap,
                                                                        final int heatMapEntryIndex,
                                                                        final int klassenKategorienIndex,
                                                                        final Integer value,
                                                                        final String legendEntry) {
        ladeZaehldatenHeatmap.setLegend(
                ChartLegendUtil.checkAndAddToLegendWhenNotAvailable(
                        ladeZaehldatenHeatmap.getLegend(),
                        legendEntry
                )
        );
        final int nullCheckedValue = ObjectUtils.defaultIfNull(value, 0);
        ladeZaehldatenHeatmap.setRangeMin(
                Math.min(nullCheckedValue, ladeZaehldatenHeatmap.getRangeMin())
        );
        ladeZaehldatenHeatmap.setRangeMax(
                Math.max(nullCheckedValue, ladeZaehldatenHeatmap.getRangeMax())
        );
        ladeZaehldatenHeatmap.getSeriesEntriesFirstChart().add(
                createHeatMapEntry(
                        heatMapEntryIndex,
                        klassenKategorienIndex,
                        value
                )
        );
    }

    /**
     * Erstellt einen einzelnen Eintrag in der Heatmap.
     *
     * @param heatMapEntryIndex      Der Spaltenindex in der Heatmap
     * @param klassenKategorienIndex Der Zeilenindex in der Heatmap
     * @param value                  Der Wert im entsprechenden Heatmapfeld definiert durch Spaltenindex und Zeilenindex.
     * @return Eine Liste bestehend aus Spaltenindex, Zeilenindex und dem Wert.
     */
    public static List<Integer> createHeatMapEntry(final int heatMapEntryIndex,
                                                   final int klassenKategorienIndex,
                                                   final Integer value) {
        final List<Integer> heatmapEntry = new ArrayList<>();
        heatmapEntry.add(heatMapEntryIndex);
        heatmapEntry.add(klassenKategorienIndex);
        heatmapEntry.add(value);
        return heatmapEntry;
    }

    /**
     * Diese Methode führt die Datenaufbereitung für das Heatmap-Diagramm durch.
     * Als Basis zur Datenaufbereitung dienen die im Parameter zaehldatenTable
     * übergebenen Informationen.
     * Die in den options gewählten Fahrzeugklassen bzw. Fahrzeugkategorien
     * werden in dieser Methode zur Darstellung in der Heatmap aufbereitet.
     * <p>
     * Sind in den options die Werte {@link Zeitblock#ZB_00_24} und {@link Zaehldauer#DAUER_2_X_4_STUNDEN}
     * zu finden, so wird die Datenaufbereitung für zwei Unterdiagramme vorgenommen.
     * Ist diese Wertkombination nicht vorhanden, findet keine Aufteilung in zwei Unterdiagramme statt
     * und die Daten werden für ein Diagramm aufbereitet.
     * <p>
     * Wenn die Aufteilung in zwei Unterdiagramme vorgenommen wird, werden die beiden Felder
     * {@link LadeZaehldatenHeatmapDTO}#getXAxisDataSecondChart und {@link LadeZaehldatenHeatmapDTO}#getSeriesEntriesSecondChart
     * befüllt, die ansonsten leer bleiben.
     *
     * @param zaehldatenTable Die Datengrundlage zur Aufbereitung des Heatmap-Diagramms.
     * @param options         Die durch den User im Frontend gewählten Optionen.
     * @return Die aufbreiteten Daten für das Heatmap-Diagramm entsprechend der gewählten Optionen.
     */
    public LadeZaehldatenHeatmapDTO ladeProcessedZaehldatenHeatmap(final LadeZaehldatenTableDTO zaehldatenTable,
                                                                   final OptionsDTO options) {
        final LadeZaehldatenHeatmapDTO ladeZaehldatenHeatmap = new LadeZaehldatenHeatmapDTO();
        ladeZaehldatenHeatmap.setRangeMin(0);
        ladeZaehldatenHeatmap.setRangeMax(0);
        ladeZaehldatenHeatmap.setLegend(new ArrayList<>());
        ladeZaehldatenHeatmap.setXAxisDataFirstChart(new ArrayList<>());
        ladeZaehldatenHeatmap.setSeriesEntriesFirstChart(new ArrayList<>());
        AtomicInteger heatMapEntryIndex = new AtomicInteger(0);
        zaehldatenTable.getZaehldaten().stream()
                .filter(ladeZaehldatum -> ObjectUtils.isEmpty(ladeZaehldatum.getType()))
                .forEach(ladeZaehldatum -> {
                    final AtomicInteger klassenKategorienIndex = new AtomicInteger(0);
                    if (options.getPkwEinheiten()) {
                        insertSingleHeatmapDataIntoLadeZaehldatenHeatmap(
                                ladeZaehldatenHeatmap,
                                heatMapEntryIndex.get(),
                                klassenKategorienIndex.get(),
                                ladeZaehldatum.getPkwEinheiten(),
                                ChartLegendUtil.PKW_EINHEITEN_HEATMAP);
                        klassenKategorienIndex.getAndIncrement();
                    }
                    if (options.getGueterverkehr()) {
                        insertSingleHeatmapDataIntoLadeZaehldatenHeatmap(
                                ladeZaehldatenHeatmap,
                                heatMapEntryIndex.get(),
                                klassenKategorienIndex.get(),
                                ZaehldatenProcessingUtil.nullsafeCast(ladeZaehldatum.getGueterverkehr()),
                                ChartLegendUtil.GUETERVERKEHR_HEATMAP);
                        klassenKategorienIndex.getAndIncrement();
                    }
                    if (options.getSchwerverkehr()) {
                        insertSingleHeatmapDataIntoLadeZaehldatenHeatmap(
                                ladeZaehldatenHeatmap,
                                heatMapEntryIndex.get(),
                                klassenKategorienIndex.get(),
                                ZaehldatenProcessingUtil.nullsafeCast(ladeZaehldatum.getSchwerverkehr()),
                                ChartLegendUtil.SCHWERVERKEHR_HEATMAP);
                        klassenKategorienIndex.getAndIncrement();
                    }
                    if (options.getKraftfahrzeugverkehr()) {
                        insertSingleHeatmapDataIntoLadeZaehldatenHeatmap(
                                ladeZaehldatenHeatmap,
                                heatMapEntryIndex.get(),
                                klassenKategorienIndex.get(),
                                ZaehldatenProcessingUtil.nullsafeCast(ladeZaehldatum.getKfz()),
                                ChartLegendUtil.KFZ_HEATMAP);
                        klassenKategorienIndex.getAndIncrement();
                    }
                    if (options.getFussverkehr()) {
                        insertSingleHeatmapDataIntoLadeZaehldatenHeatmap(
                                ladeZaehldatenHeatmap,
                                heatMapEntryIndex.get(),
                                klassenKategorienIndex.get(),
                                ladeZaehldatum.getFussgaenger(),
                                ChartLegendUtil.FUSSGAENGER_HEATMAP);
                        klassenKategorienIndex.getAndIncrement();
                    }
                    if (options.getRadverkehr()) {
                        insertSingleHeatmapDataIntoLadeZaehldatenHeatmap(
                                ladeZaehldatenHeatmap,
                                heatMapEntryIndex.get(),
                                klassenKategorienIndex.get(),
                                ladeZaehldatum.getFahrradfahrer(),
                                ChartLegendUtil.RAD_HEATMAP);
                        klassenKategorienIndex.getAndIncrement();
                    }
                    if (options.getKraftraeder()) {
                        insertSingleHeatmapDataIntoLadeZaehldatenHeatmap(
                                ladeZaehldatenHeatmap,
                                heatMapEntryIndex.get(),
                                klassenKategorienIndex.get(),
                                ladeZaehldatum.getKraftraeder(),
                                ChartLegendUtil.KRAFTRAEDER_HEATMAP);
                        klassenKategorienIndex.getAndIncrement();
                    }
                    if (options.getBusse()) {
                        insertSingleHeatmapDataIntoLadeZaehldatenHeatmap(
                                ladeZaehldatenHeatmap,
                                heatMapEntryIndex.get(),
                                klassenKategorienIndex.get(),
                                ladeZaehldatum.getBusse(),
                                ChartLegendUtil.BUSSE_HEATMAP);
                        klassenKategorienIndex.getAndIncrement();
                    }
                    if (options.getLastzuege()) {
                        insertSingleHeatmapDataIntoLadeZaehldatenHeatmap(
                                ladeZaehldatenHeatmap,
                                heatMapEntryIndex.get(),
                                klassenKategorienIndex.get(),
                                ladeZaehldatum.getLastzuege(),
                                ChartLegendUtil.LASTZUEGE_HEATMAP);
                        klassenKategorienIndex.getAndIncrement();
                    }
                    if (options.getLastkraftwagen()) {
                        insertSingleHeatmapDataIntoLadeZaehldatenHeatmap(
                                ladeZaehldatenHeatmap,
                                heatMapEntryIndex.get(),
                                klassenKategorienIndex.get(),
                                ladeZaehldatum.getLkw(),
                                ChartLegendUtil.LKW_HEATMAP);
                        klassenKategorienIndex.getAndIncrement();
                    }
                    if (options.getPersonenkraftwagen()) {
                        insertSingleHeatmapDataIntoLadeZaehldatenHeatmap(
                                ladeZaehldatenHeatmap,
                                heatMapEntryIndex.get(),
                                klassenKategorienIndex.get(),
                                ladeZaehldatum.getPkw(),
                                ChartLegendUtil.PKW_HEATMAP);
                        klassenKategorienIndex.getAndIncrement();
                    }
                    ladeZaehldatenHeatmap.setXAxisDataFirstChart(
                            ZaehldatenProcessingUtil.checkAndAddToXAxisWhenNotAvailable(
                                    ladeZaehldatenHeatmap.getXAxisDataFirstChart(),
                                    ZaehldatenProcessingUtil.getStartUhrzeit(ladeZaehldatum)
                            )
                    );
                    heatMapEntryIndex.incrementAndGet();
                });
        splitSeriesEntriesIntoFirstChartAndSecondChartIfNecessaryInLadeZaehldatenHeatmap(ladeZaehldatenHeatmap, options);
        return ladeZaehldatenHeatmap;
    }

    /**
     * Falls sich in den options die Werte {@link Zeitblock#ZB_00_24} und {@link Zaehldauer#DAUER_2_X_4_STUNDEN}
     * befinden, wird das Diagramm in zwei Unterdiagramme aufgeteilt.
     * Die Aufteilung der Daten für die beiden Unterdiagramme wird in der mitte der X-Achse
     * des Gesamtdiagramms vorgenommen.
     *
     * @param ladeZaehldatenHeatmap Die für das Diagramm aufbereitete Daten.
     *                              Die unterteilung in Unterdiagramme ist noch nicht durchgeführt.
     * @param options               Die {@link OptionsDTO} zur Prüfung auf {@link Zeitblock#ZB_00_24}
     *                              und {@link Zaehldauer#DAUER_2_X_4_STUNDEN}.
     */
    public static void splitSeriesEntriesIntoFirstChartAndSecondChartIfNecessaryInLadeZaehldatenHeatmap(
            final LadeZaehldatenHeatmapDTO ladeZaehldatenHeatmap,
            final OptionsDTO options) {

        if (options.getZeitblock().equals(Zeitblock.ZB_00_24)
                && options.getZaehldauer().equals(Zaehldauer.DAUER_2_X_4_STUNDEN)
                && !(StringUtils.equals(options.getZeitauswahl(), LadeZaehldatenService.ZEITAUSWAHL_SPITZENSTUNDE_KFZ)
                || StringUtils.equals(options.getZeitauswahl(), LadeZaehldatenService.ZEITAUSWAHL_SPITZENSTUNDE_RAD)
                || StringUtils.equals(options.getZeitauswahl(), LadeZaehldatenService.ZEITAUSWAHL_SPITZENSTUNDE_FUSS))) {

            // Split X axis data
            int splittedSize = ladeZaehldatenHeatmap.getXAxisDataFirstChart().size() / SPLIT_DIVISOR;

            List<List<String>> splittetXAxisData = ListUtils.partition(
                    ladeZaehldatenHeatmap.getXAxisDataFirstChart(),
                    splittedSize
            );
            ladeZaehldatenHeatmap.setXAxisDataFirstChart(new ArrayList<>(splittetXAxisData.get(0)));
            ladeZaehldatenHeatmap.setXAxisDataSecondChart(new ArrayList<>(splittetXAxisData.get(1)));

            // Split SeriesEntries (Y Axis) data
            splittedSize = ladeZaehldatenHeatmap.getSeriesEntriesFirstChart().size() / SPLIT_DIVISOR;

            List<List<List<Integer>>> splittedSeriesEntriesData = ListUtils.partition(
                    ladeZaehldatenHeatmap.getSeriesEntriesFirstChart(),
                    splittedSize
            );

            // Der Wert für die X-Achse muss für den zweiten Graph zurück gesetzt werden.
            // Wenn seriesEntriesFirst- und -SecondChart nicht gleich lang sind muss ein Datenfehler vorliegen
            final List<List<Integer>> seriesEntriesFirstChart = new ArrayList<>(splittedSeriesEntriesData.get(0));
            final List<List<Integer>> seriesEntriesSecondChart = new ArrayList<>(splittedSeriesEntriesData.get(1));
            for (int i = 0; (i < seriesEntriesFirstChart.size() && i < seriesEntriesSecondChart.size()); i++) {
                seriesEntriesSecondChart.get(i).set(0, seriesEntriesFirstChart.get(i).get(0));
            }

            ladeZaehldatenHeatmap.setSeriesEntriesFirstChart(seriesEntriesFirstChart);
            ladeZaehldatenHeatmap.setSeriesEntriesSecondChart(seriesEntriesSecondChart);
        }
    }
}