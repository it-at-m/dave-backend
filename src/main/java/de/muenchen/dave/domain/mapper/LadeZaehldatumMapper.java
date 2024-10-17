package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.dtos.laden.LadeAuswertungSpitzenstundeDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatumDTO;
import de.muenchen.dave.domain.pdf.helper.ZaehlungskenngroessenData;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface LadeZaehldatumMapper {

    LadeAuswertungSpitzenstundeDTO ladeZaehldatumDtoToLadeAuswertungSpitzenstundeDto(final LadeZaehldatumDTO ladeZaehldatumDTO);

    @Mapping(target = "startUhrzeit", source = "startUhrzeit", dateFormat = "HH:mm")
    @Mapping(target = "endeUhrzeit", source = "endeUhrzeit", dateFormat = "HH:mm")
    ZaehlungskenngroessenData ladeZaehldatumDtoToZaehlungskenngroessenData(final LadeZaehldatumDTO ladeZaehldatumDTO);

    List<ZaehlungskenngroessenData> ladeZaehldatumDtoListToZaehlungskenngroessenDataList(final List<LadeZaehldatumDTO> ladeZaehldatumDTO);

    @AfterMapping
    default void toZaehlungskenngroesse(@MappingTarget final ZaehlungskenngroessenData zaehlungskenngroessenData, final LadeZaehldatumDTO ladeZaehldatumDTO) {
        if (StringUtils.equals(zaehlungskenngroessenData.getEndeUhrzeit(), "23:59")) {
            zaehlungskenngroessenData.setEndeUhrzeit("24:00");
        }
    }
}
