package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.dtos.ChosenTageValidResponseDTO;
import de.muenchen.dave.domain.dtos.ChosenTagesTypValidEaiRequestDTO;
import de.muenchen.dave.domain.dtos.NichtPlausibleTageResponseDTO;
import de.muenchen.dave.domain.dtos.ValidWochentageInPeriodEaiRequestDTO;
import de.muenchen.dave.domain.dtos.ValidWochentageInPeriodResponseDTO;
import de.muenchen.dave.geodateneai.gen.model.ChosenTagesTypValidDTO;
import de.muenchen.dave.geodateneai.gen.model.ChosenTagesTypValidRequestDto;
import de.muenchen.dave.geodateneai.gen.model.NichtPlausibleTageDto;
import de.muenchen.dave.geodateneai.gen.model.ValidWochentageInPeriodDto;
import de.muenchen.dave.geodateneai.gen.model.ValidWochentageInPeriodRequestDto;
import org.apache.commons.lang3.ObjectUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MessstelleOptionsmenuMapper {
    NichtPlausibleTageResponseDTO requestToResponse(NichtPlausibleTageDto nichtPlausibleTageDto);

    ValidWochentageInPeriodRequestDto backendToEaiRequestValidWochentage(ValidWochentageInPeriodEaiRequestDTO validWochentageInPeriodEaiRequestDTO);

    ValidWochentageInPeriodResponseDTO eaiToBackendResponseValidWochentage(ValidWochentageInPeriodDto validWochentageInPeriodDto);

    @Mapping(target = "tagesTyp", ignore = true)
    ChosenTagesTypValidRequestDto backendToEaiRequestChosenTageValid(ChosenTagesTypValidEaiRequestDTO chosenTagesTypValidEaiRequestDTO);

    @AfterMapping
    default void backendToEaiRequestChosenTageValid(
            @MappingTarget final ChosenTagesTypValidRequestDto target,
            final ChosenTagesTypValidEaiRequestDTO source) {
        final var tagesTyp = ObjectUtils.isNotEmpty(source.getTagesTyp())
                ? ChosenTagesTypValidRequestDto.TagesTypEnum.valueOf(source.getTagesTyp().name())
                : null;
        target.setTagesTyp(tagesTyp);
    }

    ChosenTageValidResponseDTO eaiToBackendResponseChosenTageValid(ChosenTagesTypValidDTO chosenTagesTypValidDTO);
}
