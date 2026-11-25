package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.Dienstleister;
import de.muenchen.dave.domain.dtos.DienstleisterDto;
import java.util.List;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DienstleisterMapper {

    Dienstleister dto2bean(DienstleisterDto dto);

    DienstleisterDto bean2Dto(Dienstleister bean);

    List<DienstleisterDto> beanList2DtoList(List<Dienstleister> bean);

    @AfterMapping
    default void bean2Dto(@MappingTarget final DienstleisterDto dto, final Dienstleister bean) {
        dto.setEmailAddressesAsString(String.join(", ", bean.getEmailAddresses()));
    }

}
