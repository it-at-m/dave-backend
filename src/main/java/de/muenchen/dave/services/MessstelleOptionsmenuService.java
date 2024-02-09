package de.muenchen.dave.services;

import de.muenchen.dave.domain.dtos.NichtPlausibleTageResponseDTO;
import de.muenchen.dave.domain.mapper.MessstelleOptionsmenuMapper;
import de.muenchen.dave.geodateneai.gen.api.MessstelleOptionsmenuControllerApi;
import de.muenchen.dave.geodateneai.gen.model.ChosenTagesTypValidDTO;
import de.muenchen.dave.geodateneai.gen.model.NichtPlausibleTageDto;
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


    // TODO Tagestyp als enum oder string? Unterschied zwischen EAI und Backend Response, 2 Dtos notwendig, mapping?
    public ChosenTagesTypValidDTO isTagesTypValid(String startDate, String endDate, String tagesTyp) {
        return Objects.requireNonNull(messstelleOptionsmenuControllerApi.isTagesTypDataValidWithHttpInfo(startDate, endDate, tagesTyp).block()).getBody();
    }
}
