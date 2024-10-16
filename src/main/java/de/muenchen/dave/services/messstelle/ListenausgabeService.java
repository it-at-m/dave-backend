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
import de.muenchen.dave.geodateneai.gen.model.IntervalDto;
import de.muenchen.dave.util.OptionsUtil;
import de.muenchen.dave.util.messstelle.MesswerteBaseUtil;
import de.muenchen.dave.util.messstelle.MesswerteSortingIndexUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class ListenausgabeService {

    protected static final String GESAMT = "Gesamt";
    protected static final String BLOCK = "Block";
    protected static final String STUNDE = "Stunde";
    private final LadeMesswerteMapper ladeMesswerteMapper;
    private final SpitzenstundeService spitzenstundeService;

    public LadeMesswerteListenausgabeDTO ladeListenausgabe(
            final List<IntervalDto> intervals,
            final boolean isKfzMessstelle,
            final MessstelleOptionsDTO options) {
        log.debug("#ladeListenausgabe");
        final var ladeMesswerteListenausgabe = new LadeMesswerteListenausgabeDTO();
        ladeMesswerteListenausgabe.getZaehldaten().addAll(ladeMesswerteMapper.interval2LadeMesswerte(intervals, options.getIntervall()));

        if (CollectionUtils.isNotEmpty(intervals)) {
            if (OptionsUtil.isZeitauswahlSpitzenstunde(options.getZeitauswahl())) {
                final var spitzenstunde = spitzenstundeService.calculateSpitzenstundeAndAddBlockSpecificDataToResult(
                        options.getZeitblock(),
                        intervals,
                        isKfzMessstelle,
                        options.getIntervall());
                ladeMesswerteListenausgabe.getZaehldaten().add(spitzenstunde);
            }

            if (StringUtils.equalsIgnoreCase(options.getZeitauswahl(), Zeitauswahl.TAGESWERT.getCapitalizedName())
                    && Zeitblock.ZB_00_24.equals(options.getZeitblock())) {

                if (Boolean.TRUE.equals(options.getTagessumme())) {
                    ladeMesswerteListenausgabe.getZaehldaten().add(calculateTagessumme(intervals, options));
                }
                if (Boolean.TRUE.equals(options.getStundensumme()) && !ZaehldatenIntervall.STUNDE_KOMPLETT.equals(options.getIntervall())) {
                    ladeMesswerteListenausgabe.getZaehldaten().addAll(calculateSumOfIntervalsPerHour(intervals));
                }

                if (Boolean.TRUE.equals(options.getBlocksumme()) || (Boolean.TRUE.equals(options.getSpitzenstunde()))) {
                    final var intervalsWithinZeitblock0006 = getIntervalsWithinZeitblock(intervals, Zeitblock.ZB_00_06);
                    final var intervalsWithinZeitblock0610 = getIntervalsWithinZeitblock(intervals, Zeitblock.ZB_06_10);
                    final var intervalsWithinZeitblock1015 = getIntervalsWithinZeitblock(intervals, Zeitblock.ZB_10_15);
                    final var intervalsWithinZeitblock1519 = getIntervalsWithinZeitblock(intervals, Zeitblock.ZB_15_19);
                    final var intervalsWithinZeitblock1924 = getIntervalsWithinZeitblock(intervals, Zeitblock.ZB_19_24);

                    if (Boolean.TRUE.equals(options.getBlocksumme())) {
                        if (CollectionUtils.isNotEmpty(intervalsWithinZeitblock0006)) {
                            ladeMesswerteListenausgabe.getZaehldaten().add(
                                    calculateSumOfIntervalsAndAddBlockSpecificDataToResult(intervalsWithinZeitblock0006, Zeitblock.ZB_00_06));
                        }
                        if (CollectionUtils.isNotEmpty(intervalsWithinZeitblock0610)) {
                            ladeMesswerteListenausgabe.getZaehldaten().add(
                                    calculateSumOfIntervalsAndAddBlockSpecificDataToResult(intervalsWithinZeitblock0610, Zeitblock.ZB_06_10));
                        }
                        if (CollectionUtils.isNotEmpty(intervalsWithinZeitblock1015)) {
                            ladeMesswerteListenausgabe.getZaehldaten().add(
                                    calculateSumOfIntervalsAndAddBlockSpecificDataToResult(intervalsWithinZeitblock1015, Zeitblock.ZB_10_15));
                        }
                        if (CollectionUtils.isNotEmpty(intervalsWithinZeitblock1519)) {
                            ladeMesswerteListenausgabe.getZaehldaten().add(
                                    calculateSumOfIntervalsAndAddBlockSpecificDataToResult(intervalsWithinZeitblock1519, Zeitblock.ZB_15_19));
                        }
                        if (CollectionUtils.isNotEmpty(intervalsWithinZeitblock1924)) {
                            ladeMesswerteListenausgabe.getZaehldaten().add(
                                    calculateSumOfIntervalsAndAddBlockSpecificDataToResult(intervalsWithinZeitblock1924, Zeitblock.ZB_19_24));
                        }
                    }

                    if (Boolean.TRUE.equals(options.getSpitzenstunde())) {
                        LadeMesswerteDTO spitzenstunde;
                        if (CollectionUtils.isNotEmpty(intervals)) {
                            spitzenstunde = spitzenstundeService.calculateSpitzenstundeAndAddBlockSpecificDataToResult(
                                    options.getZeitblock(),
                                    intervals,
                                    isKfzMessstelle,
                                    options.getIntervall());
                            ladeMesswerteListenausgabe.getZaehldaten().add(spitzenstunde);
                        }
                        if (CollectionUtils.isNotEmpty(intervalsWithinZeitblock0006)) {
                            spitzenstunde = spitzenstundeService.calculateSpitzenstundeAndAddBlockSpecificDataToResult(
                                    Zeitblock.ZB_00_06,
                                    intervalsWithinZeitblock0006,
                                    isKfzMessstelle,
                                    options.getIntervall());
                            ladeMesswerteListenausgabe.getZaehldaten().add(spitzenstunde);
                        }
                        if (CollectionUtils.isNotEmpty(intervalsWithinZeitblock0610)) {
                            spitzenstunde = spitzenstundeService.calculateSpitzenstundeAndAddBlockSpecificDataToResult(
                                    Zeitblock.ZB_06_10,
                                    intervalsWithinZeitblock0610,
                                    isKfzMessstelle,
                                    options.getIntervall());
                            ladeMesswerteListenausgabe.getZaehldaten().add(spitzenstunde);
                        }
                        if (CollectionUtils.isNotEmpty(intervalsWithinZeitblock1015)) {
                            spitzenstunde = spitzenstundeService.calculateSpitzenstundeAndAddBlockSpecificDataToResult(
                                    Zeitblock.ZB_10_15,
                                    intervalsWithinZeitblock1015,
                                    isKfzMessstelle,
                                    options.getIntervall());
                            ladeMesswerteListenausgabe.getZaehldaten().add(spitzenstunde);
                        }
                        if (CollectionUtils.isNotEmpty(intervalsWithinZeitblock1519)) {
                            spitzenstunde = spitzenstundeService.calculateSpitzenstundeAndAddBlockSpecificDataToResult(
                                    Zeitblock.ZB_15_19,
                                    intervalsWithinZeitblock1519,
                                    isKfzMessstelle,
                                    options.getIntervall());
                            ladeMesswerteListenausgabe.getZaehldaten().add(spitzenstunde);
                        }
                        if (CollectionUtils.isNotEmpty(intervalsWithinZeitblock1924)) {
                            spitzenstunde = spitzenstundeService.calculateSpitzenstundeAndAddBlockSpecificDataToResult(
                                    Zeitblock.ZB_19_24,
                                    intervalsWithinZeitblock1924,
                                    isKfzMessstelle,
                                    options.getIntervall());
                            ladeMesswerteListenausgabe.getZaehldaten().add(spitzenstunde);
                        }
                    }
                }
            }

            if (StringUtils.equalsIgnoreCase(options.getZeitauswahl(), Zeitauswahl.BLOCK.getCapitalizedName())) {
                final var necessaryIntervals = getIntervalsWithinZeitblock(intervals, options.getZeitblock());
                if (CollectionUtils.isNotEmpty(necessaryIntervals)) {
                    if (Boolean.TRUE.equals(options.getStundensumme()) && !ZaehldatenIntervall.STUNDE_KOMPLETT.equals(options.getIntervall())) {
                        ladeMesswerteListenausgabe.getZaehldaten().addAll(calculateSumOfIntervalsPerHour(necessaryIntervals));
                    }

                    if (Boolean.TRUE.equals(options.getSpitzenstunde())) {
                        var spitzenstunde = spitzenstundeService.calculateSpitzenstundeAndAddBlockSpecificDataToResult(
                                options.getZeitblock(),
                                necessaryIntervals,
                                isKfzMessstelle,
                                options.getIntervall());
                        ladeMesswerteListenausgabe.getZaehldaten().add(spitzenstunde);
                    }

                    if (Boolean.TRUE.equals(options.getBlocksumme())) {
                        ladeMesswerteListenausgabe.getZaehldaten()
                                .add(calculateSumOfIntervalsAndAddBlockSpecificDataToResult(necessaryIntervals, options.getZeitblock()));
                    }
                }
            }

            if (StringUtils.equalsIgnoreCase(options.getZeitauswahl(), Zeitauswahl.STUNDE.getCapitalizedName())
                    && Boolean.TRUE.equals(options.getStundensumme())
                    && !ZaehldatenIntervall.STUNDE_KOMPLETT.equals(options.getIntervall())) {
                final var necessaryIntervals = getIntervalsWithinZeitblock(intervals, options.getZeitblock());
                ladeMesswerteListenausgabe.getZaehldaten().addAll(calculateSumOfIntervalsPerHour(necessaryIntervals));
            }
        }

        final var messwerteSortedBySortingIndex = ladeMesswerteListenausgabe.getZaehldaten()
                .stream()
                .sorted(Comparator.comparing(LadeMesswerteDTO::getSortingIndex))
                .toList();

        ladeMesswerteListenausgabe.setZaehldaten(messwerteSortedBySortingIndex);
        return ladeMesswerteListenausgabe;
    }

    protected List<LadeMesswerteDTO> calculateSumOfIntervalsPerHour(final List<IntervalDto> intervals) {
        final List<LadeMesswerteDTO> dtos = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            final LocalTime start = LocalTime.of(i, 0);
            final LocalTime end;
            if (i == 23) {
                end = LocalTime.of(i, 59);
            } else {
                end = LocalTime.of(i + 1, 0);
            }
            final List<IntervalDto> relevantIntervals = getIntervalsWithinRange(intervals, start, end);
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

    protected LadeMesswerteDTO calculateSumOfIntervalsAndAddBlockSpecificDataToResult(
            final List<IntervalDto> intervalsOfZeitblock,
            final Zeitblock zeitblock) {
        final LadeMesswerteDTO ladeMesswerteDTO = MesswerteBaseUtil.calculateSum(intervalsOfZeitblock);
        ladeMesswerteDTO.setEndeUhrzeit(zeitblock.getEnd().toLocalTime());
        ladeMesswerteDTO.setStartUhrzeit(zeitblock.getStart().toLocalTime());
        ladeMesswerteDTO.setType(BLOCK);
        ladeMesswerteDTO.setSortingIndex(MesswerteSortingIndexUtil.getSortingIndexWithinBlock(ladeMesswerteDTO, TypeZeitintervall.BLOCK));
        return ladeMesswerteDTO;
    }

    protected LadeMesswerteDTO calculateTagessumme(
            final List<IntervalDto> intervals,
            final MessstelleOptionsDTO options) {
        final LadeMesswerteDTO ladeMesswerteDTO = MesswerteBaseUtil.calculateSum(intervals);
        ladeMesswerteDTO.setEndeUhrzeit(options.getZeitblock().getEnd().toLocalTime());
        ladeMesswerteDTO.setStartUhrzeit(options.getZeitblock().getStart().toLocalTime());
        ladeMesswerteDTO.setType(GESAMT);
        ladeMesswerteDTO.setSortingIndex(MesswerteSortingIndexUtil.getSortingIndexGesamtCompleteDay());
        return ladeMesswerteDTO;
    }

    protected List<IntervalDto> getIntervalsWithinZeitblock(final List<IntervalDto> intervals, final Zeitblock block) {
        return intervals.stream()
                .filter(interval -> MesswerteBaseUtil.isIntervalWithingZeitblock(interval, block))
                .collect(Collectors.toList());
    }

    protected List<IntervalDto> getIntervalsWithinRange(final List<IntervalDto> intervals, final LocalTime start, final LocalTime end) {
        return intervals.stream()
                .filter(intervall -> MesswerteBaseUtil.isIntervalWithinStartAndEnd(intervall, start, end))
                .collect(Collectors.toList());
    }
}
