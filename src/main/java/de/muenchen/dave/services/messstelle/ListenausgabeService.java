/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2023
 */
package de.muenchen.dave.services.messstelle;

import de.muenchen.dave.domain.dtos.laden.messwerte.LadeMesswerteDTO;
import de.muenchen.dave.domain.dtos.laden.messwerte.LadeMesswerteListenausgabeDTO;
import de.muenchen.dave.domain.dtos.messstelle.MessstelleOptionsDTO;
import de.muenchen.dave.domain.enums.TypeZeitintervall;
import de.muenchen.dave.domain.enums.ZaehldatenIntervall;
import de.muenchen.dave.domain.enums.Zeitauswahl;
import de.muenchen.dave.domain.enums.Zeitblock;
import de.muenchen.dave.domain.mapper.LadeMesswerteMapper;
import de.muenchen.dave.geodateneai.gen.model.IntervallDto;
import de.muenchen.dave.util.OptionsUtil;
import de.muenchen.dave.util.messstelle.MesswerteBaseUtil;
import de.muenchen.dave.util.messstelle.MesswerteSortingIndexUtil;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class ListenausgabeService {

    private final LadeMesswerteMapper ladeMesswerteMapper;
    private final SpitzenstundeService spitzenstundeService;

    protected static final String GESAMT = "Gesamt";
    protected static final String BLOCK = "Block";
    protected static final String STUNDE = "Stunde";

    public LadeMesswerteListenausgabeDTO ladeListenausgabe(final List<IntervallDto> intervals, final boolean isKfzMessstelle,
            final MessstelleOptionsDTO options) {
        log.debug("#ladeListenausgabe");
        final LadeMesswerteListenausgabeDTO dto = new LadeMesswerteListenausgabeDTO();
        dto.getZaehldaten().addAll(mapIntervalsToLadeMesswerteDTOs(intervals));
        if (OptionsUtil.isZeitauswahlSpitzenstunde(options.getZeitauswahl()) && ZaehldatenIntervall.STUNDE_VIERTEL.equals(options.getIntervall())) {
            dto.getZaehldaten().add(calculateSpitzenstunde(intervals, isKfzMessstelle, options.getZeitblock()));
        }

        if (StringUtils.equalsIgnoreCase(options.getZeitauswahl(), Zeitauswahl.TAGESWERT.getCapitalizedName())
                && Zeitblock.ZB_00_24.equals(options.getZeitblock())) {
            if (Boolean.TRUE.equals(options.getTagessumme())) {
                dto.getZaehldaten().add(calculateTagessumme(intervals, options));
            }
            if (Boolean.TRUE.equals(options.getStundensumme())) {
                dto.getZaehldaten().addAll(calculateSumOfIntervalsPerHour(intervals));
            }
            if (Boolean.TRUE.equals(options.getBlocksumme())
                    || (Boolean.TRUE.equals(options.getSpitzenstunde()) && ZaehldatenIntervall.STUNDE_VIERTEL.equals(options.getIntervall()))) {
                final List<IntervallDto> intervalsWithinZeitblock0006 = getIntervalsWithinZeitblock(intervals, Zeitblock.ZB_00_06);
                final List<IntervallDto> intervalsWithinZeitblock0610 = getIntervalsWithinZeitblock(intervals, Zeitblock.ZB_06_10);
                final List<IntervallDto> intervalsWithinZeitblock1015 = getIntervalsWithinZeitblock(intervals, Zeitblock.ZB_10_15);
                final List<IntervallDto> intervalsWithinZeitblock1519 = getIntervalsWithinZeitblock(intervals, Zeitblock.ZB_15_19);
                final List<IntervallDto> intervalsWithinZeitblock1924 = getIntervalsWithinZeitblock(intervals, Zeitblock.ZB_19_24);

                if (Boolean.TRUE.equals(options.getBlocksumme())) {
                    dto.getZaehldaten().add(calculateSumOfIntervalsPerBlock(intervalsWithinZeitblock0006, Zeitblock.ZB_00_06));
                    dto.getZaehldaten().add(calculateSumOfIntervalsPerBlock(intervalsWithinZeitblock0610, Zeitblock.ZB_06_10));
                    dto.getZaehldaten().add(calculateSumOfIntervalsPerBlock(intervalsWithinZeitblock1015, Zeitblock.ZB_10_15));
                    dto.getZaehldaten().add(calculateSumOfIntervalsPerBlock(intervalsWithinZeitblock1519, Zeitblock.ZB_15_19));
                    dto.getZaehldaten().add(calculateSumOfIntervalsPerBlock(intervalsWithinZeitblock1924, Zeitblock.ZB_19_24));
                }
                if (Boolean.TRUE.equals(options.getSpitzenstunde()) && ZaehldatenIntervall.STUNDE_VIERTEL.equals(options.getIntervall())) {
                    dto.getZaehldaten().add(calculateSpitzenstunde(intervals, isKfzMessstelle, options.getZeitblock()));
                    dto.getZaehldaten().add(calculateSpitzenstunde(intervalsWithinZeitblock0006, isKfzMessstelle, Zeitblock.ZB_00_06));
                    dto.getZaehldaten().add(calculateSpitzenstunde(intervalsWithinZeitblock0610, isKfzMessstelle, Zeitblock.ZB_06_10));
                    dto.getZaehldaten().add(calculateSpitzenstunde(intervalsWithinZeitblock1015, isKfzMessstelle, Zeitblock.ZB_10_15));
                    dto.getZaehldaten().add(calculateSpitzenstunde(intervalsWithinZeitblock1519, isKfzMessstelle, Zeitblock.ZB_15_19));
                    dto.getZaehldaten().add(calculateSpitzenstunde(intervalsWithinZeitblock1924, isKfzMessstelle, Zeitblock.ZB_19_24));
                }
            }

        }

        if (StringUtils.equalsIgnoreCase(options.getZeitauswahl(), Zeitauswahl.BLOCK.getCapitalizedName())) {
            final List<IntervallDto> necessaryIntervals = getIntervalsWithinZeitblock(intervals, options.getZeitblock());
            if (Boolean.TRUE.equals(options.getStundensumme())) {
                dto.getZaehldaten().addAll(calculateSumOfIntervalsPerHour(necessaryIntervals));
            }
            if (Boolean.TRUE.equals(options.getSpitzenstunde()) && ZaehldatenIntervall.STUNDE_VIERTEL.equals(options.getIntervall())) {
                dto.getZaehldaten().add(calculateSpitzenstunde(necessaryIntervals, isKfzMessstelle, options.getZeitblock()));
            }
            if (Boolean.TRUE.equals(options.getBlocksumme())) {
                dto.getZaehldaten().add(calculateSumOfIntervalsPerBlock(necessaryIntervals, options.getZeitblock()));
            }
        }

        if (StringUtils.equalsIgnoreCase(options.getZeitauswahl(), Zeitauswahl.STUNDE.getCapitalizedName())) {
            final List<IntervallDto> necessaryIntervals = getIntervalsWithinZeitblock(intervals, options.getZeitblock());
            if (Boolean.TRUE.equals(options.getStundensumme())) {
                dto.getZaehldaten().addAll(calculateSumOfIntervalsPerHour(necessaryIntervals));
            }
        }
        dto.setZaehldaten(dto.getZaehldaten().stream().sorted(Comparator.comparing(LadeMesswerteDTO::getSortingIndex)).collect(Collectors.toList()));
        return dto;
    }

    protected List<LadeMesswerteDTO> mapIntervalsToLadeMesswerteDTOs(final List<IntervallDto> intervals) {
        final List<LadeMesswerteDTO> dtos = new ArrayList<>();
        intervals.forEach(intervall -> {
            final LadeMesswerteDTO dto = ladeMesswerteMapper.measurementValuesPerIntervalToLadeMesswerteDTO(intervall);
            dto.setSortingIndex(MesswerteSortingIndexUtil.getSortingIndexWithinBlock(dto, TypeZeitintervall.STUNDE_VIERTEL));
            dtos.add(dto);
        });
        return dtos;
    }

    protected List<LadeMesswerteDTO> calculateSumOfIntervalsPerHour(final List<IntervallDto> intervals) {
        final List<LadeMesswerteDTO> dtos = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            final LocalTime start = LocalTime.of(i, 0);
            final LocalTime end;
            if (i == 23) {
                end = LocalTime.of(i, 59);
            } else {
                end = LocalTime.of(i + 1, 0);
            }
            final List<IntervallDto> relevantIntervals = getIntervalsWithinRange(intervals, start, end);
            if (CollectionUtils.isNotEmpty(relevantIntervals)) {
                final LadeMesswerteDTO dto = MesswerteBaseUtil.calculateSum(
                        relevantIntervals);
                dto.setStartUhrzeit(start);
                dto.setEndeUhrzeit(end);
                dto.setType(STUNDE);
                dto.setSortingIndex(MesswerteSortingIndexUtil.getSortingIndexWithinBlock(dto, TypeZeitintervall.STUNDE_KOMPLETT));
                dtos.add(dto);
            }
        }
        return dtos;
    }

    protected LadeMesswerteDTO calculateSpitzenstunde(final List<IntervallDto> intervals,
            final boolean isKfzMessstelle, final Zeitblock zeitblock) {
        return spitzenstundeService.calculateSpitzenstunde(zeitblock, intervals, isKfzMessstelle);
    }

    protected LadeMesswerteDTO calculateSumOfIntervalsPerBlock(final List<IntervallDto> intervals,
            final Zeitblock zeitblock) {
        final LadeMesswerteDTO ladeMesswerteDTO = MesswerteBaseUtil.calculateSum(intervals);
        ladeMesswerteDTO.setEndeUhrzeit(zeitblock.getEnd().toLocalTime());
        ladeMesswerteDTO.setStartUhrzeit(zeitblock.getStart().toLocalTime());
        ladeMesswerteDTO.setType(BLOCK);
        ladeMesswerteDTO.setSortingIndex(MesswerteSortingIndexUtil.getSortingIndexWithinBlock(ladeMesswerteDTO, TypeZeitintervall.BLOCK));
        return ladeMesswerteDTO;
    }

    protected LadeMesswerteDTO calculateTagessumme(final List<IntervallDto> intervals,
            final MessstelleOptionsDTO options) {
        final LadeMesswerteDTO ladeMesswerteDTO = MesswerteBaseUtil.calculateSum(intervals);
        ladeMesswerteDTO.setEndeUhrzeit(options.getZeitblock().getEnd().toLocalTime());
        ladeMesswerteDTO.setStartUhrzeit(options.getZeitblock().getStart().toLocalTime());
        ladeMesswerteDTO.setType(GESAMT);
        ladeMesswerteDTO.setSortingIndex(MesswerteSortingIndexUtil.getSortingIndexGesamtCompleteDay());
        return ladeMesswerteDTO;
    }

    protected List<IntervallDto> getIntervalsWithinZeitblock(final List<IntervallDto> intervals, final Zeitblock block) {
        return intervals.stream()
                .filter(interval -> MesswerteBaseUtil.isTimeWithinBlock(interval.getDatumUhrzeitVon().toLocalTime(), block))
                .collect(Collectors.toList());
    }

    protected List<IntervallDto> getIntervalsWithinRange(final List<IntervallDto> intervals, final LocalTime start,
            final LocalTime end) {
        return intervals.stream()
                .filter(intervall -> MesswerteBaseUtil.isTimeBetweenStartAndEnd(intervall.getDatumUhrzeitVon().toLocalTime(), start, end))
                .collect(Collectors.toList());
    }
}
