package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.Hochrechnungsfaktor;
import de.muenchen.dave.domain.dtos.HochrechnungsfaktorDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface HochrechnungsfaktorMapper {

    de.muenchen.dave.domain.elasticsearch.Hochrechnungsfaktor dto2beanElastic(HochrechnungsfaktorDto dto);

    de.muenchen.dave.domain.Hochrechnungsfaktor dto2bean(HochrechnungsfaktorDto dto);

    HochrechnungsfaktorDto beanElastic2Dto(de.muenchen.dave.domain.elasticsearch.Hochrechnungsfaktor bean);

    HochrechnungsfaktorDto bean2Dto(Hochrechnungsfaktor bean);
}
