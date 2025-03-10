package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.dtos.ChosenTageValidResponseDTO;
import de.muenchen.dave.domain.dtos.ChosenTagesTypValidEaiRequestDTO;
import de.muenchen.dave.domain.enums.TagesTyp;
import de.muenchen.dave.geodateneai.gen.model.ChosenTagesTypValidDTO;
import de.muenchen.dave.geodateneai.gen.model.ChosenTagesTypValidRequestDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class MessstelleOptionsmenuMapperTest {

    private final MessstelleOptionsmenuMapper mapper = new MessstelleOptionsmenuMapperImpl();

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
