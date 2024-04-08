package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.dtos.NichtPlausibleTageResponseDTO;
import de.muenchen.dave.domain.dtos.ValidWochentageInPeriodEaiRequestDTO;
import de.muenchen.dave.domain.dtos.ValidWochentageInPeriodResponseDTO;
import de.muenchen.dave.geodateneai.gen.model.NichtPlausibleTageDto;
import de.muenchen.dave.geodateneai.gen.model.ValidWochentageInPeriodDto;
import de.muenchen.dave.geodateneai.gen.model.ValidWochentageInPeriodRequestDto;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

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
    void eaiToBackendResponse() {
        ValidWochentageInPeriodDto dto = new ValidWochentageInPeriodDto();
        dto.setNumberOfValidTagesTypDiMiDo(5);
        dto.setNumberOfValidTagesTypMoFr(7);
        dto.setNumberOfValidTagesTypSamstag(2);
        dto.setNumberOfValidTagesTypSonntagFeiertag(1);
        dto.setNumberOfValidTagesTypWerktagFerien(10);
        dto.setNumberOfValidTagesTypMoSo(15);

        ValidWochentageInPeriodResponseDTO response = mapper.eaiToBackendResponse(dto);

        ValidWochentageInPeriodResponseDTO expectedResponse = new ValidWochentageInPeriodResponseDTO();
        expectedResponse.setNumberOfValidTagesTypDiMiDo(5);
        expectedResponse.setNumberOfValidTagesTypMoFr(7);
        expectedResponse.setNumberOfValidTagesTypSamstag(2);
        expectedResponse.setNumberOfValidTagesTypSonntagFeiertag(1);
        expectedResponse.setNumberOfValidTagesTypWerktagFerien(10);
        expectedResponse.setNumberOfValidTagesTypMoSo(15);

        Assertions.assertThat(response).usingRecursiveComparison().isEqualTo(expectedResponse);
    }

    @Test
    void backendToEaiRequest() {
        ValidWochentageInPeriodEaiRequestDTO request = new ValidWochentageInPeriodEaiRequestDTO();
        request.setStartDate("2022-01-01");
        request.setEndDate("2022-01-31");
        request.setMessstelleId("12345");

        ValidWochentageInPeriodRequestDto validWochentageInPeriodRequestDto = mapper.backendToEaiRequest(request);

        ValidWochentageInPeriodRequestDto expectedEaiRequest = new ValidWochentageInPeriodRequestDto();
        expectedEaiRequest.setStartDate("2022-01-01");
        expectedEaiRequest.setEndDate("2022-01-31");
        expectedEaiRequest.setMessstelleId("12345");

        Assertions.assertThat(validWochentageInPeriodRequestDto).usingRecursiveComparison().isEqualTo(expectedEaiRequest);
    }
}
