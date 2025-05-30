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

    public List<IntervalDto> getIntervalsOfSpitzenstunde(
            final List<IntervalDto> intervals,
            final boolean isKfzMessstelle,
            final ZaehldatenIntervall intervalSize) {
        var intervalsSpitzenstunde = new ArrayList<IntervalDto>();
        var spitzenStunde = new LadeMesswerteDTO();
        for (int index = 0; index < intervals.size(); index++) {
            final var intervalsToCheckForSpitzenstunde = new ArrayList<IntervalDto>();
            if (ZaehldatenIntervall.STUNDE_VIERTEL == intervalSize || ZaehldatenIntervall.STUNDE_VIERTEL_EINGESCHRAENKT == intervalSize) {
                if (index < intervals.size())
                    intervalsToCheckForSpitzenstunde.add(intervals.get(index));
                if (index + 1 < intervals.size())
                    intervalsToCheckForSpitzenstunde.add(intervals.get(index + 1));
                if (index + 2 < intervals.size())
                    intervalsToCheckForSpitzenstunde.add(intervals.get(index + 2));
                if (index + 3 < intervals.size())
                    intervalsToCheckForSpitzenstunde.add(intervals.get(index + 3));
            } else if (ZaehldatenIntervall.STUNDE_HALB == intervalSize) {
                if (index < intervals.size())
                    intervalsToCheckForSpitzenstunde.add(intervals.get(index));
                if (index + 1 < intervals.size())
                    intervalsToCheckForSpitzenstunde.add(intervals.get(index + 1));
            } else {
                // ZaehldatenIntervall.STUNDE_KOMPLETT
                intervalsToCheckForSpitzenstunde.add(intervals.get(index));
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

    public LadeMesswerteDTO calculateSpitzenstundeAndAddBlockSpecificDataToResult(
            final Zeitblock block,
            final List<IntervalDto> intervals,
            final boolean isKfzMessstelle,
            final ZaehldatenIntervall intervalSize) {
        LadeMesswerteDTO spitzenStunde = new LadeMesswerteDTO();
        for (int index = 0; index < intervals.size(); index++) {
            final var intervalsToCheckForSpitzenstunde = new ArrayList<IntervalDto>();
            if (ZaehldatenIntervall.STUNDE_VIERTEL == intervalSize || ZaehldatenIntervall.STUNDE_VIERTEL_EINGESCHRAENKT == intervalSize) {
                if (index < intervals.size())
                    intervalsToCheckForSpitzenstunde.add(intervals.get(index));
                if (index + 1 < intervals.size())
                    intervalsToCheckForSpitzenstunde.add(intervals.get(index + 1));
                if (index + 2 < intervals.size())
                    intervalsToCheckForSpitzenstunde.add(intervals.get(index + 2));
                if (index + 3 < intervals.size())
                    intervalsToCheckForSpitzenstunde.add(intervals.get(index + 3));
            } else if (ZaehldatenIntervall.STUNDE_HALB == intervalSize) {
                if (index < intervals.size())
                    intervalsToCheckForSpitzenstunde.add(intervals.get(index));
                if (index + 1 < intervals.size())
                    intervalsToCheckForSpitzenstunde.add(intervals.get(index + 1));
            } else {
                // ZaehldatenIntervall.STUNDE_KOMPLETT
                intervalsToCheckForSpitzenstunde.add(intervals.get(index));
            }
            final var firstInterval = intervalsToCheckForSpitzenstunde.getFirst();
            final var lastInterval = intervalsToCheckForSpitzenstunde.getLast();
            final var ladeMesswerteDto = MesswerteBaseUtil.calculateSum(intervalsToCheckForSpitzenstunde);
            ladeMesswerteDto.setStartUhrzeit(firstInterval.getDatumUhrzeitVon().toLocalTime());
            ladeMesswerteDto.setEndeUhrzeit(lastInterval.getDatumUhrzeitBis().toLocalTime());
            if (isValueToCheckAgainstCurrentSpitzenstundeLarger(isKfzMessstelle, spitzenStunde, ladeMesswerteDto)) {
                spitzenStunde = ladeMesswerteDto;
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
        return isKfzMessstelle
                ? MesswerteSortingIndexUtil.getSortingIndexSpitzenStundeCompleteDayKfz()
                : MesswerteSortingIndexUtil.getSortingIndexSpitzenStundeCompleteDayRad();
    }

    protected int getSortingIndexSpitzenStundeWithinBlock(final boolean isKfzMessstelle) {
        return isKfzMessstelle
                ? MesswerteSortingIndexUtil.getSortingIndexSpitzenStundeWithinBlockKfz()
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
