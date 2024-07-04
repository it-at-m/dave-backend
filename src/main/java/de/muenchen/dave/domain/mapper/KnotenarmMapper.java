package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteKnotenarmDTO;
import de.muenchen.dave.domain.dtos.external.ExternalKnotenarmDTO;
import de.muenchen.dave.domain.elasticsearch.Knotenarm;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface KnotenarmMapper {

    Knotenarm bearbeitenDto2bean(BearbeiteKnotenarmDTO dto);

    List<Knotenarm> externalDtoList2beanList(List<ExternalKnotenarmDTO> dto);

    BearbeiteKnotenarmDTO bean2bearbeitenDto(Knotenarm bean);
}
