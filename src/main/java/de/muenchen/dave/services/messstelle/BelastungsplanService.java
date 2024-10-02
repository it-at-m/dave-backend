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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import de.muenchen.dave.geodateneai.gen.model.IntervalDto;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class BelastungsplanService {
    private final MessstelleService messstelleService;
    private final RoundingService roundingService;
    private final SpitzenstundeService spitzenstundeService;

    public BelastungsplanMessquerschnitteDTO ladeBelastungsplan(final List<IntervalDto> intervals,
            final List<IntervalDto> totalSumOfAllMessquerschnitte,
            final String messstelleId, final MessstelleOptionsDTO options) {
        final BelastungsplanMessquerschnitteDTO belastungsplanMessquerschnitteDTO = new BelastungsplanMessquerschnitteDTO();
        final List<LadeBelastungsplanMessquerschnittDataDTO> listBelastungsplanMessquerschnitteDTO = new ArrayList<>();
        final ReadMessstelleInfoDTO messstelle = messstelleService.readMessstelleInfo(messstelleId);
        belastungsplanMessquerschnitteDTO.setMstId(messstelle.getMstId());
        belastungsplanMessquerschnitteDTO.setStadtbezirkNummer(messstelle.getStadtbezirkNummer());
        belastungsplanMessquerschnitteDTO.setStrassenname(getStrassennameFromMessquerschnitt(messstelle));
        totalSumOfAllMessquerschnitte.forEach(sumOfMessquerschnitt -> {
            LadeBelastungsplanMessquerschnittDataDTO ladeBelastungsplanMessquerschnittDataDTO = new LadeBelastungsplanMessquerschnittDataDTO();
            ladeBelastungsplanMessquerschnittDataDTO.setSumKfz(roundNumberIfNeeded(sumOfMessquerschnitt.getSummeKraftfahrzeugverkehr().intValue(), options));
            ladeBelastungsplanMessquerschnittDataDTO.setSumGv(roundNumberIfNeeded(sumOfMessquerschnitt.getSummeGueterverkehr().intValue(), options));
            ladeBelastungsplanMessquerschnittDataDTO.setSumSv(roundNumberIfNeeded(sumOfMessquerschnitt.getSummeSchwerverkehr().intValue(), options));
            ladeBelastungsplanMessquerschnittDataDTO.setSumRad(roundNumberIfNeeded(sumOfMessquerschnitt.getAnzahlRad().intValue(), options));
            ladeBelastungsplanMessquerschnittDataDTO.setPercentGV(
                    calcPercentage(sumOfMessquerschnitt.getSummeGueterverkehr().intValue(), sumOfMessquerschnitt.getSummeKraftfahrzeugverkehr().intValue()));
            ladeBelastungsplanMessquerschnittDataDTO.setPercentSv(
                    calcPercentage(sumOfMessquerschnitt.getSummeSchwerverkehr().intValue(), sumOfMessquerschnitt.getSummeKraftfahrzeugverkehr().intValue()));
            ladeBelastungsplanMessquerschnittDataDTO.setMqId(sumOfMessquerschnitt.getMqId().toString());
            ladeBelastungsplanMessquerschnittDataDTO.setDirection(getDirection(messstelle, sumOfMessquerschnitt.getMqId().toString()));
            listBelastungsplanMessquerschnitteDTO.add(ladeBelastungsplanMessquerschnittDataDTO);
        });
        final Integer totalSumKfz = totalSumOfAllMessquerschnitte.stream().mapToInt(interval -> interval.getSummeKraftfahrzeugverkehr().intValue()).sum();
        final Integer totalSumSv = totalSumOfAllMessquerschnitte.stream().mapToInt(interval -> interval.getSummeSchwerverkehr().intValue()).sum();
        final Integer totalSumGv = totalSumOfAllMessquerschnitte.stream().mapToInt(interval -> interval.getSummeGueterverkehr().intValue()).sum();
        final Integer totalSumRad = totalSumOfAllMessquerschnitte.stream().mapToInt(interval -> interval.getAnzahlRad().intValue()).sum();
        belastungsplanMessquerschnitteDTO.setTotalKfz(roundNumberIfNeeded(totalSumKfz, options));
        belastungsplanMessquerschnitteDTO.setTotalSv(roundNumberIfNeeded(totalSumSv, options));
        belastungsplanMessquerschnitteDTO.setTotalGv(roundNumberIfNeeded(totalSumGv, options));
        belastungsplanMessquerschnitteDTO.setTotalRad(roundNumberIfNeeded(totalSumRad, options));
        final Integer totalSum = totalSumGv + totalSumKfz + totalSumSv;
        belastungsplanMessquerschnitteDTO.setTotalPercentGv(calcPercentage(totalSumGv, totalSum));
        belastungsplanMessquerschnitteDTO.setTotalPercentSv(calcPercentage(totalSumSv, totalSum));
        belastungsplanMessquerschnitteDTO.setLadeBelastungsplanMessquerschnittDataDTOList(listBelastungsplanMessquerschnitteDTO);
        if (options.getMessquerschnittIds().size() == 1) {
            final var isKfzStelle = Objects.equals(options.getZeitauswahl(), "Spitzenstunde KFZ");
            final var spitzenstunde = spitzenstundeService.calculateSpitzenstunde(options.getZeitblock(), intervals, isKfzStelle, options.getIntervall());
            belastungsplanMessquerschnitteDTO.setStartUhrzeitSpitzenstunde(spitzenstunde.getStartUhrzeit());
            belastungsplanMessquerschnitteDTO.setEndeUhrzeitSpitzenstunde(spitzenstunde.getEndeUhrzeit());
        }
        return belastungsplanMessquerschnitteDTO;
    }

    private static String getStrassennameFromMessquerschnitt(ReadMessstelleInfoDTO messstelle) {
        if (CollectionUtils.isEmpty(messstelle.getMessquerschnitte())) {
            return "";
        }
        return messstelle.getMessquerschnitte().get(0).getStrassenname();
    }

    protected String getDirection(final ReadMessstelleInfoDTO messstelle, final String messquerschnittId) {
        ReadMessquerschnittDTO messquerschnittDto = messstelle.getMessquerschnitte().stream()
                .filter(readMessquerschnittDTO -> Objects.equals(readMessquerschnittDTO.getMqId(), messquerschnittId)).collect(Collectors.toList()).get(0);
        return messquerschnittDto.getFahrtrichtung();
    }

    protected BigDecimal calcPercentage(final Integer part, final Integer total) {
        BigDecimal partInBigDecimal = BigDecimal.valueOf(part);
        BigDecimal totalInBigDecimal = BigDecimal.valueOf(total);
        return partInBigDecimal.divide(totalInBigDecimal, 3, RoundingMode.HALF_UP).scaleByPowerOfTen(2);
    }

    private Integer roundNumberIfNeeded(final Integer numberToRound, final MessstelleOptionsDTO options) {
        if (Boolean.TRUE.equals(options.getWerteHundertRunden())) {
            return roundingService.roundIfNotNullOrZero(numberToRound, 100);
        } else {
            return numberToRound;
        }
    }
}
