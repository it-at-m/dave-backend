package de.muenchen.dave.services.messstelle;

import de.muenchen.dave.domain.dtos.laden.messwerte.LadeBelastungsplanMessquerschnittDataDTO;
import de.muenchen.dave.domain.dtos.laden.messwerte.ListBelastungsplanMessquerschnitteDTO;
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
    public ListBelastungsplanMessquerschnitteDTO ladeBelastungsplan(List<TotalSumPerMessquerschnitt> totalSumOfAllMessquerschnitte, String messstelleId) {
        ListBelastungsplanMessquerschnitteDTO listBelastungsplanMessquerschnitteDTOClass = new ListBelastungsplanMessquerschnitteDTO();
        List<LadeBelastungsplanMessquerschnittDataDTO> listBelastungsplanMessquerschnitteDTO  = new ArrayList<>();
        var messstelle = messstelleService.readMessstelleInfo(messstelleId);
        listBelastungsplanMessquerschnitteDTOClass.setMessstelleId(messstelleId);
        listBelastungsplanMessquerschnitteDTOClass.setStadtbezirkNummer(messstelle.getStadtbezirkNummer());
        //listBelastungsplanMessquerschnitteDTOClass.setStrassenname(messstelle.getStandort());
        //listBelastungsplanMessquerschnitteDTOClass.setZeitraum(messstelle.);
        listBelastungsplanMessquerschnitteDTOClass.setStrassenname("Agnes-Pockels-Bogen");
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
        Integer totalSumKfz = totalSumOfAllMessquerschnitte.stream().mapToInt(TotalSumPerMessquerschnitt::getSumKfz).sum();
        Integer totalSumSv = totalSumOfAllMessquerschnitte.stream().mapToInt(TotalSumPerMessquerschnitt::getSumSv).sum();
        Integer totalSumGv = totalSumOfAllMessquerschnitte.stream().mapToInt(TotalSumPerMessquerschnitt::getSumGv).sum();
        Integer totalSumRad = totalSumOfAllMessquerschnitte.stream().mapToInt(TotalSumPerMessquerschnitt::getSumRad).sum();
        listBelastungsplanMessquerschnitteDTOClass.setTotalKfz(totalSumKfz);
        listBelastungsplanMessquerschnitteDTOClass.setTotalSv(totalSumSv);
        listBelastungsplanMessquerschnitteDTOClass.setTotalGv(totalSumGv);
        listBelastungsplanMessquerschnitteDTOClass.setTotalRad(totalSumRad);
        Integer totalSum = totalSumGv + totalSumKfz + totalSumSv;
        listBelastungsplanMessquerschnitteDTOClass.setTotalPercentGv(calcPercentage(totalSumGv, totalSum));
        listBelastungsplanMessquerschnitteDTOClass.setTotalPercentSv(calcPercentage(totalSumSv, totalSum));
        listBelastungsplanMessquerschnitteDTOClass.setLadeBelastungsplanMessquerschnittDataDTOList(listBelastungsplanMessquerschnitteDTO);
        return listBelastungsplanMessquerschnitteDTOClass;
    }

    protected String getDirection(ReadMessstelleInfoDTO messstelle, String messquerschnittId) {
          ReadMessquerschnittDTO messquerschnittDto = messstelle.getMessquerschnitte().stream().filter(readMessquerschnittDTO -> Objects.equals(readMessquerschnittDTO.getMqId(), messquerschnittId)).collect(Collectors.toList()).get(0);
          return messquerschnittDto.getFahrtrichtung();
    }

    protected BigDecimal calcPercentage(Integer part, Integer total) {
        BigDecimal partInBigDecimal = BigDecimal.valueOf(part);
        BigDecimal totalInBigDecimal = BigDecimal.valueOf(total);
        return partInBigDecimal.divide(totalInBigDecimal, 3, RoundingMode.HALF_UP).scaleByPowerOfTen(2);
    }
}
