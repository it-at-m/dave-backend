package de.muenchen.dave.migrationespg.domain.mapper;

import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteKnotenarmDTO;
import de.muenchen.dave.domain.dtos.external.ExternalKnotenarmDTO;
import de.muenchen.dave.migrationespg.domain.relational.zaehlstellen.KnotenarmEntity;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface KnotenarmMapper {

    KnotenarmEntity bearbeitenDto2bean(BearbeiteKnotenarmDTO dto);

    List<KnotenarmEntity> externalDtoList2beanList(List<ExternalKnotenarmDTO> dto);

    BearbeiteKnotenarmDTO bean2bearbeitenDto(KnotenarmEntity bean);
}
