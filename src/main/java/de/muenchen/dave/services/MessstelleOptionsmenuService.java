package de.muenchen.dave.services;

import de.muenchen.dave.domain.dtos.ChosenTageValidResponseDTO;
import de.muenchen.dave.domain.dtos.ChosenTagesTypValidEaiRequestDTO;
import de.muenchen.dave.domain.dtos.NichtPlausibleTageResponseDTO;
import de.muenchen.dave.domain.mapper.MessstelleOptionsmenuMapper;
import de.muenchen.dave.geodateneai.gen.api.MessstelleOptionsmenuControllerApi;
import de.muenchen.dave.geodateneai.gen.model.ChosenTagesTypValidDTO;
import de.muenchen.dave.geodateneai.gen.model.ChosenTagesTypValidRequestDto;
import de.muenchen.dave.geodateneai.gen.model.NichtPlausibleTageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessstelleOptionsmenuService {
    private final MessstelleOptionsmenuMapper messstelleOptionsmenuMapper;
    private final MessstelleOptionsmenuControllerApi messstelleOptionsmenuControllerApi;

    public NichtPlausibleTageResponseDTO getNichtPlausibleDatenFromEai(final String messquerschnittId) {
        final NichtPlausibleTageDto eaiRequestResult = Objects
                .requireNonNull(messstelleOptionsmenuControllerApi.getNichtPlausibleTageWithHttpInfo(messquerschnittId).block()).getBody();
        return messstelleOptionsmenuMapper.requestToResponse(eaiRequestResult);
    }

    public ChosenTageValidResponseDTO isTagesTypValid(final ChosenTagesTypValidEaiRequestDTO chosenTagesTypValidEaiRequestDTO) {
        final ChosenTagesTypValidRequestDto chosenTagesTypValidRequestDto = messstelleOptionsmenuMapper
                .backendToEaiRequestChosenTageValid(chosenTagesTypValidEaiRequestDTO);
        final ChosenTagesTypValidDTO chosenTagesTypValidDTO = Objects
                .requireNonNull(messstelleOptionsmenuControllerApi.isTagesTypDataValidWithHttpInfo(chosenTagesTypValidRequestDto).block()).getBody();
        return messstelleOptionsmenuMapper.eaiToBackendResponseChosenTageValid(chosenTagesTypValidDTO);
    }
}
