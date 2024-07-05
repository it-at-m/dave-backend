package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.Dienstleister;
import de.muenchen.dave.domain.dtos.DienstleisterDTO;
import java.util.List;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
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
