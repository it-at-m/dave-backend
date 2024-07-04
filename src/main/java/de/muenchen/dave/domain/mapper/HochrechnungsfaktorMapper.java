package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.Hochrechnungsfaktor;
import de.muenchen.dave.domain.dtos.HochrechnungsfaktorDTO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface HochrechnungsfaktorMapper {

    de.muenchen.dave.domain.elasticsearch.Hochrechnungsfaktor dto2beanElastic(HochrechnungsfaktorDTO dto);

    de.muenchen.dave.domain.Hochrechnungsfaktor dto2bean(HochrechnungsfaktorDTO dto);

    HochrechnungsfaktorDTO beanElastic2Dto(de.muenchen.dave.domain.elasticsearch.Hochrechnungsfaktor bean);

    HochrechnungsfaktorDTO bean2Dto(Hochrechnungsfaktor bean);
}
