/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.services.messstelle;

import de.muenchen.dave.domain.dtos.laden.LadeZaehldatenHeatmapDTO;
import de.muenchen.dave.domain.dtos.messstelle.FahrzeugOptionsDTO;
import de.muenchen.dave.domain.dtos.messstelle.MessstelleOptionsDTO;
import de.muenchen.dave.geodateneai.gen.model.IntervalDto;
import de.muenchen.dave.util.ChartLegendUtil;
import de.muenchen.dave.util.ZaehldatenProcessingUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
@RequiredArgsConstructor
public class HeatmapService {

    /**
     * Diese Methode fügt einen einzelnen in der Heatmap darzustellenden Wert in das im Parameter
     * ladeZaehldatenHeatmap übergebenen Objekt ein. Des Weiteren
     * wird ein Legendeneintrag für den Parameter legendEntry gesetzt, falls dieser noch nicht vorhanden
     * ist. Zusätzlich werden die Variablen RangeMin und
     * RangeMax gesetzt.
     *
     * @param ladeZaehldatenHeatmap Das Objekt in welchem die aufbereiteten Daten vorgehalten werden.
     * @param heatMapEntryIndex Spaltenindex der X-Achse zur Positionierung des Wertes aus Parameter
     *            value in Heatmap.
     * @param klassenKategorienIndex Zeilenindex der Y-Achse zur Positionierung des Wertes aus Parameter
     *            value in Heatmap.
     * @param value Der Wert welcher an der Position, definiert durch Spaltenindex und Zeilenindex, in
     *            der Heatmap dargestellt werde soll. Des
     *            Weiteren wird dieser Wert zur Ermittlung von
     *            {@link LadeZaehldatenHeatmapDTO}#getRangeMax() und
     *            {@link LadeZaehldatenHeatmapDTO}#getRangeMin() herangezogen.
     * @param legendEntry Der Legendeneintrag welcher in {@link LadeZaehldatenHeatmapDTO}#getLegend()
     *            hinterlegt wird.
     */
    protected static void insertSingleHeatmapDataIntoLadeZaehldatenHeatmap(
            final LadeZaehldatenHeatmapDTO ladeZaehldatenHeatmap,
            final int heatMapEntryIndex,
            final int klassenKategorienIndex,
            final Integer value,
            final String legendEntry) {
        ladeZaehldatenHeatmap.setLegend(
                ChartLegendUtil.checkAndAddToLegendWhenNotAvailable(
                        ladeZaehldatenHeatmap.getLegend(),
                        legendEntry));
        final int nullCheckedValue = ObjectUtils.defaultIfNull(value, 0);
        final int currentRangeMin = ObjectUtils.defaultIfNull(ladeZaehldatenHeatmap.getRangeMin(), 0);
        ladeZaehldatenHeatmap.setRangeMin(Math.min(nullCheckedValue, currentRangeMin));
        final int currentRangeMax = ObjectUtils.defaultIfNull(ladeZaehldatenHeatmap.getRangeMax(), 0);
        ladeZaehldatenHeatmap.setRangeMax(Math.max(nullCheckedValue, currentRangeMax));
        ladeZaehldatenHeatmap.getSeriesEntriesFirstChart().add(
                createHeatMapEntry(
                        heatMapEntryIndex,
                        klassenKategorienIndex,
                        value));
    }

    /**
     * Erstellt einen einzelnen Eintrag in der Heatmap.
     *
     * @param heatMapEntryIndex Der Spaltenindex in der Heatmap
     * @param klassenKategorienIndex Der Zeilenindex in der Heatmap
     * @param value Der Wert im entsprechenden Heatmapfeld definiert durch Spaltenindex und Zeilenindex.
     * @return Eine Liste bestehend aus Spaltenindex, Zeilenindex und dem Wert.
     */
    protected static List<Integer> createHeatMapEntry(
            final int heatMapEntryIndex,
            final int klassenKategorienIndex,
            final Integer value) {
        final var heatmapEntry = new ArrayList<Integer>();
        heatmapEntry.add(heatMapEntryIndex);
        heatmapEntry.add(klassenKategorienIndex);
        heatmapEntry.add(value);
        return heatmapEntry;
    }

    /**
     * Diese Methode führt die Datenaufbereitung für das Heatmap-Diagramm durch. Als Basis zur
     * Datenaufbereitung dienen die im Parameter zaehldatenTable
     * übergebenen Informationen. Die in den options gewählten Fahrzeugklassen bzw. Fahrzeugkategorien
     * werden in dieser Methode zur Darstellung in der Heatmap
     * aufbereitet.
     *
     * @param intervals Die Datengrundlage zur Aufbereitung des Heatmap-Diagramms.
     * @return Die aufbreiteten Daten für das Heatmap-Diagramm entsprechend der gewählten Optionen.
     */
    public LadeZaehldatenHeatmapDTO ladeHeatmap(final List<IntervalDto> intervals, final MessstelleOptionsDTO options) {
        final LadeZaehldatenHeatmapDTO ladeZaehldatenHeatmap = new LadeZaehldatenHeatmapDTO();
        ladeZaehldatenHeatmap.setRangeMin(0);
        ladeZaehldatenHeatmap.setRangeMax(0);
        ladeZaehldatenHeatmap.setLegend(new ArrayList<>());
        ladeZaehldatenHeatmap.setXAxisDataFirstChart(new ArrayList<>());
        ladeZaehldatenHeatmap.setSeriesEntriesFirstChart(new ArrayList<>());
        final AtomicInteger heatMapEntryIndex = new AtomicInteger(0);

        final FahrzeugOptionsDTO fahrzeugOptions = options.getFahrzeuge();

        intervals.forEach(intervall -> {
            final AtomicInteger klassenKategorienIndex = new AtomicInteger(0);

            if (fahrzeugOptions.isGueterverkehr()) {
                insertSingleHeatmapDataIntoLadeZaehldatenHeatmap(
                        ladeZaehldatenHeatmap,
                        heatMapEntryIndex.get(),
                        klassenKategorienIndex.getAndIncrement(),
                        Objects.isNull(intervall.getSummeGueterverkehr()) ? null : intervall.getSummeGueterverkehr().intValue(),
                        ChartLegendUtil.GUETERVERKEHR_HEATMAP);
            }
            if (fahrzeugOptions.isSchwerverkehr()) {
                insertSingleHeatmapDataIntoLadeZaehldatenHeatmap(
                        ladeZaehldatenHeatmap,
                        heatMapEntryIndex.get(),
                        klassenKategorienIndex.getAndIncrement(),
                        Objects.isNull(intervall.getSummeSchwerverkehr()) ? null : intervall.getSummeSchwerverkehr().intValue(),
                        ChartLegendUtil.SCHWERVERKEHR_HEATMAP);
            }
            if (fahrzeugOptions.isKraftfahrzeugverkehr()) {
                insertSingleHeatmapDataIntoLadeZaehldatenHeatmap(
                        ladeZaehldatenHeatmap,
                        heatMapEntryIndex.get(),
                        klassenKategorienIndex.getAndIncrement(),
                        Objects.isNull(intervall.getSummeKraftfahrzeugverkehr()) ? null : intervall.getSummeKraftfahrzeugverkehr().intValue(),
                        ChartLegendUtil.KFZ_HEATMAP);
            }
            if (fahrzeugOptions.isRadverkehr()) {
                insertSingleHeatmapDataIntoLadeZaehldatenHeatmap(
                        ladeZaehldatenHeatmap,
                        heatMapEntryIndex.get(),
                        klassenKategorienIndex.getAndIncrement(),
                        Objects.isNull(intervall.getAnzahlRad()) ? null : intervall.getAnzahlRad().intValue(),
                        ChartLegendUtil.RAD_HEATMAP);
            }
            if (fahrzeugOptions.isKraftraeder()) {
                insertSingleHeatmapDataIntoLadeZaehldatenHeatmap(
                        ladeZaehldatenHeatmap,
                        heatMapEntryIndex.get(),
                        klassenKategorienIndex.getAndIncrement(),
                        Objects.isNull(intervall.getAnzahlKrad()) ? null : intervall.getAnzahlKrad().intValue(),
                        ChartLegendUtil.KRAFTRAEDER_HEATMAP);
            }
            if (fahrzeugOptions.isBusse()) {
                insertSingleHeatmapDataIntoLadeZaehldatenHeatmap(
                        ladeZaehldatenHeatmap,
                        heatMapEntryIndex.get(),
                        klassenKategorienIndex.getAndIncrement(),
                        Objects.isNull(intervall.getAnzahlBus()) ? null : intervall.getAnzahlBus().intValue(),
                        ChartLegendUtil.BUSSE_HEATMAP);
            }
            if (fahrzeugOptions.isLieferwagen()) {
                insertSingleHeatmapDataIntoLadeZaehldatenHeatmap(
                        ladeZaehldatenHeatmap,
                        heatMapEntryIndex.get(),
                        klassenKategorienIndex.getAndIncrement(),
                        Objects.isNull(intervall.getAnzahlLfw()) ? null : intervall.getAnzahlLfw().intValue(),
                        ChartLegendUtil.LFW_HEATMAP);
            }
            if (fahrzeugOptions.isLastzuege()) {
                insertSingleHeatmapDataIntoLadeZaehldatenHeatmap(
                        ladeZaehldatenHeatmap,
                        heatMapEntryIndex.get(),
                        klassenKategorienIndex.getAndIncrement(),
                        Objects.isNull(intervall.getSummeLastzug()) ? null : intervall.getSummeLastzug().intValue(),
                        ChartLegendUtil.LASTZUEGE_HEATMAP);
            }
            if (fahrzeugOptions.isLastkraftwagen()) {
                insertSingleHeatmapDataIntoLadeZaehldatenHeatmap(
                        ladeZaehldatenHeatmap,
                        heatMapEntryIndex.get(),
                        klassenKategorienIndex.getAndIncrement(),
                        Objects.isNull(intervall.getAnzahlLkw()) ? null : intervall.getAnzahlLkw().intValue(),
                        ChartLegendUtil.LKW_HEATMAP);
            }
            if (fahrzeugOptions.isPersonenkraftwagen()) {
                insertSingleHeatmapDataIntoLadeZaehldatenHeatmap(
                        ladeZaehldatenHeatmap,
                        heatMapEntryIndex.get(),
                        klassenKategorienIndex.getAndIncrement(),
                        Objects.isNull(intervall.getSummeAllePkw()) ? null : intervall.getSummeAllePkw().intValue(),
                        ChartLegendUtil.PKW_HEATMAP);
            }
            ladeZaehldatenHeatmap.setXAxisDataFirstChart(
                    ZaehldatenProcessingUtil.checkAndAddToXAxisWhenNotAvailable(
                            ladeZaehldatenHeatmap.getXAxisDataFirstChart(),
                            intervall.getDatumUhrzeitVon().toLocalTime().toString()));
            heatMapEntryIndex.incrementAndGet();
        });
        return ladeZaehldatenHeatmap;
    }
}
