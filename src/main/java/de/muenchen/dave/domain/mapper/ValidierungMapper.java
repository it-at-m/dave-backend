package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.dtos.messstelle.ValidateZeitraumAndTagestypForMessstelleDTO;
import de.muenchen.dave.domain.model.messstelle.ValidateZeitraumAndTagesTypForMessstelleModel;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ValidierungMapper {

    @Mapping(source = "zeitraum", target = "zeitraum", ignore = true)
    ValidateZeitraumAndTagesTypForMessstelleModel dto2model(final ValidateZeitraumAndTagestypForMessstelleDTO dto);

    @AfterMapping
    default void dto2modelAfterMapping(
            final ValidateZeitraumAndTagestypForMessstelleDTO dto,
            @MappingTarget final ValidateZeitraumAndTagesTypForMessstelleModel model) {

    }
}
