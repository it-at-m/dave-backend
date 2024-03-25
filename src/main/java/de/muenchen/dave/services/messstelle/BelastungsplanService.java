package de.muenchen.dave.services.messstelle;

import de.muenchen.dave.domain.dtos.laden.messwerte.BelastungsplanMessquerschnitteDTO;
import de.muenchen.dave.domain.dtos.laden.messwerte.LadeBelastungsplanMessquerschnittDataDTO;
import de.muenchen.dave.domain.dtos.messstelle.ReadMessquerschnittDTO;
import de.muenchen.dave.domain.dtos.messstelle.ReadMessstelleInfoDTO;
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

    public BelastungsplanMessquerschnitteDTO ladeBelastungsplan(final List<TotalSumPerMessquerschnitt> totalSumOfAllMessquerschnitte,
            final String messstelleId) {
        final BelastungsplanMessquerschnitteDTO belastungsplanMessquerschnitteDTO = new BelastungsplanMessquerschnitteDTO();
        final List<LadeBelastungsplanMessquerschnittDataDTO> listBelastungsplanMessquerschnitteDTO = new ArrayList<>();
        final ReadMessstelleInfoDTO messstelle = messstelleService.readMessstelleInfo(messstelleId);
        belastungsplanMessquerschnitteDTO.setMstId(messstelle.getMstId());
        belastungsplanMessquerschnitteDTO.setStadtbezirkNummer(messstelle.getStadtbezirkNummer());
        belastungsplanMessquerschnitteDTO.setStrassenname(messstelle.getStandort());
        totalSumOfAllMessquerschnitte.forEach(sumOfMessquerschnitt -> {
            LadeBelastungsplanMessquerschnittDataDTO ladeBelastungsplanMessquerschnittDataDTO = new LadeBelastungsplanMessquerschnittDataDTO();
            ladeBelastungsplanMessquerschnittDataDTO.setSumKfz(sumOfMessquerschnitt.getSumKfz());
            ladeBelastungsplanMessquerschnittDataDTO.setSumGv(sumOfMessquerschnitt.getSumGv());
            ladeBelastungsplanMessquerschnittDataDTO.setSumSv(sumOfMessquerschnitt.getSumSv());
            ladeBelastungsplanMessquerschnittDataDTO.setSumRad(sumOfMessquerschnitt.getSumRad());
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
        belastungsplanMessquerschnitteDTO.setTotalKfz(totalSumKfz);
        belastungsplanMessquerschnitteDTO.setTotalSv(totalSumSv);
        belastungsplanMessquerschnitteDTO.setTotalGv(totalSumGv);
        belastungsplanMessquerschnitteDTO.setTotalRad(totalSumRad);
        final Integer totalSum = totalSumGv + totalSumKfz + totalSumSv;
        belastungsplanMessquerschnitteDTO.setTotalPercentGv(calcPercentage(totalSumGv, totalSum));
        belastungsplanMessquerschnitteDTO.setTotalPercentSv(calcPercentage(totalSumSv, totalSum));
        belastungsplanMessquerschnitteDTO.setLadeBelastungsplanMessquerschnittDataDTOList(listBelastungsplanMessquerschnitteDTO);
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
}
