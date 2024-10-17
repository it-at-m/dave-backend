package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.Dienstleister;
import de.muenchen.dave.domain.dtos.DienstleisterDTO;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DienstleisterMapper {

    Dienstleister dto2bean(DienstleisterDTO dto);

    List<Dienstleister> dtoList2beanList(List<DienstleisterDTO> dto);

    DienstleisterDTO bean2Dto(Dienstleister bean);

    List<DienstleisterDTO> beanList2DtoList(List<Dienstleister> bean);

    @AfterMapping
    default void bean2Dto(@MappingTarget final DienstleisterDTO dto, final Dienstleister bean) {
        dto.setEmailAddressesAsString(String.join(", ", bean.getEmailAddresses()));
    }

}
