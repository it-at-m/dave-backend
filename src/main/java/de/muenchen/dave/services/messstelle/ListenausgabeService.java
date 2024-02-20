/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.services.messstelle;

import de.muenchen.dave.domain.dtos.laden.LadeListenausgabeMessstelleDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatenTableDTO;
import de.muenchen.dave.geodateneai.gen.model.MeasurementValuesPerInterval;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class ListenausgabeService {

    private static final Integer ZERO = 0;
    private static final Integer ONE = 1;

    // Refactoring: Synergieeffekt mit ProcessZaehldatenSteplineService nutzen
    public LadeZaehldatenTableDTO ladeListenausgabe(final List<MeasurementValuesPerInterval> intervalle) {
        log.debug("#ladeListenausgabe");
        final LadeZaehldatenTableDTO ladeZaehldatenTableDTO = new LadeZaehldatenTableDTO();
        // TODO Stunden, Blöcke und Spitzenstunde berechnen
//        berechneGleitendeSpitzenstunde(zaehlungId.get(), Zeitblock.ZB_00_06, fahrbeziehung, zeitintervalleForFahrbeziehung)
//                .setGleitendeSpstdKfzRadFussToSpitzenstundeList(gleitendeSpitzenstunden);
//        berechneGleitendeSpitzenstunde(zaehlungId.get(), Zeitblock.ZB_06_10, fahrbeziehung, zeitintervalleForFahrbeziehung)
//                .setGleitendeSpstdKfzRadFussToSpitzenstundeList(gleitendeSpitzenstunden);
//        berechneGleitendeSpitzenstunde(zaehlungId.get(), Zeitblock.ZB_10_15, fahrbeziehung, zeitintervalleForFahrbeziehung)
//                .setGleitendeSpstdKfzRadFussToSpitzenstundeList(gleitendeSpitzenstunden);
//        berechneGleitendeSpitzenstunde(zaehlungId.get(), Zeitblock.ZB_15_19, fahrbeziehung, zeitintervalleForFahrbeziehung)
//                .setGleitendeSpstdKfzRadFussToSpitzenstundeList(gleitendeSpitzenstunden);
//        berechneGleitendeSpitzenstunde(zaehlungId.get(), Zeitblock.ZB_19_24, fahrbeziehung, zeitintervalleForFahrbeziehung)
//                .setGleitendeSpstdKfzRadFussToSpitzenstundeList(gleitendeSpitzenstunden);
//        berechneGleitendeSpitzenstunde(zaehlungId.get(), Zeitblock.ZB_00_24, fahrbeziehung, zeitintervalleForFahrbeziehung)
//                .setGleitendeSpstdKfzRadFussToSpitzenstundeList(gleitendeSpitzenstunden);
        return ladeZaehldatenTableDTO;
    }

    protected List<LadeListenausgabeMessstelleDTO> calculateSumOfBlocks(final List<MeasurementValuesPerInterval> intervalle) {
        final List<LadeListenausgabeMessstelleDTO> dtos = new ArrayList<>();
        final String block = "Block";

        // 0000 - 0600
        final LadeListenausgabeMessstelleDTO block0006 = calculateSum(intervalle.subList(0, 4 * 6));
        block0006.setType(block);
        block0006.setStartUhrzeit("00:00");
        block0006.setEndeUhrzeit("06:00");
        dtos.add(block0006);
        // 0600 - 1000
        final LadeListenausgabeMessstelleDTO block0610 = calculateSum(intervalle.subList(4 * 6, 4 * 10));
        block0610.setType(block);
        block0610.setStartUhrzeit("06:00");
        block0610.setEndeUhrzeit("10:00");
        dtos.add(block0610);
        // 1000 - 1500
        final LadeListenausgabeMessstelleDTO block1015 = calculateSum(intervalle.subList(4 * 10, 4 * 15));
        block1015.setType(block);
        block1015.setStartUhrzeit("10:00");
        block1015.setEndeUhrzeit("15:00");
        dtos.add(block1015);
        // 1500 - 1900
        final LadeListenausgabeMessstelleDTO block1519 = calculateSum(intervalle.subList(4 * 15, 4 * 19));
        block1519.setType(block);
        block1519.setStartUhrzeit("15:00");
        block1519.setEndeUhrzeit("19:00");
        dtos.add(block1519);
        // 1900 - 2400
        final LadeListenausgabeMessstelleDTO block1924 = calculateSum(intervalle.subList(4 * 19, 4 * 24));
        block1924.setType(block);
        block1924.setStartUhrzeit("19:00");
        block1924.setEndeUhrzeit("24:00");
        dtos.add(block1924);
        // 0000 - 2400
        final LadeListenausgabeMessstelleDTO block0024 = calculateSum(intervalle.subList(0, 4 * 24));
        block0024.setType(block);
        block0024.setStartUhrzeit("00:00");
        block0024.setEndeUhrzeit("24:00");
        dtos.add(block0024);
        return dtos;
    }

    protected List<LadeListenausgabeMessstelleDTO> calculateSumOfHours(final List<MeasurementValuesPerInterval> intervalle) {
        final List<LadeListenausgabeMessstelleDTO> dtos = new ArrayList<>();
        final String stunde = "Stunde";
        for(int index = 0; index < intervalle.size(); index = index+4) {
            final LadeListenausgabeMessstelleDTO dto = calculateSum(intervalle.subList(index, index + 4));
            dto.setType(stunde);
            dto.setStartUhrzeit(String.format("%s:00", (index/4) < 10 ? "0"+index : index));
            dto.setEndeUhrzeit(String.format("%s:00", ((index+4)/4) < 10 ? "0"+index : index));
            dtos.add(dto);
        }
        return dtos;
    }

    protected LadeListenausgabeMessstelleDTO calculateSum(final List<MeasurementValuesPerInterval> intervalle) {
        final LadeListenausgabeMessstelleDTO dto = new LadeListenausgabeMessstelleDTO();
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
