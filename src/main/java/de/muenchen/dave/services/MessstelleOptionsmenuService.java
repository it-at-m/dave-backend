package de.muenchen.dave.services;

import de.muenchen.dave.domain.dtos.ChosenTageValidResponseDTO;
import de.muenchen.dave.domain.dtos.ChosenTagesTypValidEaiRequestDTO;
import de.muenchen.dave.domain.dtos.NichtPlausibleTageResponseDTO;
import de.muenchen.dave.domain.dtos.ValidWochentageInPeriodEaiRequestDTO;
import de.muenchen.dave.domain.dtos.ValidWochentageInPeriodResponseDTO;
import de.muenchen.dave.domain.mapper.MessstelleOptionsmenuMapper;
import de.muenchen.dave.geodateneai.gen.api.MessstelleOptionsmenuControllerApi;
import de.muenchen.dave.geodateneai.gen.model.ChosenTagesTypValidDTO;
import de.muenchen.dave.geodateneai.gen.model.ChosenTagesTypValidRequestDto;
import de.muenchen.dave.geodateneai.gen.model.NichtPlausibleTageDto;
import de.muenchen.dave.geodateneai.gen.model.ValidWochentageInPeriodDto;
import de.muenchen.dave.geodateneai.gen.model.ValidWochentageInPeriodRequestDto;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

    public ChosenTageValidResponseDTO isTagesTypValid(ChosenTagesTypValidEaiRequestDTO chosenTagesTypValidEaiRequestDTO) {
        ChosenTagesTypValidRequestDto chosenTagesTypValidRequestDto = messstelleOptionsmenuMapper.backendToEaiRequestChosenTageValid(chosenTagesTypValidEaiRequestDTO);
        ChosenTagesTypValidDTO chosenTagesTypValidDTO = Objects.requireNonNull(messstelleOptionsmenuControllerApi.isTagesTypDataValidWithHttpInfo(chosenTagesTypValidRequestDto).block()).getBody();
        return messstelleOptionsmenuMapper.eaiToBackendResponseChosenTageValid(chosenTagesTypValidDTO);
    }

    public ValidWochentageInPeriodResponseDTO getValidWochentageInPeriod(ValidWochentageInPeriodEaiRequestDTO validWochentageInPeriodRequestDto) {
        ValidWochentageInPeriodRequestDto validWochentageInPeriodEaiRequestDTO = messstelleOptionsmenuMapper
                .backendToEaiRequestValidWochentage(validWochentageInPeriodRequestDto);
        ValidWochentageInPeriodDto validWochentageInPeriodDto = Objects.requireNonNull(messstelleOptionsmenuControllerApi.getValidWochentageInPeriodWithHttpInfo(validWochentageInPeriodEaiRequestDTO).block(),
                "Die Anfrage für Valide Wochentage innerhalb eines Zeitraums hat null zurückgegeben").getBody();
        return messstelleOptionsmenuMapper.eaiToBackendResponseValidWochentage(validWochentageInPeriodDto);
    }
}
