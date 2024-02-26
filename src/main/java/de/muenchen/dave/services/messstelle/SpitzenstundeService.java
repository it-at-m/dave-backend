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
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class SpitzenstundeService {

    protected static final String SPITZENSTUNDE_TAG = "SpStdTag";
    protected static final String SPITZENSTUNDE_TAG_KFZ = SPITZENSTUNDE_TAG + " KFZ";
    protected static final String SPITZENSTUNDE_TAG_RAD = SPITZENSTUNDE_TAG + " Rad";
    protected static final String SPITZENSTUNDE_BLOCK = "SpStdBlock";
    protected static final String SPITZENSTUNDE_BLOCK_KFZ = SPITZENSTUNDE_BLOCK + " KFZ";
    protected static final String SPITZENSTUNDE_BLOCK_RAD = SPITZENSTUNDE_BLOCK + " Rad";

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

            if (isKfzMessstelle) {
                if (spitzenStunde.getKfz() == null) {
                    spitzenStunde = ladeMesswerteDTO;
                } else {
                    if (ladeMesswerteDTO.getKfz() > spitzenStunde.getKfz()) {
                        spitzenStunde = ladeMesswerteDTO;
                    }
                }
            } else {
                if (spitzenStunde.getFahrradfahrer() == null) {
                    spitzenStunde = ladeMesswerteDTO;
                } else {
                    if (ladeMesswerteDTO.getFahrradfahrer() > spitzenStunde.getFahrradfahrer()) {
                        spitzenStunde = ladeMesswerteDTO;
                    }
                }
            }
        }

        if (isKfzMessstelle) {
            if (Zeitblock.ZB_00_24.equals(block)) {
                spitzenStunde.setType(SPITZENSTUNDE_TAG_KFZ);
                spitzenStunde.setSortingIndex(MesswerteSortingIndexUtil.getSortingIndexSpitzenStundeCompleteDayKfz());
            } else {
                spitzenStunde.setType(SPITZENSTUNDE_BLOCK_KFZ);
                spitzenStunde.setSortingIndex(
                        MesswerteSortingIndexUtil.getFirstStepSortingIndex(spitzenStunde)
                                + MesswerteSortingIndexUtil.getSortingIndexSpitzenStundeWithinBlockKfz());
            }
        } else {
            if (Zeitblock.ZB_00_24.equals(block)) {
                spitzenStunde.setType(SPITZENSTUNDE_TAG_RAD);
                spitzenStunde.setSortingIndex(MesswerteSortingIndexUtil.getSortingIndexSpitzenStundeCompleteDayRad());
            } else {
                spitzenStunde.setType(SPITZENSTUNDE_BLOCK_RAD);
                spitzenStunde.setSortingIndex(
                        MesswerteSortingIndexUtil.getFirstStepSortingIndex(spitzenStunde)
                                + MesswerteSortingIndexUtil.getSortingIndexSpitzenStundeWithinBlockRad());
            }
        }

        return spitzenStunde;
    }

}
