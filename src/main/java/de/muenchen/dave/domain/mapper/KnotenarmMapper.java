package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteKnotenarmDTO;
import de.muenchen.dave.domain.dtos.external.ExternalKnotenarmDTO;
import de.muenchen.dave.relational.zaehlstellen.KnotenarmEntity;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface KnotenarmMapper {

    KnotenarmEntity bearbeitenDto2bean(BearbeiteKnotenarmDTO dto);

    List<KnotenarmEntity> externalDtoList2beanList(List<ExternalKnotenarmDTO> dto);

    BearbeiteKnotenarmDTO bean2bearbeitenDto(KnotenarmEntity bean);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdTime", ignore = true)
    @Mapping(target = "zaehlung", ignore = true)
    @Mapping(target = "filename", ignore = true)
    void updateEntity(BearbeiteKnotenarmDTO dto, @MappingTarget KnotenarmEntity entity
    );
}
