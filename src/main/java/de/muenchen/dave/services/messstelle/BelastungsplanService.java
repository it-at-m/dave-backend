/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.services.messstelle;

import de.muenchen.dave.domain.dtos.laden.messwerte.BelastungsplanMessquerschnitteDTO;
import de.muenchen.dave.domain.dtos.laden.messwerte.LadeBelastungsplanMessquerschnittDataDTO;
import de.muenchen.dave.domain.dtos.messstelle.MessstelleOptionsDTO;
import de.muenchen.dave.domain.dtos.messstelle.ReadMessquerschnittDTO;
import de.muenchen.dave.domain.dtos.messstelle.ReadMessstelleInfoDTO;
import de.muenchen.dave.geodateneai.gen.model.IntervalDto;
import de.muenchen.dave.util.messstelle.MesswerteBaseUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class BelastungsplanService {

    private final MessstelleService messstelleService;

    private final RoundingService roundingService;

    private final SpitzenstundeService spitzenstundeService;

    public BelastungsplanMessquerschnitteDTO ladeBelastungsplan(
            final List<IntervalDto> intervals,
            final List<IntervalDto> totalSumOfAllMessquerschnitte,
            final String messstelleId,
            final MessstelleOptionsDTO options) {

        final BelastungsplanMessquerschnitteDTO belastungsplanMessquerschnitte = new BelastungsplanMessquerschnitteDTO();

        final ReadMessstelleInfoDTO messstelle = messstelleService.readMessstelleInfo(messstelleId);

        belastungsplanMessquerschnitte.setMstId(messstelle.getMstId());
        belastungsplanMessquerschnitte.setStadtbezirkNummer(messstelle.getStadtbezirkNummer());
        belastungsplanMessquerschnitte.setStrassenname(getStrassennameFromMessquerschnitt(messstelle));

        final var messquerschnitte = totalSumOfAllMessquerschnitte
                .stream()
                .map(sumOfMessquerschnitt -> {
                    LadeBelastungsplanMessquerschnittDataDTO messquerschnitt = new LadeBelastungsplanMessquerschnittDataDTO();

                    final var kfz = ObjectUtils.defaultIfNull(sumOfMessquerschnitt.getSummeKraftfahrzeugverkehr(), BigDecimal.ZERO).intValue();
                    final var sumKfz = roundNumberToHundredIfNeeded(kfz, options);
                    messquerschnitt.setSumKfz(sumKfz);

                    final var gv = ObjectUtils.defaultIfNull(sumOfMessquerschnitt.getSummeGueterverkehr(), BigDecimal.ZERO).intValue();
                    final var sumGv = roundNumberToHundredIfNeeded(gv, options);
                    messquerschnitt.setSumGv(sumGv);

                    final var sv = ObjectUtils.defaultIfNull(sumOfMessquerschnitt.getSummeSchwerverkehr(), BigDecimal.ZERO).intValue();
                    final var sumSv = roundNumberToHundredIfNeeded(sv, options);
                    messquerschnitt.setSumSv(sumSv);

                    final var rad = ObjectUtils.defaultIfNull(sumOfMessquerschnitt.getAnzahlRad(), BigDecimal.ZERO).intValue();
                    final var sumRad = roundNumberToHundredIfNeeded(rad, options);
                    messquerschnitt.setSumRad(sumRad);

                    final var percentGv = calcPercentage(gv, kfz);
                    messquerschnitt.setPercentGV(percentGv);

                    final var percentSv = calcPercentage(sv, kfz);
                    messquerschnitt.setPercentSv(percentSv);

                    final var mqId = sumOfMessquerschnitt.getMqId().toString();
                    messquerschnitt.setMqId(mqId);

                    final var direction = getDirection(messstelle, sumOfMessquerschnitt.getMqId().toString());
                    messquerschnitt.setDirection(direction);

                    return messquerschnitt;
                })
                .toList();
        belastungsplanMessquerschnitte.setLadeBelastungsplanMessquerschnittDataDTOList(messquerschnitte);

        final Integer totalSumKfz = totalSumOfAllMessquerschnitte
                .stream()
                .mapToInt(interval -> ObjectUtils.defaultIfNull(interval.getSummeKraftfahrzeugverkehr(), BigDecimal.ZERO).intValue())
                .sum();
        belastungsplanMessquerschnitte.setTotalKfz(roundNumberToHundredIfNeeded(totalSumKfz, options));

        final Integer totalSumSv = totalSumOfAllMessquerschnitte
                .stream()
                .mapToInt(interval -> ObjectUtils.defaultIfNull(interval.getSummeSchwerverkehr(), BigDecimal.ZERO).intValue())
                .sum();
        belastungsplanMessquerschnitte.setTotalSv(roundNumberToHundredIfNeeded(totalSumSv, options));

        final Integer totalSumGv = totalSumOfAllMessquerschnitte
                .stream()
                .mapToInt(interval -> ObjectUtils.defaultIfNull(interval.getSummeGueterverkehr(), BigDecimal.ZERO).intValue())
                .sum();
        belastungsplanMessquerschnitte.setTotalGv(roundNumberToHundredIfNeeded(totalSumGv, options));

        final Integer totalSumRad = totalSumOfAllMessquerschnitte
                .stream()
                .mapToInt(interval -> ObjectUtils.defaultIfNull(interval.getAnzahlRad(), BigDecimal.ZERO).intValue())
                .sum();
        belastungsplanMessquerschnitte.setTotalRad(roundNumberToHundredIfNeeded(totalSumRad, options));

        final var totalSum = totalSumGv + totalSumKfz + totalSumSv;
        final var totalPercentageGv = calcPercentage(totalSumGv, totalSum);
        belastungsplanMessquerschnitte.setTotalPercentGv(totalPercentageGv);
        final var totalPercentageSv = calcPercentage(totalSumSv, totalSum);
        belastungsplanMessquerschnitte.setTotalPercentSv(totalPercentageSv);

        if (options.getMessquerschnittIds().size() == 1) {
            final var isKfzStelle = Objects.equals(options.getZeitauswahl(), "Spitzenstunde KFZ");
            final var spitzenstunde = spitzenstundeService.calculateSpitzenstundeAndAddBlockSpecificDataToResult(
                    options.getZeitblock(),
                    intervals,
                    isKfzStelle,
                    options.getIntervall());
            belastungsplanMessquerschnitte.setStartUhrzeitSpitzenstunde(spitzenstunde.getStartUhrzeit());
            belastungsplanMessquerschnitte.setEndeUhrzeitSpitzenstunde(spitzenstunde.getEndeUhrzeit());
        }

        return belastungsplanMessquerschnitte;
    }

    protected static String getStrassennameFromMessquerschnitt(ReadMessstelleInfoDTO messstelle) {
        return CollectionUtils.isEmpty(messstelle.getMessquerschnitte())
                ? ""
                : messstelle.getMessquerschnitte().getFirst().getStrassenname();
    }

    protected String getDirection(final ReadMessstelleInfoDTO messstelle, final String messquerschnittId) {
        ReadMessquerschnittDTO messquerschnittDto = messstelle.getMessquerschnitte().stream()
                .filter(readMessquerschnittDTO -> Objects.equals(readMessquerschnittDTO.getMqId(), messquerschnittId))
                .toList()
                .getFirst();
        return messquerschnittDto.getFahrtrichtung();
    }

    protected BigDecimal calcPercentage(final Integer dividend, final Integer divisor) {
        final var percentage = MesswerteBaseUtil.calculateAnteilProzent(dividend, divisor);
        return BigDecimal.valueOf(percentage).setScale(1, RoundingMode.HALF_UP);
    }

    protected Integer roundNumberToHundredIfNeeded(final Integer numberToRound, final MessstelleOptionsDTO options) {
        if (Boolean.TRUE.equals(options.getWerteHundertRunden())) {
            return roundingService.roundIfNotNullOrZero(numberToRound, 100);
        } else {
            return numberToRound;
        }
    }
}
