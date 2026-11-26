package de.muenchen.dave.domain.dtos;

import com.github.javafaker.Faker;

public class BearbeiteHochrechnungsfaktorDtoRandomFactory {

    public static HochrechnungsfaktorDto getOne() {
        HochrechnungsfaktorDto h = new HochrechnungsfaktorDto();

        h.setGv(Faker.instance().number().randomDouble(3, 0, 3));
        h.setKfz(Faker.instance().number().randomDouble(3, 0, 3));
        h.setSv(Faker.instance().number().randomDouble(3, 0, 3));

        return h;
    }
}
