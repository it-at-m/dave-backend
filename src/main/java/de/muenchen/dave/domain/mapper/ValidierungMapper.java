package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.dtos.messstelle.ValidateZeitraumAndTagestypForMessstelleDTO;
import de.muenchen.dave.domain.model.messstelle.ValidateZeitraumAndTagesTypForMessstelleModel;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ValidierungMapper {

    ValidateZeitraumAndTagesTypForMessstelleModel dto2model(ValidateZeitraumAndTagestypForMessstelleDTO dto);
}
