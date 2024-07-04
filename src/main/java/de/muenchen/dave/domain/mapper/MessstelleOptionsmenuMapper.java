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
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MessstelleOptionsmenuMapper {
    NichtPlausibleTageResponseDTO requestToResponse(NichtPlausibleTageDto nichtPlausibleTageDto);

    ValidWochentageInPeriodRequestDto backendToEaiRequestValidWochentage(ValidWochentageInPeriodEaiRequestDTO validWochentageInPeriodEaiRequestDTO);

    ValidWochentageInPeriodResponseDTO eaiToBackendResponseValidWochentage(ValidWochentageInPeriodDto validWochentageInPeriodDto);

    ChosenTagesTypValidRequestDto backendToEaiRequestChosenTageValid(ChosenTagesTypValidEaiRequestDTO chosenTagesTypValidEaiRequestDTO);

    ChosenTageValidResponseDTO eaiToBackendResponseChosenTageValid(ChosenTagesTypValidDTO chosenTagesTypValidDTO);
}
