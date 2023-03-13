package de.muenchen.dave.domain.dtos;

import com.github.javafaker.Faker;

public class BearbeiteHochrechnungsfaktorDTORandomFactory {

    public static HochrechnungsfaktorDTO getOne() {
        HochrechnungsfaktorDTO h = new HochrechnungsfaktorDTO();

        h.setGv(Faker.instance().number().randomDouble(3, 0, 3));
        h.setKfz(Faker.instance().number().randomDouble(3, 0, 3));
        h.setSv(Faker.instance().number().randomDouble(3, 0, 3));

        return h;
    }
}
