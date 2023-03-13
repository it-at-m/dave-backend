package de.muenchen.dave.services;

import de.muenchen.dave.domain.Hochrechnungsfaktor;
import de.muenchen.dave.domain.dtos.HochrechnungsfaktorDTO;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


class HochrechnungsfaktorServiceTest {

    @Test
    void setDtoDataToEntity() {
        final Hochrechnungsfaktor entity = new Hochrechnungsfaktor();
        final HochrechnungsfaktorDTO dto = new HochrechnungsfaktorDTO();
        dto.setMatrix("MATRIX");
        dto.setKfz(99.9);
        dto.setGv(55.5);
        dto.setSv(11.1);
        dto.setActive(true);
        dto.setDefaultFaktor(true);

        final Hochrechnungsfaktor expected = new Hochrechnungsfaktor();
        expected.setMatrix("MATRIX");
        expected.setKfz(99.9);
        expected.setGv(55.5);
        expected.setSv(11.1);
        expected.setActive(true);
        expected.setDefaultFaktor(true);

        final Hochrechnungsfaktor result = HochrechnungsfaktorService.setDtoDataToEntity(dto, entity);

        assertThat(result, is(expected));
    }

}
