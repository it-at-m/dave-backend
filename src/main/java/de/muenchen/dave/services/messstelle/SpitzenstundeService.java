/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2023
 */
package de.muenchen.dave.services.messstelle;

import de.muenchen.dave.domain.dtos.laden.messwerte.LadeMesswerteDTO;
import de.muenchen.dave.domain.enums.ZaehldatenIntervall;
import de.muenchen.dave.domain.enums.Zeitblock;
import de.muenchen.dave.geodateneai.gen.model.IntervalDto;
import de.muenchen.dave.util.messstelle.MesswerteBaseUtil;
import de.muenchen.dave.util.messstelle.MesswerteSortingIndexUtil;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class SpitzenstundeService {

    protected static final String SPITZENSTUNDE = "SpStd";
    protected static final String TAG = "Tag";
    protected static final String BLOCK = "Block";
    protected static final String KFZ = "KFZ";
    protected static final String RAD = "Rad";

    public List<IntervalDto> getIntervalsOfSpitzenstunde(final List<IntervalDto> intervals,
            final boolean isKfzMessstelle) {
        List<IntervalDto> result = new ArrayList<>();
        LadeMesswerteDTO spitzenStunde = new LadeMesswerteDTO();
        for (int index = 0; index + 3 < intervals.size(); index++) {
            final var i0 = intervals.get(index);
            final var i1 = intervals.get(index + 1);
            final var i2 = intervals.get(index + 2);
            final var i3 = intervals.get(index + 3);
            final var spitzenstundeIntervals = List.of(i0, i1, i2, i3);
            final LadeMesswerteDTO ladeMesswerteDTO = MesswerteBaseUtil.calculateSum(spitzenstundeIntervals);
            ladeMesswerteDTO.setStartUhrzeit(i0.getDatumUhrzeitVon().toLocalTime());
            ladeMesswerteDTO.setEndeUhrzeit(i3.getDatumUhrzeitBis().toLocalTime());
            if (isValueToCheckAgainstCurrentSpitzenstundeLarger(isKfzMessstelle, spitzenStunde, ladeMesswerteDTO)) {
                spitzenStunde = ladeMesswerteDTO;
                result = spitzenstundeIntervals;
            }
        }
        return result;
    }

    public List<IntervalDto> getIntervalsOfSpitzenstunde(
            final List<IntervalDto> intervals,
            final boolean isKfzMessstelle,
            final ZaehldatenIntervall intervalSize) {
        final int quarterPerHour;
        if (ZaehldatenIntervall.STUNDE_VIERTEL == intervalSize || ZaehldatenIntervall.STUNDE_VIERTEL_EINGESCHRAENKT == intervalSize) {
            quarterPerHour = 4;
        } else if (ZaehldatenIntervall.STUNDE_HALB == intervalSize) {
            quarterPerHour = 2;
        } else {
            quarterPerHour = 1;
        }
        var intervalsSpitzenstunde = new ArrayList<IntervalDto>();
        var spitzenStunde = new LadeMesswerteDTO();
        for (int index = 0; index + quarterPerHour < intervals.size(); index++) {
            final var intervalsToCheckForSpitzenstunde = new ArrayList<IntervalDto>();
            if (ZaehldatenIntervall.STUNDE_VIERTEL == intervalSize || ZaehldatenIntervall.STUNDE_VIERTEL_EINGESCHRAENKT == intervalSize) {
                intervalsToCheckForSpitzenstunde.addAll(
                        List.of(
                                intervals.get(index),
                                intervals.get(index + 1),
                                intervals.get(index + 2),
                                intervals.get(index + 3)));
            } else if (ZaehldatenIntervall.STUNDE_HALB == intervalSize) {
                intervalsToCheckForSpitzenstunde.addAll(List.of(intervals.get(index), intervals.get(index + 1)));
            } else {
                // ZaehldatenIntervall.STUNDE_KOMPLETT
                intervalsToCheckForSpitzenstunde.addAll(List.of(intervals.get(index)));
            }
            final var firstInterval = intervalsToCheckForSpitzenstunde.getFirst();
            final var lastInterval = intervalsToCheckForSpitzenstunde.getLast();
            final var sumToCheckAgainstSpitzenstunde = MesswerteBaseUtil.calculateSum(intervalsToCheckForSpitzenstunde);
            sumToCheckAgainstSpitzenstunde.setStartUhrzeit(firstInterval.getDatumUhrzeitVon().toLocalTime());
            sumToCheckAgainstSpitzenstunde.setEndeUhrzeit(lastInterval.getDatumUhrzeitBis().toLocalTime());
            if (isValueToCheckAgainstCurrentSpitzenstundeLarger(isKfzMessstelle, spitzenStunde, sumToCheckAgainstSpitzenstunde)) {
                spitzenStunde = sumToCheckAgainstSpitzenstunde;
                intervalsSpitzenstunde = intervalsToCheckForSpitzenstunde;
            }
        }
        return intervalsSpitzenstunde;
    }

    public LadeMesswerteDTO calculateSpitzenstunde(final Zeitblock block, final List<IntervalDto> intervals, final boolean isKfzMessstelle) {
        LadeMesswerteDTO spitzenStunde = new LadeMesswerteDTO();
        for (int index = 0; index + 3 < intervals.size(); index++) {
            final var i0 = intervals.get(index);
            final var i1 = intervals.get(index + 1);
            final var i2 = intervals.get(index + 2);
            final var i3 = intervals.get(index + 3);
            final LadeMesswerteDTO ladeMesswerteDTO = MesswerteBaseUtil.calculateSum(List.of(i0, i1, i2, i3));
            ladeMesswerteDTO.setStartUhrzeit(i0.getDatumUhrzeitVon().toLocalTime());
            ladeMesswerteDTO.setEndeUhrzeit(i3.getDatumUhrzeitVon().toLocalTime());
            if (isValueToCheckAgainstCurrentSpitzenstundeLarger(isKfzMessstelle, spitzenStunde, ladeMesswerteDTO)) {
                spitzenStunde = ladeMesswerteDTO;
            }
        }
        spitzenStunde.setType(getType(isKfzMessstelle, block));
        spitzenStunde.setSortingIndex(getSortingIndex(isKfzMessstelle, block, spitzenStunde));
        return spitzenStunde;
    }

    protected String getType(final boolean isKfzMessstelle, final Zeitblock zeitblock) {
        final StringBuilder type = new StringBuilder(SPITZENSTUNDE);
        if (Zeitblock.ZB_00_24.equals(zeitblock)) {
            type.append(TAG);
        } else {
            type.append(BLOCK);
        }
        type.append(" ");
        if (isKfzMessstelle) {
            type.append(KFZ);
        } else {
            type.append(RAD);
        }
        return new String(type);
    }

    protected int getSortingIndex(final boolean isKfzMessstelle, final Zeitblock zeitblock, final LadeMesswerteDTO spitzenStunde) {
        int sortingIndex;
        if (Zeitblock.ZB_00_24.equals(zeitblock)) {
            sortingIndex = getSortingIndexSpitzenStundeCompleteDay(isKfzMessstelle);
        } else {
            sortingIndex = MesswerteSortingIndexUtil.getFirstStepSortingIndex(spitzenStunde) + getSortingIndexSpitzenStundeWithinBlock(isKfzMessstelle);
        }
        return sortingIndex;
    }

    protected int getSortingIndexSpitzenStundeCompleteDay(final boolean isKfzMessstelle) {
        return isKfzMessstelle ? MesswerteSortingIndexUtil.getSortingIndexSpitzenStundeCompleteDayKfz()
                : MesswerteSortingIndexUtil.getSortingIndexSpitzenStundeCompleteDayRad();
    }

    protected int getSortingIndexSpitzenStundeWithinBlock(final boolean isKfzMessstelle) {
        return isKfzMessstelle ? MesswerteSortingIndexUtil.getSortingIndexSpitzenStundeWithinBlockKfz()
                : MesswerteSortingIndexUtil.getSortingIndexSpitzenStundeWithinBlockRad();
    }

    protected boolean isValueToCheckAgainstCurrentSpitzenstundeLarger(
            final boolean isKfzMessstelle,
            final LadeMesswerteDTO currentSpitzenstunde,
            final LadeMesswerteDTO toCheckAgainstCurrentSpitzenstunde) {
        boolean result;
        if (isKfzMessstelle) {
            result = isNewValueLarger(currentSpitzenstunde.getKfz(), toCheckAgainstCurrentSpitzenstunde.getKfz());
        } else {
            result = isNewValueLarger(currentSpitzenstunde.getFahrradfahrer(), toCheckAgainstCurrentSpitzenstunde.getFahrradfahrer());
        }
        return result;
    }

    protected boolean isNewValueLarger(final Integer currentMax, final Integer newValue) {
        return currentMax == null || newValue > currentMax;
    }

}
