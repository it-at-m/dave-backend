package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.Hochrechnungsfaktor;
import de.muenchen.dave.domain.dtos.HochrechnungsfaktorDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface HochrechnungsfaktorMapper {

    @Mapping(target = "version", source = "entityVersion")
    de.muenchen.dave.domain.elasticsearch.Hochrechnungsfaktor dto2beanElastic(HochrechnungsfaktorDTO dto);

    @Mapping(target = "version", source = "entityVersion")
    de.muenchen.dave.domain.Hochrechnungsfaktor dto2bean(HochrechnungsfaktorDTO dto);

    @Mapping(target = "entityVersion", source = "version")
    HochrechnungsfaktorDTO beanElastic2Dto(de.muenchen.dave.domain.elasticsearch.Hochrechnungsfaktor bean);

    @Mapping(target = "entityVersion", source = "version")
    HochrechnungsfaktorDTO bean2Dto(Hochrechnungsfaktor bean);
}
