package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.Hochrechnungsfaktor;
import de.muenchen.dave.domain.dtos.HochrechnungsfaktorDTO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface HochrechnungsfaktorMapper {

    Hochrechnungsfaktor dto2bean(HochrechnungsfaktorDTO dto);

    HochrechnungsfaktorDTO bean2Dto(Hochrechnungsfaktor bean);
}
