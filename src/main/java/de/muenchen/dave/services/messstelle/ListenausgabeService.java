/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.services.messstelle;

import de.muenchen.dave.domain.dtos.laden.messwerte.LadeMesswerteDTO;
import de.muenchen.dave.domain.dtos.laden.messwerte.LadeMesswerteListenausgabeDTO;
import de.muenchen.dave.geodateneai.gen.model.MeasurementValuesPerInterval;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class ListenausgabeService {

    private static final Integer ZERO = 0;
    private static final Integer ONE = 1;

    // Refactoring: Synergieeffekt mit ProcessZaehldatenSteplineService nutzen
    public LadeMesswerteListenausgabeDTO ladeListenausgabe(final List<MeasurementValuesPerInterval> intervalle) {
        log.debug("#ladeListenausgabe");
        final LadeMesswerteListenausgabeDTO dto = new LadeMesswerteListenausgabeDTO();
        dto.getZaehldaten().addAll(calculateIntervalls(intervalle));
        dto.getZaehldaten().addAll(calculateBlocksAndHours(intervalle));
        // TODO Spitzenstunde berechnen

        dto.setZaehldaten(dto.getZaehldaten().stream().sorted(Comparator.comparing(LadeMesswerteDTO::getSortingIndex)).collect(Collectors.toList()));
        return dto;
    }

    protected List<LadeMesswerteDTO> calculateBlocksAndHours(final List<MeasurementValuesPerInterval> intervalle) {
        // TODO Berechnung in 2 Methoden Trennen
        final List<LadeMesswerteDTO> dtos = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            final LocalTime start = LocalTime.of(i, 0);
            final LocalTime end;
            if (i == 23) {
                end = LocalTime.of(i, 59);
            } else {
                end = LocalTime.of(i + 1, 0);
            }
            final LadeMesswerteDTO dto = calculateSum(intervalle.stream().filter(intervall -> isTimeBetween(intervall.getUhrzeitVon(), start, end))
                    .collect(Collectors.toList()));
            dto.setStartUhrzeit(start);
            dto.setEndeUhrzeit(end);
            dto.setType("Stunde");
            dto.setSortingIndex(100 * (i + 1) - 5);
            dtos.add(dto);
        }
        // 00:00 - 06:00
        final AtomicReference<LocalTime> start = new AtomicReference<>(LocalTime.of(0, 0));
        final AtomicReference<LocalTime> end = new AtomicReference<>(LocalTime.of(6, 0));
        final LadeMesswerteDTO block0006 = calculateSum(
                intervalle.stream().filter(intervall -> isTimeBetween(intervall.getUhrzeitVon(), start.get(), end.get()))
                        .collect(Collectors.toList()));
        block0006.setEndeUhrzeit(end.get());
        block0006.setStartUhrzeit(start.get());
        block0006.setType("Block");
        block0006.setSortingIndex(100 * end.get().getHour() - 3);
        dtos.add(block0006);

        // 06:00 - 10:00
        start.set(LocalTime.of(6, 0));
        end.set(LocalTime.of(10, 0));
        final LadeMesswerteDTO block0610 = calculateSum(
                intervalle.stream().filter(intervall -> isTimeBetween(intervall.getUhrzeitVon(), start.get(), end.get()))
                        .collect(Collectors.toList()));
        block0610.setEndeUhrzeit(end.get());
        block0610.setStartUhrzeit(start.get());
        block0610.setType("Block");
        block0610.setSortingIndex(100 * end.get().getHour() - 3);
        dtos.add(block0610);

        // 10:00 - 15:00
        start.set(LocalTime.of(10, 0));
        end.set(LocalTime.of(15, 0));
        final LadeMesswerteDTO block1015 = calculateSum(
                intervalle.stream().filter(intervall -> isTimeBetween(intervall.getUhrzeitVon(), start.get(), end.get()))
                        .collect(Collectors.toList()));
        block1015.setEndeUhrzeit(end.get());
        block1015.setStartUhrzeit(start.get());
        block1015.setType("Block");
        block1015.setSortingIndex(100 * end.get().getHour() - 3);
        dtos.add(block1015);

        // 15:00 - 19:00
        start.set(LocalTime.of(15, 0));
        end.set(LocalTime.of(19, 0));
        final LadeMesswerteDTO block1519 = calculateSum(
                intervalle.stream().filter(intervall -> isTimeBetween(intervall.getUhrzeitVon(), start.get(), end.get()))
                        .collect(Collectors.toList()));
        block1519.setEndeUhrzeit(end.get());
        block1519.setStartUhrzeit(start.get());
        block1519.setType("Block");
        block1519.setSortingIndex(100 * end.get().getHour() - 3);
        dtos.add(block1519);

        // 19:00 - 24:00
        start.set(LocalTime.of(19, 0));
        end.set(LocalTime.of(23, 59));
        final LadeMesswerteDTO block1924 = calculateSum(
                intervalle.stream().filter(intervall -> isTimeBetween(intervall.getUhrzeitVon(), start.get(), end.get()))
                        .collect(Collectors.toList()));
        block1924.setEndeUhrzeit(end.get());
        block1924.setStartUhrzeit(start.get());
        block1924.setType("Block");
        block1924.setSortingIndex(100 * end.get().getHour() - 3);
        dtos.add(block1924);

        // 00:00 - 24:00
        start.set(LocalTime.of(0, 0));
        final LadeMesswerteDTO block0024 = calculateSum(
                intervalle.stream().filter(intervall -> isTimeBetween(intervall.getUhrzeitVon(), start.get(), end.get()))
                        .collect(Collectors.toList()));
        block0024.setEndeUhrzeit(end.get());
        block0024.setStartUhrzeit(start.get());
        block0024.setType("Gesamt");
        block0024.setSortingIndex(2400);
        dtos.add(block0024);

        return dtos;
    }

    protected boolean isTimeBetween(final LocalTime toCheck, final LocalTime start, final LocalTime end) {
        return (toCheck.isAfter(start) || toCheck.equals(start)) && toCheck.isBefore(end);
    }

    protected List<LadeMesswerteDTO> calculateIntervalls(final List<MeasurementValuesPerInterval> intervalle) {
        final List<LadeMesswerteDTO> dtos = new ArrayList<>();
        intervalle.forEach(intervall -> {
            final LadeMesswerteDTO dto = new LadeMesswerteDTO();
            dto.setType("");
            final String hour = String.valueOf(intervall.getUhrzeitVon().getHour());
            String minute = String.valueOf(intervall.getUhrzeitVon().getMinute());
            if (minute.length() == 1) {
                minute += minute;
            }
            int sortingIndex = Integer.parseInt(hour + minute);
            System.err.println(sortingIndex);
            dto.setSortingIndex(sortingIndex);
            dto.setStartUhrzeit(intervall.getUhrzeitVon());
            dto.setEndeUhrzeit(intervall.getUhrzeitBis());
            dto.setPkw(intervall.getSummeAllePkw());
            dto.setLkw(intervall.getAnzahlLkw());
            dto.setLfw(intervall.getAnzahlLfw());
            dto.setLastzuege(intervall.getSummeLastzug());
            dto.setBusse(intervall.getAnzahlBus());
            dto.setKraftraeder(intervall.getAnzahlKrad());
            dto.setFahrradfahrer(intervall.getAnzahlRad());
            dto.setKfz(intervall.getSummeKraftfahrzeugverkehr());
            dto.setSchwerverkehr(intervall.getSummeSchwerverkehr());
            dto.setGueterverkehr(intervall.getSummeGueterverkehr());
            dto.setAnteilSchwerverkehrAnKfzProzent(intervall.getProzentSchwerverkehr());
            dto.setAnteilGueterverkehrAnKfzProzent(intervall.getProzentGueterverkehr());
            dtos.add(dto);
        });
        return dtos;
    }

    protected LadeMesswerteDTO calculateSum(final List<MeasurementValuesPerInterval> intervalle) {
        final LadeMesswerteDTO dto = new LadeMesswerteDTO();
        dto.setPkw(intervalle.stream().mapToInt(MeasurementValuesPerInterval::getSummeAllePkw).sum());
        dto.setLkw(intervalle.stream().mapToInt(MeasurementValuesPerInterval::getAnzahlLkw).sum());
        dto.setLfw(intervalle.stream().mapToInt(MeasurementValuesPerInterval::getAnzahlLfw).sum());
        dto.setLastzuege(intervalle.stream().mapToInt(MeasurementValuesPerInterval::getSummeLastzug).sum());
        dto.setBusse(intervalle.stream().mapToInt(MeasurementValuesPerInterval::getAnzahlBus).sum());
        dto.setKraftraeder(intervalle.stream().mapToInt(MeasurementValuesPerInterval::getAnzahlKrad).sum());
        dto.setFahrradfahrer(intervalle.stream().mapToInt(MeasurementValuesPerInterval::getAnzahlRad).sum());
        //        dto.setFussgaenger();
        dto.setKfz(intervalle.stream().mapToInt(MeasurementValuesPerInterval::getSummeKraftfahrzeugverkehr).sum());
        dto.setSchwerverkehr(intervalle.stream().mapToInt(MeasurementValuesPerInterval::getSummeSchwerverkehr).sum());
        dto.setGueterverkehr(intervalle.stream().mapToInt(MeasurementValuesPerInterval::getSummeGueterverkehr).sum());
        dto.setAnteilSchwerverkehrAnKfzProzent(calculateAnteilProzent(dto.getSchwerverkehr(), dto.getKfz()));
        dto.setAnteilGueterverkehrAnKfzProzent(calculateAnteilProzent(dto.getGueterverkehr(), dto.getKfz()));
        return dto;
    }

    protected Double calculateAnteilProzent(final Integer dividend, final Integer divisor) {
        final Double percentage = (Double.valueOf(dividend) / divisor) * 100;
        return BigDecimal.valueOf(percentage).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }
}
