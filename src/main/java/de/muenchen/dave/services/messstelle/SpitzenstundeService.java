/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2023
 */
package de.muenchen.dave.services.messstelle;

import de.muenchen.dave.domain.dtos.laden.messwerte.LadeMesswerteDTO;
import de.muenchen.dave.domain.enums.Zeitblock;
import de.muenchen.dave.geodateneai.gen.model.MeasurementValuesPerInterval;
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

    public List<MeasurementValuesPerInterval> getIntervalsOfSpitzenstunde(final List<MeasurementValuesPerInterval> intervals,
            final boolean isKfzMessstelle) {
        List<MeasurementValuesPerInterval> result = new ArrayList<>();
        LadeMesswerteDTO spitzenStunde = new LadeMesswerteDTO();
        for (int index = 0; index + 3 < intervals.size(); index++) {
            final MeasurementValuesPerInterval i0 = intervals.get(index);
            final MeasurementValuesPerInterval i1 = intervals.get(index + 1);
            final MeasurementValuesPerInterval i2 = intervals.get(index + 2);
            final MeasurementValuesPerInterval i3 = intervals.get(index + 3);
            List<MeasurementValuesPerInterval> spitzenstundeIntervals = List.of(i0, i1, i2, i3);
            final LadeMesswerteDTO ladeMesswerteDTO = MesswerteBaseUtil.calculateSum(spitzenstundeIntervals);
            ladeMesswerteDTO.setStartUhrzeit(i0.getStartUhrzeit());
            ladeMesswerteDTO.setEndeUhrzeit(i3.getEndeUhrzeit());
            if (saveNewValue(isKfzMessstelle, spitzenStunde, ladeMesswerteDTO)) {
                spitzenStunde = ladeMesswerteDTO;
                result = spitzenstundeIntervals;
            }
        }
        return result;
    }

    public LadeMesswerteDTO calculateSpitzenstunde(final Zeitblock block, final List<MeasurementValuesPerInterval> intervals, final boolean isKfzMessstelle) {
        LadeMesswerteDTO spitzenStunde = new LadeMesswerteDTO();
        for (int index = 0; index + 3 < intervals.size(); index++) {
            final MeasurementValuesPerInterval i0 = intervals.get(index);
            final MeasurementValuesPerInterval i1 = intervals.get(index + 1);
            final MeasurementValuesPerInterval i2 = intervals.get(index + 2);
            final MeasurementValuesPerInterval i3 = intervals.get(index + 3);
            final LadeMesswerteDTO ladeMesswerteDTO = MesswerteBaseUtil.calculateSum(List.of(i0, i1, i2, i3));
            ladeMesswerteDTO.setStartUhrzeit(i0.getStartUhrzeit());
            ladeMesswerteDTO.setEndeUhrzeit(i3.getEndeUhrzeit());
            if (saveNewValue(isKfzMessstelle, spitzenStunde, ladeMesswerteDTO)) {
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

    protected boolean saveNewValue(final boolean isKfzMessstelle, final LadeMesswerteDTO actualSpitzenstunde, final LadeMesswerteDTO newValue) {
        boolean result;
        if (isKfzMessstelle) {
            result = isNewValueBigger(actualSpitzenstunde.getKfz(), newValue.getKfz());
        } else {
            result = isNewValueBigger(actualSpitzenstunde.getFahrradfahrer(), newValue.getFahrradfahrer());
        }
        return result;
    }

    protected boolean isNewValueBigger(final Integer actualMax, final Integer newValue) {
        return actualMax == null || newValue > actualMax;
    }

}
