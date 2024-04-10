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

@Mapper(componentModel = "spring")
public interface MessstelleOptionsmenuMapper {
    NichtPlausibleTageResponseDTO requestToResponse(NichtPlausibleTageDto nichtPlausibleTageDto);

    ValidWochentageInPeriodRequestDto backendToEaiRequest(ValidWochentageInPeriodEaiRequestDTO validWochentageInPeriodEaiRequestDTO);

    ValidWochentageInPeriodResponseDTO eaiToBackendResponse(ValidWochentageInPeriodDto validWochentageInPeriodDto);

    ChosenTagesTypValidRequestDto backendToEaiRequest(ChosenTagesTypValidEaiRequestDTO chosenTagesTypValidEaiRequestDTO);

    ChosenTageValidResponseDTO eaiToBackendResponse(ChosenTagesTypValidDTO chosenTagesTypValidDTO);
}
