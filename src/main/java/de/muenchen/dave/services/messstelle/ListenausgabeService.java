/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.services.messstelle;

import de.muenchen.dave.domain.dtos.laden.messwerte.LadeMesswerteDTO;
import de.muenchen.dave.domain.dtos.laden.messwerte.LadeMesswerteListenausgabeDTO;
import de.muenchen.dave.domain.enums.TypeZeitintervall;
import de.muenchen.dave.domain.enums.Zeitblock;
import de.muenchen.dave.domain.mapper.LadeMesswerteMapper;
import de.muenchen.dave.geodateneai.gen.model.MeasurementValuesPerInterval;
import de.muenchen.dave.util.messstelle.MesswerteBaseUtil;
import de.muenchen.dave.util.messstelle.MesswerteSortingIndexUtil;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class ListenausgabeService {

    private final LadeMesswerteMapper ladeMesswerteMapper;

    protected static final String GESAMT = "Gesamt";
    protected static final String BLOCK = "Block";
    protected static final String STUNDE = "Stunde";
    protected static final String SPITZENSTUNDE_TAG = "SpStdTag";
    protected static final String SPITZENSTUNDE_TAG_KFZ = SPITZENSTUNDE_TAG + " KFZ";
    protected static final String SPITZENSTUNDE_TAG_RAD = SPITZENSTUNDE_TAG + " Rad";
    protected static final String SPITZENSTUNDE_BLOCK = "SpStdBlock";
    protected static final String SPITZENSTUNDE_BLOCK_KFZ = SPITZENSTUNDE_BLOCK + " KFZ";
    protected static final String SPITZENSTUNDE_BLOCK_RAD = SPITZENSTUNDE_BLOCK + " Rad";

    public LadeMesswerteListenausgabeDTO ladeListenausgabe(final List<MeasurementValuesPerInterval> intervals) {
        log.debug("#ladeListenausgabe");
        final LadeMesswerteListenausgabeDTO dto = new LadeMesswerteListenausgabeDTO();
        dto.getZaehldaten().addAll(calculateIntervalls(intervals));
        dto.getZaehldaten().addAll(calculateHours(intervals));
        dto.getZaehldaten().addAll(calculateDataPerBlock(Zeitblock.ZB_00_06, intervals));
        dto.getZaehldaten().addAll(calculateDataPerBlock(Zeitblock.ZB_06_10, intervals));
        dto.getZaehldaten().addAll(calculateDataPerBlock(Zeitblock.ZB_10_15, intervals));
        dto.getZaehldaten().addAll(calculateDataPerBlock(Zeitblock.ZB_15_19, intervals));
        dto.getZaehldaten().addAll(calculateDataPerBlock(Zeitblock.ZB_19_24, intervals));
        dto.getZaehldaten().addAll(calculateDataPerBlock(Zeitblock.ZB_00_24, intervals));
        dto.setZaehldaten(dto.getZaehldaten().stream().sorted(Comparator.comparing(LadeMesswerteDTO::getSortingIndex)).collect(Collectors.toList()));
        return dto;
    }

    protected List<LadeMesswerteDTO> calculateIntervalls(final List<MeasurementValuesPerInterval> intervals) {
        final List<LadeMesswerteDTO> dtos = new ArrayList<>();
        intervals.forEach(intervall -> {
            final LadeMesswerteDTO dto = ladeMesswerteMapper.measurementValuesPerIntervalToLadeMesswerteDTO(intervall);
            dto.setSortingIndex(MesswerteSortingIndexUtil.getSortingIndexWithinBlock(dto, TypeZeitintervall.STUNDE_VIERTEL));
            dtos.add(dto);
        });
        return dtos;
    }

    protected List<LadeMesswerteDTO> calculateHours(final List<MeasurementValuesPerInterval> intervals) {
        final List<LadeMesswerteDTO> dtos = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            final LocalTime start = LocalTime.of(i, 0);
            final LocalTime end;
            if (i == 23) {
                end = LocalTime.of(i, 59);
            } else {
                end = LocalTime.of(i + 1, 0);
            }
            final LadeMesswerteDTO dto = calculateSum(
                    intervals.stream().filter(intervall -> MesswerteBaseUtil.isTimeBetweenStartAndEnd(intervall.getStartUhrzeit(), start, end))
                            .collect(Collectors.toList()));
            dto.setStartUhrzeit(start);
            dto.setEndeUhrzeit(end);
            dto.setType(STUNDE);
            dto.setSortingIndex(MesswerteSortingIndexUtil.getSortingIndexWithinBlock(dto, TypeZeitintervall.STUNDE_KOMPLETT));
            dtos.add(dto);
        }
        return dtos;
    }

    protected List<LadeMesswerteDTO> calculateDataPerBlock(final Zeitblock block, final List<MeasurementValuesPerInterval> intervals) {
        final List<MeasurementValuesPerInterval> necessaryIntervals = intervals.stream()
                .filter(intervall -> MesswerteBaseUtil.isTimeWithinBlock(intervall.getStartUhrzeit(), block))
                .collect(Collectors.toList());
        final List<LadeMesswerteDTO> ladeMesswerteDTOS = new ArrayList<>(calculateSpitzenstunde(block, necessaryIntervals));
        ladeMesswerteDTOS.add(calculateblock(block, necessaryIntervals));
        return ladeMesswerteDTOS;
    }

    protected List<LadeMesswerteDTO> calculateSpitzenstunde(final Zeitblock block, final List<MeasurementValuesPerInterval> intervals) {
        LadeMesswerteDTO maxKfz = new LadeMesswerteDTO();
        LadeMesswerteDTO maxRad = new LadeMesswerteDTO();
        for (int index = 0; index + 3 < intervals.size(); index++) {
            final MeasurementValuesPerInterval i0 = intervals.get(index);
            final MeasurementValuesPerInterval i1 = intervals.get(index + 1);
            final MeasurementValuesPerInterval i2 = intervals.get(index + 2);
            final MeasurementValuesPerInterval i3 = intervals.get(index + 3);
            final LadeMesswerteDTO ladeMesswerteDTO = calculateSum(List.of(i0, i1, i2, i3));
            ladeMesswerteDTO.setStartUhrzeit(i0.getStartUhrzeit());
            ladeMesswerteDTO.setEndeUhrzeit(i3.getEndeUhrzeit());
            if (maxKfz.getKfz() == null) {
                maxKfz = ladeMesswerteDTO;
            } else {
                if (ladeMesswerteDTO.getKfz() > maxKfz.getKfz()) {
                    maxKfz = ladeMesswerteDTO;
                }
            }
            if (maxRad.getFahrradfahrer() == null) {
                maxRad = ladeMesswerteDTO;
            } else {
                if (ladeMesswerteDTO.getFahrradfahrer() > maxRad.getFahrradfahrer()) {
                    maxRad = ladeMesswerteDTO;
                }
            }
        }
        if (maxKfz.getKfz() != null) {
            maxKfz.setType(SPITZENSTUNDE_BLOCK_KFZ);
            maxKfz.setSortingIndex(
                    MesswerteSortingIndexUtil.getFirstStepSortingIndex(maxKfz) + MesswerteSortingIndexUtil.getSortingIndexSpitzenStundeWithinBlockKfz());
            if (block.getEnd().getMinute() == 59 && block.getStart().getHour() == 0) {
                maxKfz.setType(SPITZENSTUNDE_TAG_KFZ);
                maxKfz.setSortingIndex(MesswerteSortingIndexUtil.getSortingIndexSpitzenStundeCompleteDayKfz());
            }
        }
        if (maxRad.getFahrradfahrer() != null) {
            maxRad.setType(SPITZENSTUNDE_BLOCK_RAD);
            maxRad.setSortingIndex(
                    MesswerteSortingIndexUtil.getFirstStepSortingIndex(maxRad) + MesswerteSortingIndexUtil.getSortingIndexSpitzenStundeWithinBlockRad());
            if (block.getEnd().getMinute() == 59 && block.getStart().getHour() == 0) {
                maxRad.setType(SPITZENSTUNDE_TAG_RAD);
                maxRad.setSortingIndex(MesswerteSortingIndexUtil.getSortingIndexSpitzenStundeCompleteDayRad());
            }
        }

        return List.of(maxKfz, maxRad);
    }

    protected LadeMesswerteDTO calculateblock(final Zeitblock block, final List<MeasurementValuesPerInterval> intervals) {
        final LadeMesswerteDTO ladeMesswerteDTO = calculateSum(intervals);
        ladeMesswerteDTO.setEndeUhrzeit(block.getEnd().toLocalTime());
        ladeMesswerteDTO.setStartUhrzeit(block.getStart().toLocalTime());
        if (Zeitblock.ZB_00_24.equals(block)) {
            ladeMesswerteDTO.setType(GESAMT);
            ladeMesswerteDTO.setSortingIndex(MesswerteSortingIndexUtil.getSortingIndexGesamtCompleteDay());
        } else {
            ladeMesswerteDTO.setType(BLOCK);
            ladeMesswerteDTO.setSortingIndex(MesswerteSortingIndexUtil.getSortingIndexWithinBlock(ladeMesswerteDTO, TypeZeitintervall.BLOCK));
        }
        return ladeMesswerteDTO;
    }

    protected LadeMesswerteDTO calculateSum(final List<MeasurementValuesPerInterval> intervals) {
        final LadeMesswerteDTO dto = new LadeMesswerteDTO();
        dto.setPkw(intervals.stream().mapToInt(MeasurementValuesPerInterval::getSummeAllePkw).sum());
        dto.setLkw(intervals.stream().mapToInt(MeasurementValuesPerInterval::getAnzahlLkw).sum());
        dto.setLfw(intervals.stream().mapToInt(MeasurementValuesPerInterval::getAnzahlLfw).sum());
        dto.setLastzuege(intervals.stream().mapToInt(MeasurementValuesPerInterval::getSummeLastzug).sum());
        dto.setBusse(intervals.stream().mapToInt(MeasurementValuesPerInterval::getAnzahlBus).sum());
        dto.setKraftraeder(intervals.stream().mapToInt(MeasurementValuesPerInterval::getAnzahlKrad).sum());
        dto.setFahrradfahrer(intervals.stream().mapToInt(MeasurementValuesPerInterval::getAnzahlRad).sum());
        dto.setKfz(intervals.stream().mapToInt(MeasurementValuesPerInterval::getSummeKraftfahrzeugverkehr).sum());
        dto.setSchwerverkehr(intervals.stream().mapToInt(MeasurementValuesPerInterval::getSummeSchwerverkehr).sum());
        dto.setGueterverkehr(intervals.stream().mapToInt(MeasurementValuesPerInterval::getSummeGueterverkehr).sum());
        dto.setAnteilSchwerverkehrAnKfzProzent(calculateAnteilProzent(dto.getSchwerverkehr(), dto.getKfz()));
        dto.setAnteilGueterverkehrAnKfzProzent(calculateAnteilProzent(dto.getGueterverkehr(), dto.getKfz()));
        return dto;
    }

    protected Double calculateAnteilProzent(final Integer dividend, final Integer divisor) {
        final Double percentage = (Double.valueOf(dividend) / divisor) * 100;
        return BigDecimal.valueOf(percentage).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }
}
