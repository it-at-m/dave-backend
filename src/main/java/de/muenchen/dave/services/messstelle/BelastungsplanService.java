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
                    final var sumKfz = roundNumberToHundredIfNeeded(sumOfMessquerschnitt.getSummeKraftfahrzeugverkehr().intValue(), options);
                    messquerschnitt.setSumKfz(sumKfz);
                    final var sumGv = roundNumberToHundredIfNeeded(sumOfMessquerschnitt.getSummeGueterverkehr().intValue(), options);
                    messquerschnitt.setSumGv(sumGv);
                    final var sumSv = roundNumberToHundredIfNeeded(sumOfMessquerschnitt.getSummeSchwerverkehr().intValue(), options);
                    messquerschnitt.setSumSv(sumSv);
                    final var sumRad = roundNumberToHundredIfNeeded(sumOfMessquerschnitt.getAnzahlRad().intValue(), options);
                    messquerschnitt.setSumRad(sumRad);
                    final var percentGv = calcPercentage(sumOfMessquerschnitt.getSummeGueterverkehr().intValue(),
                            sumOfMessquerschnitt.getSummeKraftfahrzeugverkehr().intValue());
                    messquerschnitt.setPercentGV(percentGv);
                    final var percentSv = calcPercentage(sumOfMessquerschnitt.getSummeSchwerverkehr().intValue(),
                            sumOfMessquerschnitt.getSummeKraftfahrzeugverkehr().intValue());
                    messquerschnitt.setPercentSv(percentSv);
                    final var mqId = sumOfMessquerschnitt.getMqId().toString();
                    messquerschnitt.setMqId(mqId);
                    final var direction = getDirection(messstelle, sumOfMessquerschnitt.getMqId().toString());
                    messquerschnitt.setDirection(direction);
                    return messquerschnitt;
                })
                .toList();

        final Integer totalSumKfz = totalSumOfAllMessquerschnitte.stream().mapToInt(interval -> interval.getSummeKraftfahrzeugverkehr().intValue()).sum();
        final Integer totalSumSv = totalSumOfAllMessquerschnitte.stream().mapToInt(interval -> interval.getSummeSchwerverkehr().intValue()).sum();
        final Integer totalSumGv = totalSumOfAllMessquerschnitte.stream().mapToInt(interval -> interval.getSummeGueterverkehr().intValue()).sum();
        final Integer totalSumRad = totalSumOfAllMessquerschnitte.stream().mapToInt(interval -> interval.getAnzahlRad().intValue()).sum();


        belastungsplanMessquerschnitte.setTotalKfz(roundNumberToHundredIfNeeded(totalSumKfz, options));
        belastungsplanMessquerschnitte.setTotalSv(roundNumberToHundredIfNeeded(totalSumSv, options));
        belastungsplanMessquerschnitte.setTotalGv(roundNumberToHundredIfNeeded(totalSumGv, options));
        belastungsplanMessquerschnitte.setTotalRad(roundNumberToHundredIfNeeded(totalSumRad, options));
        final Integer totalSum = totalSumGv + totalSumKfz + totalSumSv;
        belastungsplanMessquerschnitte.setTotalPercentGv(calcPercentage(totalSumGv, totalSum));
        belastungsplanMessquerschnitte.setTotalPercentSv(calcPercentage(totalSumSv, totalSum));
        belastungsplanMessquerschnitte.setLadeBelastungsplanMessquerschnittDataDTOList(messquerschnitte);
        if (options.getMessquerschnittIds().size() == 1) {
            final var isKfzStelle = Objects.equals(options.getZeitauswahl(), "Spitzenstunde KFZ");
            final var spitzenstunde = spitzenstundeService.calculateSpitzenstundeAndAddBlockSpecificDataToResult(options.getZeitblock(), intervals, isKfzStelle,
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
