/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.services.messstelle;

import de.muenchen.dave.domain.dtos.laden.LadeZaehldatenHeatmapDTO;
import de.muenchen.dave.geodateneai.gen.model.MeasurementValuesPerInterval;
import de.muenchen.dave.util.ChartLegendUtil;
import de.muenchen.dave.util.ZaehldatenProcessingUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
@AllArgsConstructor
public class HeatmapService {

    /**
     * Diese Methode führt die Datenaufbereitung für das Heatmap-Diagramm durch.
     * Als Basis zur Datenaufbereitung dienen die im Parameter zaehldatenTable
     * übergebenen Informationen.
     * Die in den options gewählten Fahrzeugklassen bzw. Fahrzeugkategorien
     * werden in dieser Methode zur Darstellung in der Heatmap aufbereitet.
     *
     * @param intervalle Die Datengrundlage zur Aufbereitung des Heatmap-Diagramms.
     * @return Die aufbreiteten Daten für das Heatmap-Diagramm entsprechend der gewählten Optionen.
     */
    public LadeZaehldatenHeatmapDTO ladeProcessedMessdatenHeatmap(final List<MeasurementValuesPerInterval> intervalle) {
        final LadeZaehldatenHeatmapDTO ladeZaehldatenHeatmap = new LadeZaehldatenHeatmapDTO();
        ladeZaehldatenHeatmap.setRangeMin(0);
        ladeZaehldatenHeatmap.setRangeMax(0);
        ladeZaehldatenHeatmap.setLegend(new ArrayList<>());
        ladeZaehldatenHeatmap.setXAxisDataFirstChart(new ArrayList<>());
        ladeZaehldatenHeatmap.setSeriesEntriesFirstChart(new ArrayList<>());
        AtomicInteger heatMapEntryIndex = new AtomicInteger(0);

        intervalle.forEach(intervall -> {
            final AtomicInteger klassenKategorienIndex = new AtomicInteger(0);

            insertSingleHeatmapDataIntoLadeZaehldatenHeatmap(
                    ladeZaehldatenHeatmap,
                    heatMapEntryIndex.get(),
                    klassenKategorienIndex.get(),
                    ZaehldatenProcessingUtil.nullsafeCast(intervall.getSummeKraftfahrzeugverkehr()),
                    ChartLegendUtil.KFZ_HEATMAP);
            klassenKategorienIndex.getAndIncrement();

            insertSingleHeatmapDataIntoLadeZaehldatenHeatmap(
                    ladeZaehldatenHeatmap,
                    heatMapEntryIndex.get(),
                    klassenKategorienIndex.get(),
                    ZaehldatenProcessingUtil.nullsafeCast(intervall.getSummeGueterverkehr()),
                    ChartLegendUtil.GUETERVERKEHR_HEATMAP);
            klassenKategorienIndex.getAndIncrement();

            insertSingleHeatmapDataIntoLadeZaehldatenHeatmap(
                    ladeZaehldatenHeatmap,
                    heatMapEntryIndex.get(),
                    klassenKategorienIndex.get(),
                    ZaehldatenProcessingUtil.nullsafeCast(intervall.getSummeSchwerverkehr()),
                    ChartLegendUtil.SCHWERVERKEHR_HEATMAP);
            klassenKategorienIndex.getAndIncrement();

            insertSingleHeatmapDataIntoLadeZaehldatenHeatmap(
                    ladeZaehldatenHeatmap,
                    heatMapEntryIndex.get(),
                    klassenKategorienIndex.get(),
                    ZaehldatenProcessingUtil.nullsafeCast(intervall.getAnzahlRad()),
                    ChartLegendUtil.RAD_HEATMAP);
            klassenKategorienIndex.getAndIncrement();

            insertSingleHeatmapDataIntoLadeZaehldatenHeatmap(
                    ladeZaehldatenHeatmap,
                    heatMapEntryIndex.get(),
                    klassenKategorienIndex.get(),
                    ZaehldatenProcessingUtil.nullsafeCast(intervall.getAnzahlKrad()),
                    ChartLegendUtil.KRAFTRAEDER_HEATMAP);
            klassenKategorienIndex.getAndIncrement();

            insertSingleHeatmapDataIntoLadeZaehldatenHeatmap(
                    ladeZaehldatenHeatmap,
                    heatMapEntryIndex.get(),
                    klassenKategorienIndex.get(),
                    ZaehldatenProcessingUtil.nullsafeCast(intervall.getAnzahlBus()),
                    ChartLegendUtil.BUSSE_HEATMAP);
            klassenKategorienIndex.getAndIncrement();

            insertSingleHeatmapDataIntoLadeZaehldatenHeatmap(
                    ladeZaehldatenHeatmap,
                    heatMapEntryIndex.get(),
                    klassenKategorienIndex.get(),
                    ZaehldatenProcessingUtil.nullsafeCast(intervall.getSummeLastzug()),
                    ChartLegendUtil.LASTZUEGE_HEATMAP);
            klassenKategorienIndex.getAndIncrement();

            insertSingleHeatmapDataIntoLadeZaehldatenHeatmap(
                    ladeZaehldatenHeatmap,
                    heatMapEntryIndex.get(),
                    klassenKategorienIndex.get(),
                    ZaehldatenProcessingUtil.nullsafeCast(intervall.getAnzahlLkw()),
                    ChartLegendUtil.LKW_HEATMAP);
            klassenKategorienIndex.getAndIncrement();

            insertSingleHeatmapDataIntoLadeZaehldatenHeatmap(
                    ladeZaehldatenHeatmap,
                    heatMapEntryIndex.get(),
                    klassenKategorienIndex.get(),
                    ZaehldatenProcessingUtil.nullsafeCast(intervall.getAnzahlLfw()),
                    ChartLegendUtil.LFW_HEATMAP);
            klassenKategorienIndex.getAndIncrement();

            insertSingleHeatmapDataIntoLadeZaehldatenHeatmap(
                    ladeZaehldatenHeatmap,
                    heatMapEntryIndex.get(),
                    klassenKategorienIndex.get(),
                    ZaehldatenProcessingUtil.nullsafeCast(intervall.getSummeAllePkw()),
                    ChartLegendUtil.PKW_HEATMAP);
            klassenKategorienIndex.getAndIncrement();

            ladeZaehldatenHeatmap.setXAxisDataFirstChart(
                    ZaehldatenProcessingUtil.checkAndAddToXAxisWhenNotAvailable(
                            ladeZaehldatenHeatmap.getXAxisDataFirstChart(),
                            intervall.getUhrzeitVon().toString()));
            heatMapEntryIndex.incrementAndGet();
        });

        // TODO wenn die Fußgänger schon vorgesehen werden sollen, dann muss das auch in der EAI gemacht werden

        return ladeZaehldatenHeatmap;
    }

    /**
     * Diese Methode fügt einen einzelnen in der Heatmap darzustellenden Wert in
     * das im Parameter ladeZaehldatenHeatmap übergebenen Objekt ein.
     * Des Weiteren wird ein Legendeneintrag für den Parameter legendEntry gesetzt,
     * falls dieser noch nicht vorhanden ist.
     * Zusätzlich werden die Variablen RangeMin und RangeMax gesetzt.
     *
     * @param ladeZaehldatenHeatmap Das Objekt in welchem die aufbereiteten Daten vorgehalten werden.
     * @param heatMapEntryIndex Spaltenindex der X-Achse zur Positionierung des Wertes aus Parameter
     *            value in Heatmap.
     * @param klassenKategorienIndex Zeilenindex der Y-Achse zur Positionierung des Wertes aus Parameter
     *            value in Heatmap.
     * @param value Der Wert welcher an der Position, definiert durch Spaltenindex und Zeilenindex,
     *            in der Heatmap dargestellt werde soll. Des Weiteren wird dieser Wert zur Ermittlung
     *            von
     *            {@link LadeZaehldatenHeatmapDTO}#getRangeMax() und
     *            {@link LadeZaehldatenHeatmapDTO}#getRangeMin() herangezogen.
     * @param legendEntry Der Legendeneintrag welcher in {@link LadeZaehldatenHeatmapDTO}#getLegend()
     *            hinterlegt wird.
     */
    protected void insertSingleHeatmapDataIntoLadeZaehldatenHeatmap(final LadeZaehldatenHeatmapDTO ladeZaehldatenHeatmap,
            final int heatMapEntryIndex,
            final int klassenKategorienIndex,
            final Integer value,
            final String legendEntry) {
        ladeZaehldatenHeatmap.setLegend(
                ChartLegendUtil.checkAndAddToLegendWhenNotAvailable(
                        ladeZaehldatenHeatmap.getLegend(),
                        legendEntry));
        final int nullCheckedValue = ObjectUtils.defaultIfNull(value, 0);
        ladeZaehldatenHeatmap.setRangeMin(
                Math.min(nullCheckedValue, ladeZaehldatenHeatmap.getRangeMin()));
        ladeZaehldatenHeatmap.setRangeMax(
                Math.max(nullCheckedValue, ladeZaehldatenHeatmap.getRangeMax()));
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
    protected List<Integer> createHeatMapEntry(final int heatMapEntryIndex,
            final int klassenKategorienIndex,
            final Integer value) {
        final List<Integer> heatmapEntry = new ArrayList<>();
        heatmapEntry.add(heatMapEntryIndex);
        heatmapEntry.add(klassenKategorienIndex);
        heatmapEntry.add(value);
        return heatmapEntry;
    }
}
