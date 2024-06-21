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
import de.muenchen.dave.geodateneai.gen.model.MeasurementValuesPerInterval;
import de.muenchen.dave.geodateneai.gen.model.TotalSumPerMessquerschnitt;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class BelastungsplanService {
    private final MessstelleService messstelleService;
    private final RoundingService roundingService;
    private final SpitzenstundeService spitzenstundeService;

    public BelastungsplanMessquerschnitteDTO ladeBelastungsplan(List<MeasurementValuesPerInterval> intervals,
            final List<TotalSumPerMessquerschnitt> totalSumOfAllMessquerschnitte,
            final String messstelleId, final MessstelleOptionsDTO options) {
        final BelastungsplanMessquerschnitteDTO belastungsplanMessquerschnitteDTO = new BelastungsplanMessquerschnitteDTO();
        final List<LadeBelastungsplanMessquerschnittDataDTO> listBelastungsplanMessquerschnitteDTO = new ArrayList<>();
        final ReadMessstelleInfoDTO messstelle = messstelleService.readMessstelleInfo(messstelleId);
        belastungsplanMessquerschnitteDTO.setMstId(messstelle.getMstId());
        belastungsplanMessquerschnitteDTO.setStadtbezirkNummer(messstelle.getStadtbezirkNummer());
        belastungsplanMessquerschnitteDTO.setStrassenname(messstelle.getMessquerschnitte().get(0).getStrassenname());
        totalSumOfAllMessquerschnitte.forEach(sumOfMessquerschnitt -> {
            LadeBelastungsplanMessquerschnittDataDTO ladeBelastungsplanMessquerschnittDataDTO = new LadeBelastungsplanMessquerschnittDataDTO();
            ladeBelastungsplanMessquerschnittDataDTO.setSumKfz(roundNumberIfNeeded(sumOfMessquerschnitt.getSumKfz(), options));
            ladeBelastungsplanMessquerschnittDataDTO.setSumGv(roundNumberIfNeeded(sumOfMessquerschnitt.getSumGv(), options));
            ladeBelastungsplanMessquerschnittDataDTO.setSumSv(roundNumberIfNeeded(sumOfMessquerschnitt.getSumSv(), options));
            ladeBelastungsplanMessquerschnittDataDTO.setSumRad(roundNumberIfNeeded(sumOfMessquerschnitt.getSumRad(), options));
            ladeBelastungsplanMessquerschnittDataDTO.setPercentGV(calcPercentage(sumOfMessquerschnitt.getSumGv(), sumOfMessquerschnitt.getSumKfz()));
            ladeBelastungsplanMessquerschnittDataDTO.setPercentSv(calcPercentage(sumOfMessquerschnitt.getSumSv(), sumOfMessquerschnitt.getSumKfz()));
            ladeBelastungsplanMessquerschnittDataDTO.setMqId(sumOfMessquerschnitt.getMqId());
            ladeBelastungsplanMessquerschnittDataDTO.setDirection(getDirection(messstelle, sumOfMessquerschnitt.getMqId()));
            listBelastungsplanMessquerschnitteDTO.add(ladeBelastungsplanMessquerschnittDataDTO);
        });
        final Integer totalSumKfz = totalSumOfAllMessquerschnitte.stream().mapToInt(TotalSumPerMessquerschnitt::getSumKfz).sum();
        final Integer totalSumSv = totalSumOfAllMessquerschnitte.stream().mapToInt(TotalSumPerMessquerschnitt::getSumSv).sum();
        final Integer totalSumGv = totalSumOfAllMessquerschnitte.stream().mapToInt(TotalSumPerMessquerschnitt::getSumGv).sum();
        final Integer totalSumRad = totalSumOfAllMessquerschnitte.stream().mapToInt(TotalSumPerMessquerschnitt::getSumRad).sum();
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
            final var spitzenstunde = spitzenstundeService.calculateSpitzenstunde(options.getZeitblock(), intervals, isKfzStelle);
            belastungsplanMessquerschnitteDTO.setStartUhrzeitSpitzenstunde(spitzenstunde.getStartUhrzeit());
            belastungsplanMessquerschnitteDTO.setEndeUhrzeitSpitzenstunde(spitzenstunde.getEndeUhrzeit());
        }
        return belastungsplanMessquerschnitteDTO;
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
