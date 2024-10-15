package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.dtos.ChosenTageValidResponseDTO;
import de.muenchen.dave.domain.dtos.ChosenTagesTypValidEaiRequestDTO;
import de.muenchen.dave.domain.dtos.NichtPlausibleTageResponseDTO;
import de.muenchen.dave.domain.enums.TagesTyp;
import de.muenchen.dave.geodateneai.gen.model.ChosenTagesTypValidDTO;
import de.muenchen.dave.geodateneai.gen.model.ChosenTagesTypValidRequestDto;
import de.muenchen.dave.geodateneai.gen.model.NichtPlausibleTageDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

class MessstelleOptionsmenuMapperTest {

    private final MessstelleOptionsmenuMapper mapper = new MessstelleOptionsmenuMapperImpl();

    @Test
    void requestToResponse() {
        ArrayList<LocalDate> listOfDates = new ArrayList<>(List.of(LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 1, 3),
                LocalDate.of(2024, 1, 7)));
        final NichtPlausibleTageDto requested = new NichtPlausibleTageDto();
        requested.setNichtPlausibleTage(listOfDates);

        final NichtPlausibleTageResponseDTO response = new NichtPlausibleTageResponseDTO();
        response.setNichtPlausibleTage(listOfDates);

        Assertions.assertThat(this.mapper.requestToResponse(requested))
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(response);
    }

    @Test
    void backendToEaiRequestChosenTageValidChosenValidWochentage() {

        ChosenTagesTypValidEaiRequestDTO eaiRequest = new ChosenTagesTypValidEaiRequestDTO();
        eaiRequest.setStartDate("2022-01-01");
        eaiRequest.setEndDate("2022-01-03");
        eaiRequest.setTagesTyp(TagesTyp.SAMSTAG);

        ChosenTagesTypValidRequestDto expectedRequest = new ChosenTagesTypValidRequestDto();
        expectedRequest.setStartDate("2022-01-01");
        expectedRequest.setEndDate("2022-01-03");
        expectedRequest.setTagesTyp(ChosenTagesTypValidRequestDto.TagesTypEnum.SAMSTAG);

        ChosenTagesTypValidRequestDto actualRequest = mapper.backendToEaiRequestChosenTageValid(eaiRequest);

        Assertions.assertThat(actualRequest).isNotNull();
        Assertions.assertThat(actualRequest).usingRecursiveComparison().isEqualTo(expectedRequest);
    }

    @Test
    void eaiToBackendResponseChosenTageValidChosenValidWochentage() {
        ChosenTagesTypValidDTO chosenTagesTypValidDTO = new ChosenTagesTypValidDTO();
        chosenTagesTypValidDTO.setIsValid(true);

        ChosenTageValidResponseDTO expectedResponse = new ChosenTageValidResponseDTO();
        expectedResponse.setIsValid(true);

        ChosenTageValidResponseDTO actualResponse = mapper.eaiToBackendResponseChosenTageValid(chosenTagesTypValidDTO);

        Assertions.assertThat(actualResponse).isNotNull();
        Assertions.assertThat(actualResponse).usingRecursiveComparison().isEqualTo(expectedResponse);

    }
}
