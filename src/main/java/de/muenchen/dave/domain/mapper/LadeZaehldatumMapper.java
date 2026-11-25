package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.dtos.laden.LadeAuswertungSpitzenstundeDto;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatumDto;
import de.muenchen.dave.domain.pdf.helper.ZaehlungskenngroessenData;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface LadeZaehldatumMapper {

    LadeAuswertungSpitzenstundeDto ladeZaehldatumDtoToLadeAuswertungSpitzenstundeDto(final LadeZaehldatumDto ladeZaehldatumDTO);

    @Mapping(target = "startUhrzeit", source = "startUhrzeit", dateFormat = "HH:mm")
    @Mapping(target = "endeUhrzeit", source = "endeUhrzeit", dateFormat = "HH:mm")
    ZaehlungskenngroessenData ladeZaehldatumDtoToZaehlungskenngroessenData(final LadeZaehldatumDto ladeZaehldatumDTO);

    List<ZaehlungskenngroessenData> ladeZaehldatumDtoListToZaehlungskenngroessenDataList(final List<LadeZaehldatumDto> ladeZaehldatumDTO);

    @AfterMapping
    default void toZaehlungskenngroesse(@MappingTarget final ZaehlungskenngroessenData zaehlungskenngroessenData, final LadeZaehldatumDto ladeZaehldatumDTO) {
        if (StringUtils.equals(zaehlungskenngroessenData.getEndeUhrzeit(), "23:59")) {
            zaehlungskenngroessenData.setEndeUhrzeit("24:00");
        }
    }
}
