package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.dtos.NichtPlausibleTageEaiRequestDTO;
import de.muenchen.dave.domain.dtos.NichtPlausibleTageResponseDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TagesaggregatMessquerschnittMapper {
    NichtPlausibleTageResponseDTO requestToResponse(NichtPlausibleTageEaiRequestDTO nichtPlausibleTageEaiRequestDTO);

}
