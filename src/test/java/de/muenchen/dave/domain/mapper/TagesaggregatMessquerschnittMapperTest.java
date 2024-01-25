package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.dtos.NichtPlausibleTageResponseDTO;
import de.muenchen.dave.geodateneai.gen.model.NichtPlausibleTageDto;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class TagesaggregatMessquerschnittMapperTest {

    private final TagesaggregatMessquerschnittMapper mapper = new TagesaggregatMessquerschnittMapperImpl();

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
}
